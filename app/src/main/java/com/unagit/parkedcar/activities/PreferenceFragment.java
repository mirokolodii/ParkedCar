package com.unagit.parkedcar.activities;

import android.os.Bundle;
import com.unagit.parkedcar.R;

import androidx.preference.PreferenceFragmentCompat;

public class PreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }


}
