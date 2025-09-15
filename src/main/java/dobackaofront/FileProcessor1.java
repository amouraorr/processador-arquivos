package dobackaofront;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Solução para o problema de processamento de arquivos em paralelo. 10 arquivos de texto são processados em paralelo,
 * cada um em uma thread separada. O resultado é uma lista de linhas processadas, que é thread-safe.
 *
 * Classe responsável por processar arquivos de texto em paralelo.
 * Esta versão aprimorada demonstra boas práticas de concorrência, gerenciamento de recursos e clareza de código.
 */
public class FileProcessor1 {

    // Lista thread-safe para armazenar as linhas processadas.
    private static final List<String> lines = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        // Exemplo: lista de arquivos a serem processados (poderia vir de args, diretório, etc.)
        List<String> filesToProcess = List.of(
                "data.txt",
                "data1.txt",
                "data2.txt",
                "data3.txt",
                "data4.txt",
                "data5.txt",
                "data6.txt",
                "data7.txt",
                "data8.txt",
                "data9.txt",
                "data10.txt"
        );

        // Executor com número fixo de threads.
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            // Para cada arquivo, submete uma tarefa para processá-lo.
            for (String fileName : filesToProcess) {
                executor.submit(() -> processFile(fileName));
            }
        } finally {
            executor.shutdown();
            try {
                // Aguarda todas as tarefas terminarem antes de prosseguir.
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Impressão do resultado após todas as tarefas concluídas.
        System.out.println("Lines processed: " + lines.size());
        // Exemplo: imprimir as primeiras 5 linhas processadas.
        lines.stream().limit(5).forEach(System.out::println);
    }

    /**
     * Processa um arquivo linha a linha, convertendo cada linha para maiúsculas e armazenando na lista compartilhada.
     * @param fileName Nome do arquivo a ser processado.
     */
    private static void processFile(String fileName) {
        System.out.println("Tentando processar: " + fileName);
        File file = new File(fileName);
        if (!file.exists()) {
            System.err.println("Arquivo não encontrado: " + fileName + " (verifique o diretório de execução)");
            return;
        }
        // try-with-resources garante o fechamento do arquivo mesmo em caso de exceção.
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Adiciona a linha processada à lista thread-safe.
                lines.add(line.toUpperCase());
            }
        } catch (IOException e) {
            // Loga o erro, mas não interrompe o processamento dos demais arquivos.
            System.err.println("Erro ao processar o arquivo " + fileName + ": " + e.getMessage());
        }
    }
}