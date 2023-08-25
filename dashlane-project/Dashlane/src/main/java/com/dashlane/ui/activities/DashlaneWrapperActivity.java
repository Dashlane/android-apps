package com.dashlane.ui.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.navigation.Navigator;

import androidx.annotation.Nullable;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashlaneWrapperActivity extends DashlaneActivity {

    public static void startActivity(Activity origin,
            Uri destination,
            @Nullable Bundle extra) {
        Intent intent = newIntent(origin, destination, extra);
        origin.startActivity(intent);
    }

    public static void startActivityForResult(int requestCode,
            Activity origin,
            Uri destination,
            @Nullable Bundle extras) {
        Intent intent = newIntent(origin, destination, extras);
        origin.startActivityForResult(intent, requestCode);
    }

    private static Intent newIntent(Context context,
            Uri destination,
            @Nullable Bundle extras) {
        Intent intent = new Intent(context, DashlaneWrapperActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(destination);
        if (extras != null) {
            intent.putExtras(extras);
        }
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dashlane_wrapper);
        getActionBarUtil().setup();
        Navigator navigator = SingletonProvider.getNavigator();
        navigator.handleDeepLink(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
