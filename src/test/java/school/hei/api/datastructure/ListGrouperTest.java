package school.hei.api.datastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class ListGrouperTest {

  private final ListGrouper<Integer> subject = new ListGrouper<>();

  @Test
  void groups_into_pages_of_given_size() {
    var numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

    var grouped = subject.apply(numbers, 3);

    assertEquals(3, grouped.size());
    assertEquals(List.of(1, 2, 3), grouped.get(0));
    assertEquals(List.of(4, 5, 6), grouped.get(1));
    assertEquals(List.of(7, 8), grouped.get(2));
  }

  @Test
  void group_size_bigger_than_list_returns_single_group() {
    var grouped = subject.apply(List.of(1, 2), 10);

    assertEquals(1, grouped.size());
    assertEquals(List.of(1, 2), grouped.get(0));
  }

  @Test
  void empty_list_returns_no_group() {
    assertTrue(subject.apply(List.of(), 3).isEmpty());
  }

  @Test
  void group_size_one_groups_every_element() {
    var grouped = subject.apply(List.of(1, 2, 3), 1);

    assertEquals(3, grouped.size());
    assertEquals(List.of(1), grouped.get(0));
    assertEquals(List.of(2), grouped.get(1));
    assertEquals(List.of(3), grouped.get(2));
  }
}
