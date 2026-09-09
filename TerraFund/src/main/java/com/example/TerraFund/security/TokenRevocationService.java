package com.example.TerraFund.security;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenRevocationService {

    private final Set<String> revokedJtis = ConcurrentHashMap.newKeySet();
    private final Map<String, Set<String>> issuedJtisByUser = new ConcurrentHashMap<>();

    public void recordIssued(String email, String jti) {
        issuedJtisByUser.computeIfAbsent(email, k -> ConcurrentHashMap.newKeySet()).add(jti);
    }

    public void revoke(String jti) {
        if (jti != null) {
            revokedJtis.add(jti);
        }
    }

    public void revokeAllForUser(String email) {
        Set<String> jtis = issuedJtisByUser.remove(email);
        if (jtis != null) {
            revokedJtis.addAll(jtis);
        }
    }

    public boolean isRevoked(String jti) {
        return jti != null && revokedJtis.contains(jti);
    }
}