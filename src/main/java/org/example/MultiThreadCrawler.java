package org.example;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static java.lang.String.join;


public class MultiThreadCrawler {

    private final BlockingQueue<Node> searchQueue = new LinkedBlockingQueue<>();

    private final Set<String> visited = ConcurrentHashMap.newKeySet();

    private final int threads;

    private final ExecutorService executor;

    public MultiThreadCrawler(int threads) {
        this.threads = threads;
        this.executor = Executors.newFixedThreadPool(threads);
    }

    public String main(String from, String target, long timeout, TimeUnit timeUnit, int maxSearchDeep) throws TimeoutException, ExecutionException {
        searchQueue.offer(new Node(from, null, 0));
        visited.add(from.toLowerCase());
        List<Callable<Node>> tasks = new ArrayList<>(threads);
        IntStream.range(0, threads).forEach(
                x -> tasks.add(() -> find(target, maxSearchDeep))
        );
        Node result = null;
        try {
            result = executor.invokeAny(tasks, timeout, timeUnit);
        } catch (TimeoutException e) {
            System.out.println("Превышен допустимый лимит времени поиска");
            executor.shutdown();
            throw e;
        } catch (ExecutionException e) {
            System.out.println("Превышен допустимый лимит глубины поиска");
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return toAnswer(result);
    }

    private Stack<Node> toStack(Node node) {
        Stack<Node> stack = new Stack<>();
        while (node != null) {
            stack.push(node);
            node = node.next;
        }
        return stack;
    }

    private String toAnswer(Node node) {
        List<String> words = new ArrayList<>();
        Stack<Node> st = toStack(node);
        while (!st.empty()) {
            words.add(st.pop().title);
        }
        return String.join("->", words);
    }

    private Node find(String target, int maxSearchDeep) {
        WikiClient client = new WikiClient();
        while (true) {
            try {
                Node curr = searchQueue.take();
                if (curr.level > maxSearchDeep) {
                    // завершаем выполнение, если превышена максимальная глубина поиска
                    throw new MaxSearchDeepException();
                }
                System.out.println(Thread.currentThread().getId() + " Get page: " + curr.title + " level: " + curr.level);
                Set<String> links = client.getByTitle(curr.title);
                for (String link : links) {
                    String currentLink = link.toLowerCase();
                    if (visited.contains(currentLink)) {
                        continue;
                    }
                    visited.add(currentLink);
                    Node subNode = new Node(link, curr, curr.level + 1);
                    if (target.equalsIgnoreCase(currentLink)) {
                        return subNode;
                    }
                    searchQueue.offer(subNode);
                }
            } catch (IOException e) {
                // Ошибка запроса страницы
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @AllArgsConstructor
    private static class Node {
        String title;
        Node next;
        int level;
    }

}
