package hash.extendable;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Teste 1: Insercao e Busca Basica ===");
        testBasicPutGet();

        System.out.println("\n=== Teste 2: Atualizacao de Valor ===");
        testUpdate();

        System.out.println("\n=== Teste 3: Split de Bucket ===");
        testSplit();

        System.out.println("\n=== Teste 4: Remocao e Merge ===");
        testRemoveAndMerge();

        System.out.println("\n=== Teste 5: Cenario Completo ===");
        testFullScenario();

        System.out.println("\n=== Todos os testes passaram! ===");
    }

    private static void testBasicPutGet() {
        Hashtable<String, Integer> ht = new Hashtable<>(3);
        String[][] inserts = {{"chave1", "10"}, {"chave2", "20"}, {"chave3", "30"}};
        for (String[] kv : inserts) {
            String key = kv[0];
            Integer val = Integer.parseInt(kv[1]);
            System.out.println("  Insert: key=" + key + ", value=" + val + ", dirIndex=" + ht.getHashIndex(key));
            ht.put(key, val);
        }

        assert ht.get("chave1") == 10 : "Falha: chave1";
        assert ht.get("chave2") == 20 : "Falha: chave2";
        assert ht.get("chave3") == 30 : "Falha: chave3";
        assert ht.get("inexistente") == null : "Falha: chave inexistente deve retornar null";
        System.out.println("  [OK] Insercao e busca basica");
        ht.printStructure();
    }

    private static void testUpdate() {
        Hashtable<String, String> ht = new Hashtable<>(2);
        System.out.println("  Insert: key=nome, value=Joao, dirIndex=" + ht.getHashIndex("nome"));
        ht.put("nome", "Joao");
        assert ht.get("nome").equals("Joao") : "Falha: valor original";

        System.out.println("  Insert: key=nome, value=Maria, dirIndex=" + ht.getHashIndex("nome"));
        String old = ht.put("nome", "Maria");
        assert old.equals("Joao") : "Falha: deveria retornar valor antigo";
        assert ht.get("nome").equals("Maria") : "Falha: valor atualizado";
        System.out.println("  [OK] Atualizacao de valor");
        ht.printStructure();
    }

    private static void testSplit() {
        Hashtable<Integer, String> ht = new Hashtable<>(2);
        System.out.println("  Insert: key=1, value=um, dirIndex=" + ht.getHashIndex(1));
        ht.put(1, "um");
        System.out.println("  Insert: key=2, value=dois, dirIndex=" + ht.getHashIndex(2));
        ht.put(2, "dois");

        System.out.println("  Inserir 3 elementos em bucket de capacidade 2...");
        System.out.println("  Insert: key=3, value=tres, dirIndex=" + ht.getHashIndex(3));
        ht.put(3, "tres");

        assert ht.get(1) != null : "Falha: chave1 apos split";
        assert ht.get(2) != null : "Falha: chave2 apos split";
        assert ht.get(3) != null : "Falha: chave3 apos split";
        System.out.println("  [OK] Split de bucket funcionando");
        ht.printStructure();
    }

    private static void testRemoveAndMerge() {
        Hashtable<Integer, String> ht = new Hashtable<>(2);
        System.out.println("  Insert: key=1, value=um, dirIndex=" + ht.getHashIndex(1));
        ht.put(1, "um");
        System.out.println("  Insert: key=2, value=dois, dirIndex=" + ht.getHashIndex(2));
        ht.put(2, "dois");
        System.out.println("  Insert: key=3, value=tres, dirIndex=" + ht.getHashIndex(3));
        ht.put(3, "tres");

        System.out.println("  --- Bucket structure after inserts ---");
        ht.printStructure();

        System.out.println("  Removendo todas as chaves...");
        ht.remove(1);
        ht.remove(2);
        ht.remove(3);

        assert ht.get(1) == null : "Falha: chave1 deve ser null apos remocao";
        assert ht.get(2) == null : "Falha: chave2 deve ser null apos remocao";
        assert ht.get(3) == null : "Falha: chave3 deve ser null apos remocao";
        System.out.println("  [OK] Remocao e merge funcionando");
        ht.printStructure();
    }

    private static void testFullScenario() {
        Hashtable<String, Integer> ht = new Hashtable<>(3);

        System.out.println("  Inserindo 10 chaves...");
        for (int i = 0; i < 10; i++) {
            String key = "item" + i;
            System.out.println("  Insert: key=" + key + ", value=" + (i * 10) + ", dirIndex=" + ht.getHashIndex(key));
            ht.put(key, i * 10);
        }

        System.out.println("  Verificando todas as chaves...");
        for (int i = 0; i < 10; i++) {
            Integer val = ht.get("item" + i);
            assert val != null && val == i * 10 : "Falha: item" + i;
        }

        System.out.println("  --- Bucket structure after all inserts ---");
        ht.printStructure();

        System.out.println("  Removendo chaves pares...");
        for (int i = 0; i < 10; i += 2) {
            ht.remove("item" + i);
        }

        System.out.println("  Verificando chaves restantes...");
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                assert ht.get("item" + i) == null : "Falha: chave par deve ter sido removida";
            } else {
                assert ht.get("item" + i) == i * 10 : "Falha: chave impar deve existir";
            }
        }

        System.out.println("  [OK] Cenario completo");
        ht.printStructure();
    }
}
