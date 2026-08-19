package school.hei.api.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.hei.api.model.GroupFlow;

public interface GroupFlowRepository extends JpaRepository<GroupFlow, String> {

  List<GroupFlow> findByStudentIdOrderByFlowDatetimeAsc(String studentId);

  List<GroupFlow> findByGroupIdOrderByFlowDatetimeAsc(String groupId);

  Optional<GroupFlow> findFirstByStudentIdOrderByFlowDatetimeDesc(String studentId);

  List<GroupFlow> findByStudentIdInOrderByFlowDatetimeDesc(List<String> studentIds);
}
