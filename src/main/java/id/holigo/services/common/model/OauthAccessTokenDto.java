package id.holigo.services.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OauthAccessTokenDto {

    private String type;

    private Long expiresIn;

    private String accessToken;

    private String refreshToken;
}
