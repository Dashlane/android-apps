package com.dashlane.notification.model;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.logger.ExceptionLog;
import com.dashlane.navigation.NavigationConstants;
import com.dashlane.network.BaseNetworkResponse;
import com.dashlane.network.webservices.authentication.GetTokenService;
import com.dashlane.notification.FcmMessage;
import com.dashlane.notification.NotificationIntentUtilsKt;
import com.dashlane.preference.ConstantsPrefs;
import com.dashlane.preference.UserPreferencesManager;
import com.dashlane.security.DashlaneIntent;
import com.dashlane.session.Session;
import com.dashlane.ui.activities.SplashScreenActivity;
import com.dashlane.useractivity.log.install.InstallLogRepository;
import com.dashlane.util.Constants;
import com.dashlane.util.StringUtils;
import com.dashlane.util.notification.DashlaneNotificationBuilder;
import com.dashlane.util.notification.NotificationHelper;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class TokenNotificationHandler extends AbstractNotificationHandler {

    private static final int NOTIFICATION_ID = TokenNotificationHandler.class.hashCode();
    private static final String JSON_KEY_TOKEN = "token";
    private final TokenNotificationLogger mTokenNotificationLogger;
    private String mToken;

    

    public TokenNotificationHandler(Context context, FcmMessage message, InstallLogRepository installLogRepository) {
        super(context, message);
        mTokenNotificationLogger = new TokenNotificationLogger(installLogRepository);
        parseMessage();
        setNotificationId(NOTIFICATION_ID);
    }

    

    public static String getLastTokenForCurrentUser() {

        String decrypted = SingletonProvider.getTokenJsonProvider().getJson();

        if (StringUtils.isNotSemanticallyNull(decrypted)) {
            try {
                JSONObject json = new JSONObject(decrypted);
                long ttl = json.getLong("ttl");
                Instant now = Instant.now();
                Instant instant = Instant.ofEpochMilli(ttl);

                if (now.isBefore(instant)) {
                    return json.getString(JSON_KEY_TOKEN);
                } else {
                    removeSavedToken();
                }
            } catch (JSONException e) {
                ExceptionLog.v(e);
            }
        }

        return null;
    }

    

    public static void removeSavedToken() {
        SingletonProvider.getTokenJsonProvider().setJson(null);
    }

    

    @Override
    public void handlePushNotification() {
        
        
        if (!isForLastLoggedInUser() || hasAlreadyHandled()) {
            return;
        }
        setUpCancelAlarm(getContext());
        notifyUser(getContext());
    }

    

    public boolean shouldNotify(String username) {
        return hasRecipient() && getRecipientEmail().equals(username) && isAlive();
    }

    

    public String getToken() {
        return mToken;
    }

    

    public boolean needWebserviceCall() {
        return !hasToken();
    }

    

    public void setShown() {
        removeSavedToken();
    }

    

    @Override
    protected void parseMessage() {
        super.parseMessage();
        String gcmData = getFcmMessage().getData();
        try {
            JSONObject jsonFormatedData = new JSONObject(gcmData);

            if (jsonFormatedData.has(JSON_KEY_TOKEN)) {
                mToken = jsonFormatedData.getString(JSON_KEY_TOKEN);
            }
        } catch (JSONException e) {
            ExceptionLog.v(e);
        }
    }

    

    private boolean isAlive() {
        if (hasTTL()) {
            Instant now = Instant.now();
            Instant instant = Instant.ofEpochMilli(getTTL());
            return now.isBefore(instant);
        }
        return false;
    }

    

    private boolean hasToken() {
        return StringUtils.isNotSemanticallyNull(mToken);
    }

    

    private void notifyUser(Context context) {
        Constants.GCM.Token.put(getRecipientEmail(), this);
        Constants.GCM.TokenShouldNotify.put(getRecipientEmail(), true);
        if (needWebserviceCall()) {
            getTokenFromServerIfUserLoggedIn();
        }
        notifyUserWithNotification(context);
        notifyUserWithPopup(context);
    }

    

    private String getNotificationMessage(Context context) {
        if (hasToken()) {
            mTokenNotificationLogger.logShowToken();
            return String.format(context.getString(R.string.gcmtint_token_is), mToken);
        }
        if (hasRecipient() && SingletonProvider.getGlobalPreferencesManager().isMultipleAccountLoadedOnThisDevice()) {
            mTokenNotificationLogger.logShowDialogWithoutToken();
            return String.format(context.getString(R.string.gcmtint_token_for_user), getRecipientEmail());
        } else if (hasRecipient() && !SingletonProvider.getGlobalPreferencesManager()
                                                       .isMultipleAccountLoadedOnThisDevice()) {
            mTokenNotificationLogger.logShowDialogWithoutToken();
            return context.getString(R.string.gcmtint_token_for_unique_user);
        }
        return context.getString(R.string.gcmtint);

    }

    

    private void notifyUserWithNotification(Context context) {
        Intent notificationIntent = DashlaneIntent.newInstance(context, SplashScreenActivity.class);

        
        NotificationIntentUtilsKt.appendNotificationExtras(notificationIntent, getFcmMessage().getCode().name());

        notificationIntent.putExtra(NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION, true);
        notificationIntent.putExtra(NavigationConstants.USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_USER,
                                    getRecipientEmail());
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session != null &&
            session.getUserId().equals(getRecipientEmail())) {
            notificationIntent.putExtra(NavigationConstants
                                                .USER_COMES_FROM_EXTERNAL_PUSH_TOKEN_NOTIFICATION_ALREADY_LOGGED_IN,
                                        true);
        }
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        String message = getNotificationMessage(context);
        Notification note = new DashlaneNotificationBuilder(context)
                .setContentTitleDashlane()
                .setContentText(message, true)
                .setIconDashlane()
                .setContentIntent(pendingIntent)
                .setChannel(NotificationHelper.Channel.TOKEN)
                .setAutoCancel()
                .build();
        SingletonProvider.getFcmHelper().logDisplay(getFcmMessage().getCode().name());
        NotificationManagerCompat.from(context).notify(getNotificationId(), note);
    }

    

    private void notifyUserWithPopup(Context context) {
        LocalBroadcastManager.getInstance(context)
                             .sendBroadcast(DashlaneIntent.newInstance(Constants.BROADCASTS.NEW_TOKEN_BROADCAST));
    }

    

    private void getTokenFromServerIfUserLoggedIn() {
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session == null) {
            
            return;
        }
        String username = session.getUserId();
        if (!username.equals(getRecipientEmail())) {
            return;
        }
        GetTokenService service = SingletonProvider.getComponent().getTokenService();
        service.createCall(username, session.getUki()).enqueue(
                new Callback<BaseNetworkResponse<GetTokenService.Content>>() {
                    @Override
                    public void onResponse(@NonNull Call<BaseNetworkResponse<GetTokenService.Content>> call,
                                           @NonNull Response<BaseNetworkResponse<GetTokenService.Content>> response) {
                        UserPreferencesManager preferencesManager =
                                SingletonProvider.getUserPreferencesManager();
                        if (response.isSuccessful()) {
                            try {
                                JSONObject json = new JSONObject();
                                preferencesManager.putBoolean(ConstantsPrefs.TOKEN_RETRIEVED_ON_PUSH,
                                                              true);
                                json.put("ttl", getTTL());
                                json.put(JSON_KEY_TOKEN, getToken(response));
                                SingletonProvider.getTokenJsonProvider().setJson(json.toString());
                            } catch (JSONException e) {
                                preferencesManager.remove(ConstantsPrefs.TOKEN_RETRIEVED_ON_PUSH);
                            }
                        } else {
                            preferencesManager.remove(ConstantsPrefs.TOKEN_RETRIEVED_ON_PUSH);
                        }

                    }

                    @Override
                    public void onFailure(@NonNull Call<BaseNetworkResponse<GetTokenService.Content>> call,
                                          @NonNull Throwable t) {
                        
                    }
                });
    }

    

    private boolean hasAlreadyHandled() {
        TokenNotificationHandler pushToken = Constants.GCM.Token.get(getRecipientEmail());
        return pushToken != null && getTTL() == pushToken.getTTL();
    }

    @NotNull
    private String getToken(
            @NonNull Response<BaseNetworkResponse<GetTokenService.Content>> response) {
        BaseNetworkResponse<GetTokenService.Content> body = response.body();
        if (body != null) {
            GetTokenService.Content content = body.getContent();
            if (content != null) {
                return content.getToken();
            }
        }
        return "";
    }
}
