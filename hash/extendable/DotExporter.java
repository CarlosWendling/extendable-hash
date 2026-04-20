package hash.extendable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Responsável por exportar a estrutura da hash table extensível
 * para formato DOT (Graphviz) e gerar visualizações SVG automaticamente.
 */
public class DotExporter {

  /**
   * @param ht A hash table a ser visualizada
   * @param testName Nome do teste para identificar os arquivos gerados
   */
  public static void export(Hashtable<?, ?> ht, String testName) {
    try {
      Path projectDir = Paths.get("").toAbsolutePath();
      Path dotDir = projectDir.resolve("dotFiles");
      Path svgDir = projectDir.resolve("svgFiles");
      Files.createDirectories(dotDir);
      Files.createDirectories(svgDir);

      String safeName = testName.replaceAll("[^a-zA-Z0-9_-]", "_");
      Path dotPath = dotDir.resolve(safeName + ".dot");
      Path svgPath = svgDir.resolve(safeName + ".svg");

      // Gera conteúdo DOT e salva arquivo
      String dotContent = toDot(ht, safeName);
      Files.write(dotPath, dotContent.getBytes(StandardCharsets.UTF_8));

      // Converte DOT para SVG usando Graphviz
      String dotExe = getDotExecutable();
      renderSvg(dotExe, dotPath, svgPath);

      System.out.println("  [Graphviz] gerado: " + dotPath.toString());
      System.out.println("  [Graphviz] gerado: " + svgPath.toString());
    } catch (IOException e) {
      System.err.println("Falha ao gerar arquivos DOT/SVG: " + e.getMessage());
    }
  }

  /**
   * Obtém o caminho do executável dot do Graphviz.
   */
  private static String getDotExecutable() {
    String envPath = System.getenv("GRAPHVIZ_DOT_PATH");
    if (envPath != null && !envPath.trim().isEmpty()) {
      return envPath;
    }
    return "C:\\Program Files (x86)\\Graphviz\\bin\\dot.exe";
  }

  /**
   * Executa o Graphviz para converter arquivo DOT em SVG.
   * @throws IOException se o processo falhar
   */
  private static void renderSvg(String dotExe, Path dotPath, Path svgPath) throws IOException {
    ProcessBuilder pb = new ProcessBuilder(dotExe, "-Tsvg", "-o", svgPath.toString(), dotPath.toString());
    pb.redirectErrorStream(true);
    Process process = pb.start();
    try {
      int exit = process.waitFor();
      if (exit != 0) {
        // Captura saída de erro para diagnóstico
        java.io.InputStream is = process.getInputStream();
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) != -1) {
          baos.write(buffer, 0, length);
        }
        String errorOutput = baos.toString("UTF-8");
        throw new IOException("Graphviz dot retornou codigo " + exit + ". Output: " + errorOutput);
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("Processo Graphviz interrompido", e);
    }
  }

  /**
   * Gera o conteúdo DOT representando a estrutura da hash table.
   * Cria nós para o diretório e buckets, com arestas mostrando as conexões.
   */
  private static String toDot(Hashtable<?, ?> ht, String graphName) {
    StringBuilder sb = new StringBuilder();
    sb.append("digraph \"").append(graphName).append("\" {\n");
    sb.append("  rankdir=LR;\n");
    sb.append("  node [shape=box, fontname=Arial];\n");

    // Nó do diretório: mostra índices e buckets apontados
    sb.append("  dir [label=\"Directory\\n(globalDepth=").append(ht.getGlobalDepth()).append(")\\n");
    Bucket<?, ?>[] directory = ht.getDirectory();
    for (int i = 0; i < directory.length; i++) {
      sb.append("[").append(i).append("] -> bucket").append(System.identityHashCode(directory[i])).append("\\n");
    }
    sb.append("\"];\n");

    // Nós dos buckets: mostra profundidade local, contagem e entradas
    Map<Bucket<?, ?>, String> bucketIds = new IdentityHashMap<>();
    int bucketCounter = 0;
    // Mapeia buckets únicos para IDs simples (evita duplicatas)
    for (Bucket<?, ?> bucket : directory) {
      if (!bucketIds.containsKey(bucket)) {
        bucketIds.put(bucket, "bucket" + bucketCounter++);
      }
    }

    for (Map.Entry<Bucket<?, ?>, String> entry : bucketIds.entrySet()) {
      Bucket<?, ?> bucket = entry.getKey();
      String bucketId = entry.getValue();
      sb.append("  ").append(bucketId).append(" [label=\"").append(bucketId).append("\\n");
      sb.append("localDepth=").append(bucket.getLocalDepth()).append("\\n");
      sb.append("count=").append(bucket.getCount()).append("/").append(ht.getBucketCapacity()).append("\\n");
      // Lista todas as entradas chave=valor
      for (int i = 0; i < bucket.getCount(); i++) {
        Entry<?, ?> e = bucket.getEntry(i);
        if (e != null) {
          sb.append(e.getKey()).append(" = ").append(e.getValue()).append("\\n");
        }
      }
      sb.append("\"];\n");
    }

    // Arestas do diretório para buckets, rotuladas com índices
    for (int i = 0; i < directory.length; i++) {
      Bucket<?, ?> bucket = directory[i];
      String bucketId = bucketIds.get(bucket);
      sb.append("  dir -> ").append(bucketId).append(" [label=\"").append(i).append("\"];\n");
    }

    sb.append("}\n");
    return sb.toString();
  }
}
