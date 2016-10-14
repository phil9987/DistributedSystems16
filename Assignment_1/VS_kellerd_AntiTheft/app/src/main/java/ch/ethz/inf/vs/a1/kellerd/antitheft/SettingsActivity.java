package ch.ethz.inf.vs.a1.kellerd.antitheft;


import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Toast;

/**
 * Created by simon on 04.10.16.
 */

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstances) {
        super.onCreate(savedInstances);
        //Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
        //Don't let the user change the settings when the service is running
        if (getIntent().getBooleanExtra("service running", false)) {
            Context context = getApplicationContext();
            CharSequence text = "AntiTheft service is running, changes can't be applied!";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            //Return directly to main activity
            finish();
            return;
        }

    }
}
