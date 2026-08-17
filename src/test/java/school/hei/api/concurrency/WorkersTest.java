package school.hei.api.concurrency;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;

class WorkersTest {

  private final Workers<Integer> subject = new Workers<>();

  @Test
  void applies_callables_and_returns_results() {
    var results = subject.apply(List.of(() -> 1, () -> 2, () -> 3));

    assertEquals(List.of(1, 2, 3), results);
  }

  @Test
  void empty_list_returns_empty_results() {
    assertTrue(subject.apply(List.of()).isEmpty());
  }

  @Test
  void runs_callables_concurrently() throws InterruptedException {
    var startLatch = new CountDownLatch(1);
    var ready = new CountDownLatch(2);
    Callable<Integer> waitThenReturn = () -> {
      ready.countDown();
      startLatch.await(5, SECONDS);
      return 42;
    };

    var futureResults =
        List.of(waitThenReturn, waitThenReturn);

    Thread executor = new Thread(() -> subject.apply(futureResults));
    executor.start();
    assertTrue(ready.await(5, SECONDS), "both callables should start concurrently");
    startLatch.countDown();
    executor.join(5_000);

    assertTrue(!executor.isAlive(), "executor should terminate");
  }

  @Test
  void propagates_callable_exception() {
    var callables =
        List.<Callable<Integer>>of(
            () -> {
              throw new IllegalStateException("boom");
            });

    var exception = assertThrows(RuntimeException.class, () -> subject.apply(callables));
    assertTrue(exception.getCause() instanceof ExecutionException);
    assertTrue(exception.getCause().getCause() instanceof IllegalStateException);
  }
}
