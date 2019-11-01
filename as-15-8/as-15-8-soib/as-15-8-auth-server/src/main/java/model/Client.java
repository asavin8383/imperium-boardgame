package model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Entity
@Table(schema = "security", name = "clients")
@Data
@NoArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    private String clientId;
    private String clientSecret;
    private String resourceIds;
    private boolean secretRequired;
    private boolean scoped;
    private String scopes;
    private String authorizedGrantTypes;
    private String registeredRedirectUri;
    private String authorities;
    private Integer accessTokenValiditySeconds;
    private Integer refreshTokenValiditySeconds;
    private boolean autoApprove;
    private String additionalInformation;
}
