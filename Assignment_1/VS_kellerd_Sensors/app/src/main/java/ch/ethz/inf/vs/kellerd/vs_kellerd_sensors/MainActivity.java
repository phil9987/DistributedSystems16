package ch.ethz.inf.vs.kellerd.vs_kellerd_sensors;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Array;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private SensorManager sensorMgr;
    private List<Sensor> sensors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensors = sensorMgr.getSensorList(Sensor.TYPE_ALL);     // get sensor list

        String[] sensor_names = new String[sensors.size()];
        int i = 0;                                              // get sensor names
        for (Sensor sensor : sensors) {
            sensor_names[i] = sensor.getName();
            i+=1;
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,   // adapter for listView
                android.R.layout.simple_list_item_1, sensor_names);
        ListView sensor_listview = (ListView) findViewById(R.id.sensor_listview);   // get listView
        sensor_listview.setAdapter(adapter);                     // add sensor names to listView

        sensor_listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = (String)parent.getItemAtPosition(position);
                String tag = "DEBUG";
                Log.d(tag, value);
                Log.d(tag, String.valueOf(id));
                Intent myIntent = new Intent(view.getContext(), SensorActivity.class);
                myIntent.putExtra("sensor_index",(int) id);
                myIntent.putExtra("name", value);
                startActivity(myIntent);
            }
        });
    }




}
