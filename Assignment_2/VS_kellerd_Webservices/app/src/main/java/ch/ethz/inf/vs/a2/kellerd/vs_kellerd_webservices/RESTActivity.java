package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;
import ch.ethz.inf.vs.a2.sensor.JsonSensor;
import ch.ethz.inf.vs.a2.sensor.RawHttpSensor;
import ch.ethz.inf.vs.a2.sensor.TextSensor;

public class RESTActivity extends AppCompatActivity
        implements ch.ethz.inf.vs.a2.sensor.SensorListener {

    private TextView mTemperatureTextView;
    private TextView mDebugTextView;

    private RawHttpSensor rawHttpSensor;
    private TextSensor textSensor;
    private JsonSensor jsonSensor;
    private List<AbstractSensor> sensors;

    private String mSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);

        setTitle(R.string.rest_activity_title);

        mTemperatureTextView = (TextView) findViewById(R.id.temperature);
        mDebugTextView = (TextView) findViewById(R.id.debug_text);

        mDebugTextView.setText("");

        rawHttpSensor = new RawHttpSensor();
        textSensor = new TextSensor();
        jsonSensor = new JsonSensor();
        sensors = new ArrayList<>(Arrays.asList(rawHttpSensor, textSensor, jsonSensor));
    }

    @Override
    protected void onStart() {
        super.onStart();

        for (AbstractSensor sensor: sensors) {
            sensor.registerListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSource = "Raw";
        rawHttpSensor.getTemperature();
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (AbstractSensor sensor: sensors) {
            sensor.unregisterListener(this);
        }
    }

    /*
     * Button click handler
     */

    public void onRawHttpSensorButtonClick(View view) {
        mTemperatureTextView.setText(R.string.rest_loading_temperature);
        mSource = "Raw";
        rawHttpSensor.getTemperature();
    }

    public void onTextSensorButtonClick(View view) {
        mTemperatureTextView.setText(R.string.rest_loading_temperature);
        mSource = "Text";
        textSensor.getTemperature();
    }

    public void onJsonSensorButtonClick(View view) {
        mTemperatureTextView.setText(R.string.rest_loading_temperature);
        mSource = "Json";
        jsonSensor.getTemperature();
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
