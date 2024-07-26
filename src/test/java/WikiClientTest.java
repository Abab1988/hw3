import lombok.extern.log4j.Log4j2;
import org.example.MultiThreadCrawler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WikiClientTest {

    @Test
    public void mustThrowTimeoutException() {
        MultiThreadCrawler crawler = new MultiThreadCrawler(50);
        Assertions.assertThrows(
                TimeoutException.class,
                () -> crawler.main("Java_(programming_language)", "Cat", 5, TimeUnit.SECONDS,1000));
    }

    @Test
    public void mustThrowExecutionException() {
        MultiThreadCrawler crawler = new MultiThreadCrawler(100);
        Assertions.assertThrows(
                ExecutionException.class,
                () -> crawler.main("Java_(programming_language)", "Metallica", 10, TimeUnit.MINUTES,1));
    }

    @Test
    public void mustReturnStringWhichContainsFourWords() throws TimeoutException, ExecutionException {
        MultiThreadCrawler crawler = new MultiThreadCrawler(100);
        assertEquals(
                4,
                crawler.main("Java_(programming_language)", "Cat", 5, TimeUnit.MINUTES, 100).split("->").length
        );
    }


}