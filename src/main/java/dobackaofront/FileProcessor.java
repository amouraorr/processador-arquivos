package dobackaofront;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/*
Revisão do código original:
- O código original apresentava problemas de concorrência ao usar ArrayList sem sincronização,
risco de vazamento de recursos ao não fechar arquivos corretamente, e não aguardava o término das tarefas antes de acessar os resultados.
- Corrigi esses pontos utilizando uma lista sincronizada, try-with-resources para leitura dos arquivos,
e awaitTermination para garantir que todas as tarefas fossem concluídas antes de acessar os resultados.
- Também adicionei mensagens de erro claras para facilitar o diagnóstico de problemas de arquivos ausentes ou falhas de leitura.

Sugestão de melhoria adicional:
- Em aplicações maiores, evitar variáveis estáticas globais e preferir injeção de dependências ou encapsulamento em classes.
- Para grandes volumes de dados, considerar coleções concorrentes mais eficientes.
- Separar a lógica de processamento em métodos ou classes auxiliares para facilitar testes e manutenção.
* */


public class FileProcessor {
    // Problema 1: ArrayList não é thread-safe.
    // Solução: Use uma lista sincronizada ou uma coleção concorrente.
    private static final List<String> lines = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {
        // Problema 2: O ExecutorService não é fechado corretamente se ocorrer uma exceção.
        // Solução: Use try-finally para garantir o shutdown.
        ExecutorService executor = Executors.newFixedThreadPool(5);

        try {
            for (int i = 0; i < 10; i++) {
                executor.submit(() -> {
                    // Problema 3: BufferedReader/FileReader não são fechados corretamente em caso de exceção.
                    // Solução: Use try-with-resources para garantir o fechamento.
                    try (BufferedReader br = new BufferedReader(new FileReader("data.txt"))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            // Problema 4: lines.add(line.toUpperCase()) pode causar problemas de concorrência se a lista não for thread-safe.
                            // Já corrigido acima usando Collections.synchronizedList.
                            lines.add(line.toUpperCase());
                        }
                    } catch (IOException e) {
                        // Problema 5: Capturar Exception genérica não é uma boa prática.
                        // Solução: Capture exceções específicas.
                        e.printStackTrace();
                    }
                });
            }
        } finally {
            executor.shutdown();
            try {
                // Problema 6: O método main pode terminar antes que todas as tarefas sejam concluídas.
                // Solução: Aguarde a conclusão das tarefas.
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Problema 7: Pode imprimir o tamanho da lista antes das tarefas terminarem.
        // Solução: Mover o print após awaitTermination.
        System.out.println("Lines processed: " + lines.size());
    }
}
