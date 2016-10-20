package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        setTitle(R.string.server_activity_title);
        Button buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        TextView textViewIP = (TextView) findViewById(R.id.textViewIP);

        buttonStartStop.setText(R.string.button_start_server);
        textViewIP.setText("0.0.0.0:8049");

    }
}
