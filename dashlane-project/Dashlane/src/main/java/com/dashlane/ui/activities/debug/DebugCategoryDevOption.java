package com.dashlane.ui.activities.debug;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.Toast;

import com.dashlane.BuildConfig;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.session.Session;
import com.dashlane.storage.userdata.Database;
import com.dashlane.ui.util.DialogHelper;
import com.dashlane.util.ToasterImpl;
import com.dashlane.util.UriUtils;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;



class DebugCategoryDevOption extends AbstractDebugCategory {

    public DebugCategoryDevOption(Activity debugActivity) {
        super(debugActivity);
    }

    @Override
    String getName() {
        return "Dev options";
    }

    @Override
    void addSubItems(PreferenceGroup group) {
        if (!BuildConfig.DEBUG) {
            throw new IllegalAccessError("You cannot be there in production");
        }
        addReadSharedPreference(group);
        addSendDbDecrypt(group);
    }

    private void addSendDbDecrypt(PreferenceGroup group) {
        addPreferenceButton(group, "Send Database Decrypt",
                            preference -> {
                                Activity activity = getDebugActivity();
                                Session session = SingletonProvider.getSessionManager().getSession();
                                if (session == null) return false;
                                Database database = SingletonProvider.getComponent().getUserDatabaseRepository()
                                                                     .getDatabase(session);
                                File cacheDir = new File(activity.getCacheDir(), "file_provider");
                                if (!cacheDir.mkdir() && !cacheDir.exists()) {
                                    return false;
                                }
                                File plainDd = new File(cacheDir, "plaintext.db");
                                
                                plainDd.delete();
                                database.executeRawExecSQL(
                                        "ATTACH DATABASE '" + plainDd.getAbsolutePath() + "' AS plaintext KEY '';");
                                database.executeRawExecSQL(
                                        "SELECT sqlcipher_export('plaintext');");
                                database.executeRawExecSQL(
                                        "DETACH DATABASE plaintext;");

                                Uri path = UriUtils.getOpenFileUri(activity, plainDd);
                                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                emailIntent.setType("vnd.android.cursor.dir/email");
                                emailIntent.putExtra(Intent.EXTRA_STREAM, path);
                                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                activity.startActivity(Intent.createChooser(emailIntent, "Send email..."));
                                return true;
                            });
    }

    private void addReadSharedPreference(PreferenceGroup group) {
        String anonymousDeviceId = SingletonProvider.getComponent().getDeviceInfoRepository().getAnonymousDeviceId();
        
        
        
        String prefKey = "install_receiver_adjust_sent_for_" + anonymousDeviceId;

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(getDebugActivity());
        String referere = prefs.getString(prefKey, null);
        new ToasterImpl(getDebugActivity()).show("Referer: " + referere, Toast.LENGTH_LONG);


        addPreferenceButton(group, "Read all SharedPreferences",
                            preference -> {
                                Activity activity = getDebugActivity();
                                SharedPreferences prefs1 =
                                        PreferenceManager.getDefaultSharedPreferences(activity);
                                Map<String, ?> all = prefs1.getAll();
                                Iterator<String> keyIterator = all.keySet().iterator();
                                StringBuilder sb = new StringBuilder();
                                while (keyIterator.hasNext()) {
                                    String key = keyIterator.next();
                                    Object value = all.get(key);
                                    sb.append("-- '").append(key).append("': ").append(value).append("\n");
                                }

                                new DialogHelper().builder(activity)
                                                  .setTitle("Debug SharedPreferences")
                                                  .setMessage(sb.toString())
                                                  .show();
                                return true;
                            });
    }

}
