package com.mundialpolla.auth.application;

import com.mundialpolla.auth.config.AuthProperties;
import com.mundialpolla.participants.domain.Participant;
import java.time.Clock;
import java.time.Instant;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final AuthProperties authProperties;
    private final Clock securityClock = Clock.systemUTC();

    public JwtTokenService(JwtEncoder jwtEncoder, AuthProperties authProperties) {
        this.jwtEncoder = jwtEncoder;
        this.authProperties = authProperties;
    }

    public IssuedJwt issueToken(Participant participant) {
        // JWT security time is real UTC time, intentionally independent from the World Cup ApplicationClock.
        Instant issuedAt = Instant.now(securityClock);
        Instant expiresAt = issuedAt.plus(authProperties.jwt().ttl());
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(authProperties.jwt().issuer())
                .subject(participant.getId().toString())
                .audience(java.util.List.of(authProperties.jwt().audience()))
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("participantId", participant.getId().toString())
                .claim("displayName", participant.getDisplayName())
                .build();
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();

        return new IssuedJwt(token, issuedAt, expiresAt);
    }
}
