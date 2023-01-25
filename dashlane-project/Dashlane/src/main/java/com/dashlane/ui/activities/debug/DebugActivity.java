package com.dashlane.ui.activities.debug;

import android.os.Bundle;

import com.dashlane.R;

import androidx.appcompat.app.AppCompatActivity;
import dagger.hilt.android.AndroidEntryPoint;



@AndroidEntryPoint
public class DebugActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new DebugFragment())
                .commit();
    }
}
