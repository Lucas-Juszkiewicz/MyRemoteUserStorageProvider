package com.lucas.MyRemoteUserStorageProvider;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;

public class RemoteUserStorageProvider implements UserStorageProvider, UserLookupProvider, CredentialInputValidator {

    private KeycloakSession session;
    private ComponentModel model;

    private UsersApiLegacyService userService;


    public RemoteUserStorageProvider(KeycloakSession session, ComponentModel model, UsersApiLegacyService userService) {
        this.session = session;
        this.model = model;
        this.userService = userService;
    }

    @Override
    public void close() {

    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(realm, username);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        UserModel returnValue = null;
        User user = userService.getUserByUserName(username);

        if(user!=null){
            returnValue = new UserAdapter(session, realm, model, user);
        }
        System.out.println("User form getUserByUsername in RUSP: " + user);
        System.out.println("UserAdapter form getUserByUsername in RUSP: " + returnValue);
        return returnValue;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String username) {
        return getUserByUsername(realm, username);
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    @Override
    public boolean isValid(RealmModel realmModel, UserModel user, CredentialInput credentialInput) {
        VerifyPasswordResponse verifyPasswordResponse = userService.verifyUserPassword(user.getUsername(), credentialInput.getChallengeResponse());
        System.out.println("Pass response: " + verifyPasswordResponse);

        if(verifyPasswordResponse == null){
            return false;
        }
        return verifyPasswordResponse.getResult();
    }
}