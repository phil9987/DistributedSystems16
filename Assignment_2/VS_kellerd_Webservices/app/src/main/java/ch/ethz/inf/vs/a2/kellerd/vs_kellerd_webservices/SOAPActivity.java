package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices.R;
import ch.ethz.inf.vs.a2.sensor.AbstractSensor;
import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.sensor.SoapSensor;
import ch.ethz.inf.vs.a2.sensor.XmlSensor;

public class SOAPActivity extends AppCompatActivity implements SensorListener {

    private TextView mTemperatureTextView;
    private TextView mDebugTextView;
    private AbstractSensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soapclient);

        setTitle(R.string.soap_activity_title);

        mTemperatureTextView = (TextView) findViewById(R.id.soapTemperature);
        mDebugTextView = (TextView) findViewById(R.id.soapDebug);

        mTemperatureTextView.setText("");
        mDebugTextView.setText("");

        sensor = new XmlSensor();
      //  sensor = new SoapSensor();



    }

    @Override
    protected void onStart(){
        super.onStart();
        sensor.registerListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensor.getTemperature();
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensor.unregisterListener(this);
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