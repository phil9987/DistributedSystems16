package ch.ethz.inf.vs.a1.kellerd.antitheft;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    private boolean serviceActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button settingsbtn = (Button) findViewById(R.id.settingsbtn);
        View.OnClickListener settingslstnr = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                intent.putExtra("service running", serviceActive);
                startActivity(intent);
            }
        };
        settingsbtn.setOnClickListener(settingslstnr);

        Intent intent = new Intent(this, AntiTheftService.class);
        setToggleButton(intent);

    }


    /**
     * Initialize the toggle button and register a listener to start/stop the AntiTheft service
     * @param intent Intent to start action on clickEvent on Toggle Button
     */
    private void setToggleButton(final Intent intent){
        ToggleButton lockToggle = (ToggleButton) findViewById(R.id.toggleLock);
        lockToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    startService(intent);
                    serviceActive = true;
                } else {
                    stopService(intent);
                    serviceActive = false;
                }
            }});}
}
