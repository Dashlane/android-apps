package com.dashlane.authentication.accountsmanager;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;

import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;


public class DashlaneAuthenticator extends AbstractAccountAuthenticator {

    public DashlaneAuthenticator() {
        super(null);
    }

    public DashlaneAuthenticator(Context context) {
        super(context);
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType,
                             String[] requiredFeatures, Bundle options) throws NetworkErrorException {
        final Bundle bundle = new Bundle();
        return bundle;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String authTokenType, Bundle
            options) throws NetworkErrorException {
        final Bundle bundle = new Bundle();
        return bundle;
    }


    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return authTokenType + " (Label)";
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features) throws
                                                                                                         NetworkErrorException {
        final Bundle result = new Bundle();
        result.putBoolean(KEY_BOOLEAN_RESULT, false);
        return result;
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse response, String accountType) {
        return null;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) throws
                                                                                                             NetworkErrorException {
        return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String authTokenType,
                                    Bundle options) throws NetworkErrorException {
        return null;
    }
}