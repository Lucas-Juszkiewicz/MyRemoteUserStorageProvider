package com.lucas.MyRemoteUserStorageProvider;

import jakarta.ws.rs.PathParam;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.models.KeycloakSession;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UsersApiLegacyService {

    private static final Logger LOGGER = Logger.getLogger(UsersApiLegacyService.class.getName());
    private final KeycloakSession session;

    public UsersApiLegacyService(KeycloakSession session) {
        this.session = session;
    }

    public User getUserByUserEmail(String email){
        User user = null;

        try {
            user = SimpleHttp.doGet( "http://localhost:8081/users/" + email, session).asJson(User.class);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching user with email '" + email + "' from external service.", e);
            throw new RuntimeException("Error fetching user with email '" + email + "' from external service.", e);
        }

        return user;
    }

    public VerifyPasswordResponse verifyUserPassword(@PathParam("email") String email, String password){
        SimpleHttp simpleHttp = SimpleHttp.doPost("http://localhost:8081/users/" + email + "/verify-password", session);
        VerifyPasswordResponse verifyPasswordResponse = null;
        simpleHttp.param("password", password);
        simpleHttp.header("Content-Type", "application/x-www-form-urlencoded");

        try {
            verifyPasswordResponse = simpleHttp.asJson(VerifyPasswordResponse.class);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "The provided password is incorrect", e);
            throw new RuntimeException("The provided password is incorrect", e);
        }

        return verifyPasswordResponse;
    }
}
