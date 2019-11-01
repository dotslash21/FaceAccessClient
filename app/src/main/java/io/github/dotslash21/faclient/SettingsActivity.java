package io.github.dotslash21.faclient;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.MenuItem;;

import android.os.Bundle;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
    }

    public static class MainPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);

            // gallery EditText change listener
            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_host_name)));

            // notification preference change listener
//            bindPreferenceSummaryToValue(findPreference(getString(R.string.key_notifications_new_message_ringtone)));
//
//            // feedback preference click listener
//            Preference myPref = findPreference(getString(R.string.key_send_feedback));
//            myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
//                public boolean onPreferenceClick(Preference preference) {
//                    sendFeedback(getActivity());
//                    return true;
//                }
//            });
        }
    }

    //Function handling the back button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }


    private static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }



    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String stringValue = newValue.toString();
            if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("key_host_name")) {
                    // update the changed gallery name to summary filed
                    preference.setSummary(stringValue);
                }
                else if(preference.getKey().equals("key_port_name")){
                    preference.setSummary(stringValue);
                }
            }
            return true;
        }
    };
}
