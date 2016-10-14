package ch.ethz.inf.vs.a1.kellerd.antitheft;


import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by simon on 04.10.16.
 */

public class SettingsFragment extends PreferenceFragment {
    /**
     *
     * @param savedInstanceState
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);


    }




}
