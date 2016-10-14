package ch.ethz.inf.vs.kellerd.vs_kellerd_sensors;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.Context.SENSOR_SERVICE;

public class SensorActivity extends AppCompatActivity implements SensorEventListener {
    private GraphContainer graphContainer;
    private SensorTypesImpl sensortypes;
    private SensorManager sensorMgr;
    private Sensor sensor;
    private double starttime;
    private double graph_upper_bound;
    private double graph_lower_bound;
    private int num_graph_elements;

    private GraphView graph;
    private List<LineGraphSeries<DataPoint>> mSeries;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        TextView textView = (TextView) findViewById(R.id.sensor_data_text);
        textView.setText("N/A");
        Intent myIntent = getIntent();
        String sensor_name = myIntent.getStringExtra("name");
        setTitle(sensor_name);

        int sensor_list_id = myIntent.getIntExtra("sensor_index", 0);       // get sensor id from main-activity
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        List<Sensor> sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL);    // get sensor list
        sensor = sensors.get(sensor_list_id);                               // get chosen sensor
        sensorMgr.registerListener(this,sensor, SensorManager.SENSOR_DELAY_UI);
        sensortypes = new SensorTypesImpl();
        int num_sensor_values = sensortypes.getNumberValues(sensor.getType());
        graphContainer = new GraphContainerImpl(num_sensor_values);
        starttime = -1;

        graph = (GraphView) findViewById(R.id.graph);
        mSeries = new ArrayList<LineGraphSeries<DataPoint> >(3);
        for(int i = 0; i < num_sensor_values; i++){
            mSeries.add(i, new LineGraphSeries<DataPoint>());
            int col;
            switch (i) {
                case 0:
                    col = Color.RED;
                    break;
                case 1:
                    col = Color.BLUE;
                    break;
                case 2:
                    col = Color.MAGENTA;
                    break;
                default:
                    col = Color.CYAN;
                    break;
            }
            mSeries.get(i).setColor(col);
            graph.addSeries(mSeries.get(i));
        }
        graph.getViewport().setXAxisBoundsManual(true);
        graph_upper_bound = 1;
        graph_lower_bound = 0;
        num_graph_elements = 0;
        graph.getViewport().setMinX(graph_lower_bound);
        graph.getViewport().setMaxX(graph_upper_bound);
        GridLabelRenderer labelrenderer = graph.getGridLabelRenderer();
        labelrenderer.setHorizontalAxisTitle("s");
        labelrenderer.setVerticalAxisTitle(sensortypes.getUnitString(sensor.getType()));
        labelrenderer.setLabelVerticalWidth(80);

    }

    public GraphContainer getGraphContainer() {
        return graphContainer;
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorMgr.registerListener(this,sensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorMgr.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensortype = event.sensor.getType();
        double timestamp = event.timestamp;
        float[] eventVals = event.values.clone();
        if(starttime == -1) {
            starttime = timestamp / 1000000000d;
        }
        else {
            int num_values = sensortypes.getNumberValues(sensortype);
            String unit = sensortypes.getUnitString(sensortype);
            String new_value = "";
            double x_val = timestamp / 1000000000d - starttime;
            if(graph_lower_bound == 0){
                graph_lower_bound = x_val;
                graph.getViewport().setMinX(graph_lower_bound);
            }
            for (int i = 0; i < num_values; i++) {
                new_value += String.valueOf(eventVals);
                new_value += ("\t " + unit + "\n");
            }
            try {
                graphContainer.addValues(x_val, Arrays.copyOfRange(eventVals, 0, num_values));
            }
            catch (Exception e) {
                // Pass
            }
            DataPoint[] data = graphContainer.generateData(0);
            if (data.length == 100) {
                double minx = data[0].getX();
                graph.getViewport().setMinX(minx);
                graph.getViewport().setMaxX(x_val);
            }

            TextView textView = (TextView) findViewById(R.id.sensor_data_text);
            textView.setText(new_value);


            if (graph_upper_bound < x_val) {
                graph_upper_bound = x_val;
                graph.getViewport().setMaxX(graph_upper_bound);
            }
            for (int i = 0; i < num_values; i++) {
                if (i > 0) {
                    data = graphContainer.generateData(i);
                }
                mSeries.get(i).resetData(data);
            }
            //Log.d("data_size = ", String.valueOf(num_graph_elements));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do not do anything
    }
}
