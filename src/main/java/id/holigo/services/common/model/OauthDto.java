package id.holigo.services.common.model;

import java.util.Date;

import lombok.Data;

@Data
public class OauthDto {
    String id;
    String token;
    boolean isValid;
    String subject;
    String[] authorities;
    Date expiredAt;
    String payload;

}
