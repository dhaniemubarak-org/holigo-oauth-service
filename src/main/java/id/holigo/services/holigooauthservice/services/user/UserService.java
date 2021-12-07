package id.holigo.services.holigooauthservice.services.user;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Service;
import id.holigo.services.common.model.UserAuthenticationDto;
import id.holigo.services.holigooauthservice.config.JmsConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final JmsTemplate jmsTemplate;

    private final ObjectMapper objectMapper;

    public UserAuthenticationDto getUser(UserAuthenticationDto userAuthenticationDto)
            throws JMSException, JsonMappingException, JsonProcessingException {
        Message received = jmsTemplate.sendAndReceive(JmsConfig.OAUTH_USER_DATA_QUEUE, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                Message userAuthenticationMessage = null;
                try {
                    log.info("try to create Text message");
                    userAuthenticationMessage = session
                            .createTextMessage(objectMapper.writeValueAsString(userAuthenticationDto));
                    userAuthenticationMessage.setStringProperty("_type",
                            "id.holigo.services.common.model.UserAuthenticationDto");
                } catch (JsonProcessingException e) {
                    throw new JMSException(e.getMessage());
                }
                return userAuthenticationMessage;
            }
        });
        UserAuthenticationDto result = objectMapper.readValue(received.getBody(String.class),
                UserAuthenticationDto.class);
        return result;
    }
}