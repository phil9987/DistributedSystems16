package ch.ethz.inf.vs.a2.sensor;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.ethz.inf.vs.a2.http.HttpRawRequest;

/**
 * Created by David Keller on 17.10.16.
 */

public class TextSensor extends AbstractSensor {

    final private String DEBUG_STR = "RESTful (TextSensor)";


    @Override
    public String executeRequest() throws Exception {
        URL url = new URL("http://vslab.inf.ethz.ch:8081/sunspots/Spot1/sensors/temperature");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestProperty("Accept", "text/plain");

        // We expect a single line with the value
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String body = in.readLine();

        connection.disconnect();

        return body;
    }

    @Override
    public double parseResponse(String response) {
        String lines[] = response.split("\n");
        String lastLine = lines[lines.length - 1];

        return Double.parseDouble(lastLine);
    }
}
