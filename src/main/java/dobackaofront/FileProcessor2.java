package dobackaofront;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Solução 2: Utilização de ExecutorService e ConcurrentHashMa para o problema de processamento de arquivos em paralelo.
 *
 * Classe responsável por processar arquivos de texto em paralelo,
 * encapsulando a lista de linhas e gerando um relatório de sucesso/falha.
 */
public class FileProcessor2 {

    // Encapsula as linhas processadas em uma instância, não como static/global.
    private final List<String> lines = Collections.synchronizedList(new ArrayList<>());

    // Relatório de status de processamento de cada arquivo.
    private final Map<String, String> fileStatus = new ConcurrentHashMap<>();

    /**
     * Processa uma lista de arquivos em paralelo.
     * @param fileNames Lista de nomes de arquivos a processar.
     * @param numThreads Número de threads para o pool.
     */
    public void processFiles(List<String> fileNames, int numThreads) {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (String fileName : fileNames) {
            executor.submit(() -> processFile(fileName));
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                System.err.println("Timeout ao aguardar o término das tarefas.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Execução interrompida.");
        }

        printReport();
    }

    /**
     * Processa um único arquivo, adicionando as linhas à lista e atualizando o status.
     */
    private void processFile(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) {
            fileStatus.put(fileName, "Arquivo não encontrado");
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                lines.add(line.toUpperCase());
                count++;
            }
            fileStatus.put(fileName, "Sucesso (" + count + " linhas)");
        } catch (IOException e) {
            fileStatus.put(fileName, "Erro de leitura: " + e.getMessage());
        }
    }

    /**
     * Imprime um relatório final de arquivos processados e as primeiras linhas lidas.
     */
    private void printReport() {
        System.out.println("===== Relatório de Processamento =====");
        fileStatus.forEach((file, status) -> System.out.println(file + ": " + status));
        System.out.println("Total de linhas processadas: " + lines.size());
        System.out.println("Primeiras linhas processadas:");
        lines.stream().limit(5).forEach(System.out::println);
    }

    /**
     * Exemplo de uso.
     */
    public static void main(String[] args) {
        List<String> arquivos = List.of(
                "data1.txt", "data2.txt", "data3.txt", "data4.txt", "data5.txt",
                "data6.txt", "data7.txt", "data8.txt", "data9.txt", "data10.txt"
        );
        FileProcessor2 processor = new FileProcessor2();
        processor.processFiles(arquivos, 4);
    }
}