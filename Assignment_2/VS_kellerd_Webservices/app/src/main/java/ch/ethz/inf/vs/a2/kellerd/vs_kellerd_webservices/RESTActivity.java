package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class RESTActivity extends AppCompatActivity
        implements ch.ethz.inf.vs.a2.sensor.SensorListener {

    private TextView mTemperatureTextView;
    private TextView mDebugTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);

        setTitle(R.string.rest_activity_title);

        mTemperatureTextView = (TextView) findViewById(R.id.temperature);
        mDebugTextView = (TextView) findViewById(R.id.debug_text);

        mTemperatureTextView.setText("");
        mDebugTextView.setText("");
    }

    /*
     * Implement ch.ethz.inf.vs.a2.sensor.SensorListener
     */
    
    @Override
    public void onReceiveSensorValue(double value) {
        mTemperatureTextView.setText("Temperature: " + value + "˚C");
    }

    @Override
    public void onReceiveMessage(String message) {
        mDebugTextView.setText(message);
    }
}
