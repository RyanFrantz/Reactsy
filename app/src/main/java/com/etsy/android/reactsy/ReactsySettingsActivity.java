package com.etsy.android.reactsy;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by mhorowitz on 8/21/14.
 */
public class ReactsySettingsActivity extends PreferenceActivity {
    public static final String PREFERENCE_LDAP_ID_KEY = "etsy_ldap_id";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}
