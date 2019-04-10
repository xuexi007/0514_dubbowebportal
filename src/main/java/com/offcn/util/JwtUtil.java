package com.offcn.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    static final String SECRET = "ThisIsASecret";

    public static String generateToken(String username) {
        HashMap<String, Object> map = new HashMap<>();
        //you can put any data in the map
        map.put("username", username);
        String jwt = Jwts.builder()
                .setClaims(map)
                .setExpiration(new Date(System.currentTimeMillis() + 1800000L))// 3fen
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
        return "Bearer "+jwt; //jwt前面一般都会加Bearer
    }

    public static Map<String,Object> validateToken(String token) {
        try {
            // parse the token.
            Map<String, Object> body = Jwts.parser()
                    .setSigningKey(SECRET)
                    .parseClaimsJws(token.replace("Bearer ",""))
                    .getBody();
            return body;
        }catch (Exception e){
            throw new IllegalStateException("Invalid Token. "+e.getMessage());
        }
    }
}
