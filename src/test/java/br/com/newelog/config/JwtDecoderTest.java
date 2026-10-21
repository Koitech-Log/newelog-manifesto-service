package br.com.newelog.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

/** Garante que o serviço só aceita tokens do auth-service, assinados com o segredo compartilhado. */
class JwtDecoderTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres-ok";
    private static final String OUTRO_SEGREDO = "outro-segredo-totalmente-diferente-32-chars";

    private final JwtDecoder decoder = new SecurityConfig().jwtDecoder(SEGREDO);

    @Test
    void aceitaTokenValidoDoAuthService() throws Exception {
        Jwt jwt = decoder.decode(token(SEGREDO, "newelog-auth-service", Instant.now().plusSeconds(300)));

        assertThat(jwt.getClaimAsString("perfil")).isEqualTo("GESTOR");
    }

    @Test
    void rejeitaAssinaturaDeOutroSegredo() throws Exception {
        String token = token(OUTRO_SEGREDO, "newelog-auth-service", Instant.now().plusSeconds(300));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaTokenExpirado() throws Exception {
        String token = token(SEGREDO, "newelog-auth-service", Instant.now().minusSeconds(3600));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaEmissorDiferente() throws Exception {
        String token = token(SEGREDO, "outro-emissor", Instant.now().plusSeconds(300));

        assertThatThrownBy(() -> decoder.decode(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void recusaSegredoCurtoNaInicializacao() {
        assertThatThrownBy(() -> new SecurityConfig().jwtDecoder("curto"))
                .isInstanceOf(IllegalStateException.class);
    }

    private static String token(String segredo, String emissor, Instant expira) throws Exception {
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), new JWTClaimsSet.Builder()
                .issuer(emissor)
                .subject("1")
                .claim("perfil", "GESTOR")
                .expirationTime(Date.from(expira))
                .build());
        jwt.sign(new MACSigner(segredo.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }
}
