package ch.ethz.inf.vs.a2.sensor;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by David Keller on 17.10.16.
 */

public class JsonSensor extends AbstractSensor {
    @Override
    public String executeRequest() throws Exception {
        URL url = new URL("http://vslab.inf.ethz.ch:8081/sunspots/Spot1/sensors/temperature");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Accept", "application/json");

        // We expect a single line with the value
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String body = in.readLine();

        connection.disconnect();

        return body;
    }

    @Override
    public double parseResponse(String response) {
        JSONObject json;
        double temperature = 0;

        try {
            json = new JSONObject(response);
            temperature = json.getDouble("value");
        } catch (Exception e) {
            return 0;
        }

        return temperature;
    }
}
