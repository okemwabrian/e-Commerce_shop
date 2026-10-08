package com.example.shop.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class GoogleAuthService {
    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(@Value("${app.google.client-id}") String clientId) {
        this.verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(List.of(clientId.trim()))
                .build();
    }

    public GoogleIdToken.Payload verify(String idToken) {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new BadCredentialsException("Invalid Google token");
            }
            GoogleIdToken.Payload payload = token.getPayload();
            if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
                throw new BadCredentialsException("Google email is not verified");
            }
            return payload;
        } catch (GeneralSecurityException | IOException exception) {
            throw new BadCredentialsException("Could not verify the Google token", exception);
        }
    }
}
