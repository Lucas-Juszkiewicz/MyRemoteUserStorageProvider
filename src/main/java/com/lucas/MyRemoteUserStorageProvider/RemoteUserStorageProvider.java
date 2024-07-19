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
        String email = storageId.getExternalId();
        return getUserByUsername(realm, email);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String nick) {
        UserModel returnValue = null;
        User user = userService.getUserByUserNick(nick);

        if(user!=null){
            returnValue = new UserAdapter(session, realm, model, user);
        }

        return returnValue;
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        return getUserByUsername(realm, email);
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

        if(verifyPasswordResponse == null){
            return false;
        }
        return verifyPasswordResponse.getResult();
    }
}