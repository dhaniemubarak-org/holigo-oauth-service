package id.holigo.services.common.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAuthenticationDto implements Serializable {

    static final long serialVersionUID = -5815566940065181210L;

    private Long id;

    private String phoneNumber;

    private AccountStatusEnum accountStatusEnum;

    private Collection<String> authorities;

    private String type;

    private String oneTimePassword;

    private Boolean accountNonExpired;

    private Boolean accountNonLocked;

    private Boolean credentialsNonExpired;

    private Boolean enabled;

    private UserGroupEnum userGroup;
}
