package com.gallegos.clinicagallegos.service.impl;

import com.gallegos.clinicagallegos.model.Usuario;
import com.gallegos.clinicagallegos.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.nio.charset.StandardCharsets;

@Service
public class JwtServiceImpl implements JwtService {
    @Value("${jwt.secret.key}")
    private String SECRET_KEY;

    @Value("${jwt.expiration.time}")
    private long EXPIRATION_TIME;

    @Override
    public String generarToken(Usuario usuario){
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", usuario.getRol().name());
        return construirToken(claims, usuario.getEmail());
    }

    @Override
    public String extraerUsername(String token){
        return extraerClaim(token, Claims::getSubject);
    }

    @Override
    public boolean esTokenValido(String token, Usuario usuario){
        final String username = extraerUsername(token);
        return (username.equals(usuario.getEmail()) && !esTokenExpirado(token));
    }

    // -------------------- Métodos privados --------------------
    private String construirToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // El "subject" será el email del usuario
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodasLasClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraerTodasLasClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean esTokenExpirado(String token) {
        return extraerClaim(token, Claims::getExpiration).before(new Date());
    }

    private Key getSigningKey(){
        byte[] keyBytes = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
