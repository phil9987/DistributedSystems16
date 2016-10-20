package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;
import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.sensor.SoapSensor;
import ch.ethz.inf.vs.a2.sensor.XmlSensor;

public class SOAPActivity extends AppCompatActivity implements SensorListener {

    private TextView mTemperatureTextView;
    private TextView mDebugTextView;
    private AbstractSensor xmlSensor;
    private AbstractSensor soapSensor;
    private AbstractSensor sensor;
    private RadioGroup sensorGroup;
    private RadioButton xmlBtn, soapBtn;

    private RadioGroup spotGroup;
    private RadioButton spot3Btn, spot4Btn;
    public static boolean spotChoice;

    private Button getTempBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soapclient);

        setTitle(R.string.soap_activity_title);

        spotChoice = false;

        xmlSensor = new XmlSensor();
        soapSensor= new SoapSensor();
        sensor = xmlSensor;

        mTemperatureTextView = (TextView) findViewById(R.id.soapTemperature);
        mDebugTextView = (TextView) findViewById(R.id.soapDebug);

        mTemperatureTextView.setText("");
        mDebugTextView.setText("");

        sensorGroup = (RadioGroup) findViewById(R.id.sensorGroup);
        xmlBtn = (RadioButton) findViewById(R.id.xmlBtn);
        soapBtn = (RadioButton) findViewById(R.id.soapBtn);

        xmlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Xml sensor chosen", Toast.LENGTH_SHORT);
            }
        });

        soapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Soap sensor chosen", Toast.LENGTH_SHORT);
            }
        });

        sensorGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.xmlBtn){
                    sensor = xmlSensor;
                }
                else {
                    sensor = soapSensor;
                }
            }
        });

        spotGroup = (RadioGroup) findViewById(R.id.spotGroup);
        spot3Btn = (RadioButton) findViewById(R.id.spot3Btn);
        spot4Btn = (RadioButton) findViewById(R.id.spot4Btn);

        spot3Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Spot 3 selected", Toast.LENGTH_SHORT);
            }
        });

        spot4Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Spot 4 selected", Toast.LENGTH_SHORT);
            }
        });

        spotGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.spot3Btn){
                    spotChoice = false;
                }
                else {
                    spotChoice = true;
                }
            }
        });


        getTempBtn = (Button) findViewById(R.id.getTempBtn);
        getTempBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sensor.getTemperature();
            }
        });
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