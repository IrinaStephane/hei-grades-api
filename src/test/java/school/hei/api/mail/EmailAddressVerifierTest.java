package school.hei.api.mail;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.mail.internet.InternetAddress;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.VerifyEmailIdentityRequest;

class EmailAddressVerifierTest {

  @Test
  void verifies_email_identity() throws Exception {
    var emailConf = mock(EmailConf.class);
    var sesClient = mock(SesClient.class);
    when(emailConf.getSesClient()).thenReturn(sesClient);
    var subject = new EmailAddressVerifier(emailConf);

    subject.accept(new InternetAddress("user@hei.school"));

    verify(sesClient)
        .verifyEmailIdentity(
            org.mockito.ArgumentMatchers.argThat(
                (VerifyEmailIdentityRequest request) ->
                    "user@hei.school".equals(request.emailAddress())));
  }
}
