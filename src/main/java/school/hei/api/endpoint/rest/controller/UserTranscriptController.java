package school.hei.api.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.api.service.TranscriptService;

@RestController
@AllArgsConstructor
public class UserTranscriptController {

  private final TranscriptService transcriptService;

  @PostMapping("/users/{id}/transcript")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void requestUserTranscript(
      @PathVariable("id") String userId, @RequestParam(required = false) Integer year) {
    transcriptService.requestTranscript(userId, year);
  }
}
