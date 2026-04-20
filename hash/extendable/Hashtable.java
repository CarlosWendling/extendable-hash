package hash.extendable;

@SuppressWarnings("unchecked")
public class Hashtable<K, V> {
    // Diretorio: array de ponteiros para buckets. Varias posicoes podem apontar para o mesmo bucket.
    private Bucket<K, V>[] directory;
    // Quantidade de bits do hash usados para indexar o diretorio (tamanho = 2^globalDepth).
    private int globalDepth;
    // Capacidade fixa de cada bucket (quantas entradas cabem antes de precisar split).
    private final int bucketCapacity;

    public Hashtable(int bucketCapacity) {
        if (bucketCapacity <= 0) throw new IllegalArgumentException();
        this.bucketCapacity = bucketCapacity;
        // Estado inicial: 1 bucket, diretorio de 1 posicao, nenhum bit do hash em uso.
        this.globalDepth = 0;
        this.directory = new Bucket[1];
        this.directory[0] = new Bucket<>(bucketCapacity, 0);
    }

    // Hash usando modulo pelo tamanho do diretorio (conforme proposta: mod %).
    private int hash(K key) {
        return Math.abs(key.hashCode()) % directory.length;
    }

    // Exposto apenas para logging nos testes (ver Main.java).
    public int getHashIndex(K key) {
        if (key == null) throw new NullPointerException();
        return hash(key);
    }

    public V put(K key, V value) {
        if (key == null) throw new NullPointerException();

        int index = hash(key);
        Bucket<K, V> bucket = directory[index];

        // Se o bucket esta cheio e a chave ainda nao existe nele, precisa dividir.
        // Em cascata: split pode nao separar os elementos o suficiente, entao repetimos ate caber.
        // containsKey evita split desnecessario quando a operacao e apenas um update.
        while (bucket.isFull() && !bucket.containsKey(key)) {
            split(bucket);
            index = hash(key);
            bucket = directory[index];
        }

        return bucket.put(key, value);
    }

    public V get(K key) {
        if (key == null) throw new NullPointerException();
        int index = hash(key);
        return directory[index].get(key);
    }

    public V remove(K key) {
        if (key == null) throw new NullPointerException();
        int index = hash(key);
        Bucket<K, V> bucket = directory[index];
        V value = bucket.remove(key);

        // Bucket ficou vazio apos remocao: tenta unir com o buddy para economizar espaco.
        if (bucket.isEmpty()) {
            merge(bucket, index);
        }

        return value;
    }

    private void split(Bucket<K, V> bucket) {
        // Se o bucket ja usa todos os bits disponiveis, nao da para distinguir mais entradas
        // sem aumentar o diretorio: dobramos seu tamanho e incrementamos globalDepth.
        if (bucket.getLocalDepth() == globalDepth) {
            int oldSize = directory.length;
            Bucket<K, V>[] newDir = new Bucket[oldSize * 2];
            // Replicamos cada ponteiro na metade superior: os novos indices ainda apontam
            // para os mesmos buckets ate que um split real os separe.
            for (int i = 0; i < oldSize; i++) {
                newDir[i] = directory[i];
                newDir[i + oldSize] = directory[i];
            }
            directory = newDir;
            globalDepth++;
        }

        // Cria o bucket irmao e aumenta o localDepth de ambos (agora olham mais 1 bit do hash).
        int oldLocalDepth = bucket.getLocalDepth();
        Bucket<K, V> newBucket = new Bucket<>(bucketCapacity, oldLocalDepth + 1);
        bucket.setLocalDepth(oldLocalDepth + 1);

        // Redistribui as entradas do bucket original entre ele e o novo,
        // usando o bit na posicao 'oldLocalDepth' (o novo bit discriminador) do hashCode.
        bucket.splitInto(newBucket, oldLocalDepth);

        // Atualiza o diretorio: metade dos indices que apontavam para o bucket antigo
        // agora devem apontar para o newBucket (aqueles cujo bit discriminador e 1).
        int bitPos = oldLocalDepth;
        for (int i = 0; i < directory.length; i++) {
            if (directory[i] == bucket && ((i >> bitPos) & 1) == 1) {
                directory[i] = newBucket;
            }
        }
    }

    private void merge(Bucket<K, V> bucket, int index) {
        // localDepth 0 significa que o bucket responde por todo o diretorio; nao tem com quem unir.
        if (bucket.getLocalDepth() == 0) return;

        // O buddy e o outro bucket que difere apenas no bit mais significativo do prefixo local.
        int buddyIndex = index ^ (1 << (bucket.getLocalDepth() - 1));
        Bucket<K, V> buddy = directory[buddyIndex];

        // So faz merge quando os dois buckets estao no mesmo nivel de profundidade.
        if (buddy.getLocalDepth() != bucket.getLocalDepth()) return;

        // Junta as entradas no buddy e diminui o localDepth (passa a responder por um prefixo menor).
        int newDepth = bucket.getLocalDepth() - 1;
        buddy.mergeFrom(bucket);
        buddy.setLocalDepth(newDepth);

        // Redireciona todos os indices do diretorio que apontavam para o bucket removido.
        for (int i = 0; i < directory.length; i++) {
            if (directory[i] == bucket) {
                directory[i] = buddy;
            }
        }

        // Se nenhum bucket depende mais do bit mais alto, encolhemos o diretorio.
        if (canReduceGlobalDepth()) {
            reduceGlobalDepth();
        }
    }

    // Verifica se a metade superior do diretorio e identica a inferior:
    // nesse caso o bit mais significativo e redundante e pode ser descartado.
    private boolean canReduceGlobalDepth() {
        int half = directory.length / 2;
        for (int i = 0; i < half; i++) {
            if (directory[i] != directory[i + half]) {
                return false;
            }
        }
        return true;
    }

    // Reduz o diretorio a metade do tamanho e decrementa globalDepth.
    private void reduceGlobalDepth() {
        int newSize = directory.length / 2;
        Bucket<K, V>[] newDir = new Bucket[newSize];
        System.arraycopy(directory, 0, newDir, 0, newSize);
        directory = newDir;
        globalDepth--;
    }

    // Imprime o estado do diretorio para inspecao manual nos testes.
    public void printStructure() {
        System.out.println("===========================================");
        System.out.println("  Extendable Hash Table Structure");
        System.out.println("===========================================");
        System.out.println("  Global Depth: " + globalDepth);
        System.out.println("  Directory Size: " + directory.length);
        System.out.println("  Bucket Capacity: " + bucketCapacity);
        System.out.println("-------------------------------------------");

        // Como varios indices podem apontar para o mesmo bucket, so imprimimos o conteudo uma vez.
        java.util.Set<Bucket<K, V>> printed = new java.util.HashSet<>();

        for (int i = 0; i < directory.length; i++) {
            Bucket<K, V> bucket = directory[i];
            String prefix = String.format("  [%2d] -> ", i);

            if (printed.contains(bucket)) {
                System.out.println(prefix + "(same as above)");
                continue;
            }
            printed.add(bucket);

            System.out.println(prefix + "Bucket(localDepth=" + bucket.getLocalDepth()
                    + ", count=" + bucket.getCount() + "/" + bucketCapacity + ")");

            for (int j = 0; j < bucket.getCount(); j++) {
                Entry<K, V> entry = bucket.getEntry(j);
                if (entry != null) {
                    System.out.println("         [" + j + "] " + entry.getKey()
                            + " = " + entry.getValue());
                }
            }
        }
        System.out.println("===========================================");
    }
}
