package com.dashlane.authentication.accountsmanager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import com.dashlane.util.StringUtils;
import androidx.annotation.Nullable;
import static kotlin.text.StringsKt.isBlank;

public class AccountsManager {

    private final Context mContext;

    private AccountManager mAccountManager;

    

    private static final String ACCOUNT_TYPE = "com.dashlane";

    

    private static final String SERVER_KEY = "serverkey";

    private static final String PASSWORD_TYPE = "passwordtype";
    private static final String PASSWORD_TYPE_LOCAL_KEY = "lk";

    public AccountsManager(Context context) {
        mContext = context;
    }

    private AccountManager getAccountManager() {
        if (mAccountManager == null) {
            mAccountManager = AccountManager.get(mContext);
        }
        return mAccountManager;
    }

    @Nullable
    public AccountsManagerPassword getPassword(String username) {
        try {
            AccountManager accountManager = getAccountManager();
            if (accountManager.getAccountsByType(ACCOUNT_TYPE).length == 0) return null;
            if (isBlank(username)) return null;
            Account acct = new Account(username, ACCOUNT_TYPE);

            String data = accountManager.getPassword(acct);

            if (StringUtils.isNullOrEmpty(data)) return null;

            boolean isLocalKey = PASSWORD_TYPE_LOCAL_KEY.equals(accountManager.getUserData(acct, PASSWORD_TYPE));
            String serverKey = accountManager.getUserData(acct, SERVER_KEY);

            return new AccountsManagerPassword(data, isLocalKey, serverKey);
        } catch (SecurityException ex) {
            return null;
        }
    }

    

    public void clearAllAccounts() {
        AccountManager accountManager = getAccountManager();
        Account[] accounts = null;
        try {
            accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        } catch (SecurityException ex) {
            
        }

        if (accounts == null) return;
        for (Account account : accounts) {
            accountManager.removeAccountExplicitly(account);
        }
    }
}
