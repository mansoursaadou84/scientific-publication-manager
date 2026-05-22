package com.centre.recherche.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service de gestion des tokens JWT.
 * Genere, valide et extrait les informations des tokens JWT en utilisant
 * l'algorithme HMAC-SHA256 avec une cle secrete configurable.
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret:ChangeMeToAVeryLongSecureKeyForJwtTokenGeneration2024!}")
    private String secret;

    @Value("${app.jwt.expiration:86400000}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Genere un token JWT pour l'utilisateur donne.
     *
     * @param userDetails les details de l'utilisateur pour lequel le token est genere
     * @return le token JWT signe
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return buildToken(claims, userDetails, expirationMs);
    }

    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur (sujet) du token JWT.
     *
     * @param token le token JWT
     * @return le nom d'utilisateur extrait du token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait une revendication (claim) du token JWT a l'aide d'un resolveur.
     *
     * @param token   le token JWT
     * @param resolver la fonction permettant d'extraire la revendication
     * @param <T>      le type de la revendication
     * @return la valeur de la revendication extraite
     */
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Verifie la validite d'un token JWT en comparant le nom d'utilisateur
     * et en verifiant que le token n'est pas expire.
     *
     * @param token       le token JWT a valider
     * @param userDetails les details de l'utilisateur attendu
     * @return true si le token est valide, false sinon
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}