package com.immortalcrab.as400.webserver;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import java.io.FileInputStream;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.ServletException;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.FilterChain;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

@Component
public class JWTAuthorizationFilter extends OncePerRequestFilter {

    private final String HEADER = "Authorization";
    private final String PREFIX = "Bearer ";
    private final String PUB_KEY_PATH = "/pem/sso_key.pub";

    private boolean findJWTToken(HttpServletRequest request, HttpServletResponse res) {

        String authenticationHeader = request.getHeader(HEADER);
        return !(authenticationHeader == null || !authenticationHeader.startsWith(PREFIX));
    }

    private Claims extractClaims(HttpServletRequest request) {

        try {
            String jwtToken = request.getHeader(HEADER).replace(PREFIX, "");
            return Jwts.parser().setSigningKey(loadPublicKey(PUB_KEY_PATH)).parseClaimsJws(jwtToken).getBody();
        } catch (Exception ex) {
            throw new UnsupportedJwtException(ex.getMessage());
        }
    }

    public <T> T getClaimFromToken(final Claims claims, Function<Claims, T> claimsResolver) {

        return claimsResolver.apply(claims);
    }

    public static PublicKey loadPublicKey(String filename) throws Exception {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate cert = cf.generateCertificate(new FileInputStream(filename));
        return cert.getPublicKey();
    }

    private void setUpSpringAuthentication(Claims claims) {

        /* HACK to heal the evident absent of authorities of the current SSO
           Due to java is not the center of the world and there is not everywhere */
        List<String> authorities = new ArrayList() {
            {
                add("ROLE_USER");
            }
        };

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(claims.getSubject(), null,
                authorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList()));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        try {
            if (findJWTToken(request, response)) {
                Claims claims = extractClaims(request);
                setUpSpringAuthentication(claims);
            } else {
                SecurityContextHolder.clearContext();
            }
            chain.doFilter(request, response);
        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException e) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            ((HttpServletResponse) response).sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        }

    }

}
