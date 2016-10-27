package ch.ethz.inf.vs.a3.kellerd.chat;

import android.app.Activity;
import android.os.Bundle;

public class SettingsActivity extends Activity {
    public static final String KEY_PREF_SERVER_ADDRESS = "server_address_preference";
    public static final String KEY_PREF_SERVER_PORT = "server_port_preference";

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Display the fragment as the main content.
            getFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }

    }


