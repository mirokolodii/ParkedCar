package com.unagit.parkedcar.views;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.unagit.parkedcar.R;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

public class PreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
        setupOnClickListeners();
    }

    private void setupOnClickListeners() {
        // Rate app
        findPreference(getString(R.string.pref_key_rate)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                launchMarket();
                return true;
            }
        });
    }

    private void launchMarket() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + getContext().getPackageName())));

        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(
                    getContext(),
                    getString(R.string.pref_rate_not_in_market_error),
                    Toast.LENGTH_LONG).show();

        }
    }


}
