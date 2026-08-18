package school.hei.api.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import school.hei.api.repository.PromotionRepository;

@Controller
@AllArgsConstructor
public class GraduatesPageController {

  private final PromotionRepository promotionRepository;

  @GetMapping("/graduates")
  public String showGraduates(Model model) {
    model.addAttribute("promotions", promotionRepository.findAll());
    return "graduates";
  }
}
