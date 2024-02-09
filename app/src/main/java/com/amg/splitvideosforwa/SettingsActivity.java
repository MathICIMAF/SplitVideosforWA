package com.amg.splitvideosforwa;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/* loaded from: classes.dex */
public class SettingsActivity extends AppCompatActivity {
    @Override // android.preference.PreferenceActivity, android.app.Activity
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.idFrameLayout, new SettingsFragment())
                .commit();
        MainActivity.SETTINGS_ACT = true;
    }
}
