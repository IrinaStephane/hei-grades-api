package school.hei.api.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.api.endpoint.event.EventProducer;
import school.hei.api.endpoint.event.model.TranscriptRequested;
import school.hei.api.model.exception.NotFoundException;
import school.hei.api.repository.UserRepository;

@Service
@AllArgsConstructor
public class TranscriptService {

  private final UserRepository userRepository;
  private final EventProducer<TranscriptRequested> eventProducer;

  public void requestTranscript(String studentId, Integer year) {
    if (!userRepository.existsById(studentId)) {
      throw new NotFoundException("User " + studentId + " not found");
    }
    eventProducer.accept(
        List.of(TranscriptRequested.builder().studentId(studentId).year(year).build()));
  }
}
