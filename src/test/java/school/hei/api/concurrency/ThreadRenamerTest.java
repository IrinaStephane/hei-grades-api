package school.hei.api.concurrency;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static school.hei.api.concurrency.ThreadRenamer.getRandomSubThreadNamePrefixFrom;
import static school.hei.api.concurrency.ThreadRenamer.renameFrontalThread;
import static school.hei.api.concurrency.ThreadRenamer.renameThread;
import static school.hei.api.concurrency.ThreadRenamer.renameWorkerThread;

import org.junit.jupiter.api.Test;

class ThreadRenamerTest {

  @Test
  void rename_worker_thread_sets_w_prefix() {
    var thread = new Thread(() -> {}, "worker");
    renameWorkerThread(thread);

    assertTrue(thread.getName().startsWith("w-"));
    assertNotEquals("worker", thread.getName());
  }

  @Test
  void rename_frontal_thread_sets_f_prefix() {
    var thread = new Thread(() -> {}, "frontal");
    renameFrontalThread(thread);

    assertTrue(thread.getName().startsWith("f-"));
    assertNotEquals("frontal", thread.getName());
  }

  @Test
  void rename_thread_sets_given_name() {
    var thread = new Thread(() -> {}, "old-name");
    renameThread(thread, "new-name");

    assertEquals("new-name", thread.getName());
  }

  @Test
  void random_sub_thread_name_prefix_contains_parent_name() {
    var parent = new Thread(() -> {}, "parent-name");
    var prefix = getRandomSubThreadNamePrefixFrom(parent);

    assertTrue(prefix.startsWith("parent-name-"));
    assertNotEquals(prefix, getRandomSubThreadNamePrefixFrom(parent));
  }
}
