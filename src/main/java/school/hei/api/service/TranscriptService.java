package school.hei.api.service;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.api.endpoint.event.EventProducer;
import school.hei.api.endpoint.event.model.TranscriptRequested;

@Service
@AllArgsConstructor
public class TranscriptService {

  private final EventProducer<TranscriptRequested> eventProducer;

  public void requestTranscript(String studentId, Integer year) {
    eventProducer.accept(
        List.of(TranscriptRequested.builder().studentId(studentId).year(year).build()));
  }
}
