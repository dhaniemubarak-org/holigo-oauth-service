package id.holigo.services.holigooauthservice.services.user;

import javax.jms.JMSException;
import javax.jms.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.config.JmsConfig;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper;

    public UserAuthenticationDto getUser(UserAuthenticationDto userAuthenticationDto)
            throws JMSException, JsonProcessingException {
        Message received = jmsTemplate.sendAndReceive(JmsConfig.OAUTH_USER_DATA_QUEUE, session -> {
            Message userAuthenticationMessage;
            try {
                userAuthenticationMessage = session
                        .createTextMessage(objectMapper.writeValueAsString(userAuthenticationDto));
                userAuthenticationMessage.setStringProperty("_type",
                        "id.holigo.services.common.model.UserAuthenticationDto");
            } catch (JsonProcessingException e) {
                throw new JMSException(e.getMessage());
            }
            return userAuthenticationMessage;
        });
        assert received != null;
        return objectMapper.readValue(received.getBody(String.class),
                UserAuthenticationDto.class);
    }

    public void resetOtp(UserAuthenticationDto userAuthenticationDto) {
        jmsTemplate.convertAndSend(JmsConfig.OAUTH_RESET_OTP_QUEUE, userAuthenticationDto);
    }
}