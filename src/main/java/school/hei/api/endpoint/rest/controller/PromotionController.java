package school.hei.api.endpoint.rest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.endpoint.rest.mapper.PromotionMapper;
import school.hei.api.model.dto.PromotionCreation;
import school.hei.api.model.dto.PromotionRest;
import school.hei.api.model.dto.StudentSummaryRest;
import school.hei.api.service.PromotionService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/promotions")
public class PromotionController {

  private final PromotionService promotionService;
  private final PromotionMapper promotionMapper;

  @GetMapping
  public List<PromotionRest> getPromotions() {
    return promotionMapper.toRest(promotionService.getAll());
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public PromotionRest createPromotion(@Valid @RequestBody PromotionCreation creation) {
    return promotionMapper.toRest(promotionService.create(creation));
  }

  @GetMapping("/{id}")
  public PromotionRest getPromotionById(@PathVariable String id) {
    return promotionMapper.toRest(promotionService.getById(id));
  }

  @PutMapping("/{id}")
  public PromotionRest updatePromotion(
      @PathVariable String id, @Valid @RequestBody PromotionCreation creation) {
    return promotionMapper.toRest(promotionService.update(id, creation));
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deletePromotion(@PathVariable String id) {
    promotionService.delete(id);
  }

  @GetMapping("/{id}/students")
  public List<StudentSummaryRest> getPromotionStudents(@PathVariable String id) {
    return promotionService.getStudents(id);
  }

  // GET /{id}/graduates and GET /{id}/graduates/export are NOT
  // implemented here: they require the average/diploma computation
  // owned by Personne B (Grade, GradeHistory) combined with
  // GroupService.getCourseAssignmentsFollowedByStudent(...) from this
  // package. Coordinate on where that logic + controller method lives.
}
