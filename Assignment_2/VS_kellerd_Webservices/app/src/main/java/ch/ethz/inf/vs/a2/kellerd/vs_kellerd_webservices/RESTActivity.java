package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;
import ch.ethz.inf.vs.a2.sensor.JsonSensor;
import ch.ethz.inf.vs.a2.sensor.RawHttpSensor;
import ch.ethz.inf.vs.a2.sensor.TextSensor;

public class RESTActivity extends AppCompatActivity
        implements ch.ethz.inf.vs.a2.sensor.SensorListener {

    private TextView mTemperatureTextView;
    private TextView mDebugTextView;

    private RawHttpSensor mRawHttpSensor;
    private TextSensor mTextSensor;
    private JsonSensor mJsonSensor;
    private List<AbstractSensor> mSensors;

    private String mSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);

        setTitle(R.string.rest_activity_title);

        mTemperatureTextView = (TextView) findViewById(R.id.temperature);
        mDebugTextView = (TextView) findViewById(R.id.debug_text);

        mDebugTextView.setText("");

        mSource = "";

        mRawHttpSensor = new RawHttpSensor();
        mTextSensor = new TextSensor();
        mJsonSensor = new JsonSensor();
        mSensors = new ArrayList<>(Arrays.asList(mRawHttpSensor, mTextSensor, mJsonSensor));
    }

    @Override
    protected void onStart() {
        super.onStart();

        for (AbstractSensor sensor: mSensors) {
            sensor.registerListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSource = "Raw";
        mRawHttpSensor.getTemperature();
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (AbstractSensor sensor: mSensors) {
            sensor.unregisterListener(this);
        }
    }

    /*
     * Button click handler
     */

    public void onRawHttpSensorButtonClick(View view) {
        mTemperatureTextView.setText(R.string.rest_loading_temperature);
        mSource = "Raw";
        mRawHttpSensor.getTemperature();
    }

    public void onTextSensorButtonClick(View view) {
        mTemperatureTextView.setText(R.string.rest_loading_temperature);
        mSource = "Text";
        mTextSensor.getTemperature();
    }

    public void onJsonSensorButtonClick(View view) {
        mTemperatureTextView.setText(R.string.rest_loading_temperature);
        mSource = "Json";
        mJsonSensor.getTemperature();
    }

    /*
     * Implement ch.ethz.inf.vs.a2.sensor.SensorListener
     */

    @Override
    public void onReceiveSensorValue(double value) {
        mTemperatureTextView.setText("Temperature: " + value + "˚C" + " (" + mSource + ")");
    }

    @Override
    public void onReceiveMessage(String message) {
        mDebugTextView.setText(message);
    }
}
