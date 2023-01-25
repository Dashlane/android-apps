package com.dashlane.sharing.service.response;

import com.dashlane.network.BaseNetworkResponse;
import com.google.gson.annotations.SerializedName;

import java.util.Map;



public class FindUsersResponse extends BaseNetworkResponse<Map<String, FindUsersResponse.User>> {
    public static class User {
        @SerializedName("login")
        private String mLogin;
        @SerializedName("publicKey")
        private String mPublicKey;

        public String getLogin() {
            return mLogin;
        }

        public String getPublicKey() {
            return mPublicKey;
        }
    }
}
