package home.jwt.springApp.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;
import static home.jwt.springApp.constant.SecurityConstant.*;
import static java.util.Arrays.stream;

import com.auth0.jwt.exceptions.JWTVerificationException;
import home.jwt.springApp.domain.UserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
@Component
public class JWTTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    //--------------------------------------------------------------------------------------------------
    // GENERATE THE TOKEN
    public String generateJwtToken(UserPrincipal userPrincipal){
        String[] claims = getClaimsFromUser(userPrincipal);
        return JWT
                .create()
                .withIssuer(GET_ARRAYS_LLC)
                .withAudience(GET_ARRAYS_ADMINISTRATION)
                .withIssuedAt(new Date()).withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC512(secret.getBytes()));
    }

    private String[] getClaimsFromUser(UserPrincipal user){
        List<String> authorities = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : user.getAuthorities()){
            authorities
                    .add(grantedAuthority
                            .getAuthority());
        }
        return authorities.toArray(new String[0]);
    }
    //------------------------------------------------------------------------------------------

    // GET THE TOKEN
    //--------------------------------------------------------------------------
    public List<GrantedAuthority> getAuthorities(String token){
        String[] claims =  getClaimFromToken(token);
        return
                stream(claims)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
    }

    private String[] getClaimFromToken(String token) {
        JWTVerifier verifier = getJWTVerifier();
        return
                verifier
                        .verify(token)
                        .getClaim(AUTHORITIES)
                        .asArray(String.class);
    }
    //-----------------------------------------------------------------------------

    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities, HttpServletRequest request){
        UsernamePasswordAuthenticationToken usernamePasswordAuthToken
                =  new UsernamePasswordAuthenticationToken(username, null, authorities);
        usernamePasswordAuthToken
                        .setDetails(new WebAuthenticationDetailsSource()
                        .buildDetails(request));
        return usernamePasswordAuthToken;
    }

    //----------------------------------------------------------------------------//
    public boolean isTokenValid(String username, String token){
        JWTVerifier verifier = getJWTVerifier();
        return StringUtils
                .isNotEmpty(username) && isTokenExpired(verifier, token);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier
                                .verify(token)
                                .getExpiresAt();

        return expiration.before(new Date());
    }
    //---------------------------------------------------------------------------//

    public String getSubject(String token){
        JWTVerifier verifier = getJWTVerifier();
        return verifier
                    .verify(token)
                    .getSubject();
    }
    //

    private JWTVerifier getJWTVerifier() {
        JWTVerifier verifier;
        try {
            Algorithm algorithm = HMAC512(secret);
            verifier = JWT
                    .require(algorithm)
                    .withIssuer(GET_ARRAYS_LLC)
                    .build();
        } catch (JWTVerificationException exception) {
            throw new
                    JWTVerificationException
                           (TOKEN_CANNOT_BE_VERIFIED);
        }
        return verifier;
    }



}
