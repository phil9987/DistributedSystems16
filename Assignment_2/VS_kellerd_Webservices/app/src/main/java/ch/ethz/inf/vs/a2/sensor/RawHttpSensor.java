package ch.ethz.inf.vs.a2.sensor;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import ch.ethz.inf.vs.a2.http.HttpRawRequestImpl;
import ch.ethz.inf.vs.a2.sensor.AbstractSensor;

/**
 * Created by David Keller on 17.10.16.
 */

public class RawHttpSensor extends AbstractSensor {

    final private String DEBUG_STR = "RESTful";

    final private String HOST = "vslab.inf.ethz.ch";
    final private int PORT = 8081;
    final private String PATH = "/sunspots/Spot1/sensors/temperature";

    private PrintWriter out;
    private BufferedReader in;

    @Override
    public String executeRequest() throws Exception {
        HttpRawRequestImpl httpRawRequest = new HttpRawRequestImpl();
        Socket socket = new Socket(HOST, PORT);
        out = new PrintWriter(socket.getOutputStream());
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Send request
        out.print(httpRawRequest.generateRequest(HOST, PORT, PATH));
        out.flush();

        // Get response
        String response = "";
        String line;
        while ((line = in.readLine()) != null) {
            response += line + "\n";
        }

        return response;
    }

    @Override
    public double parseResponse(String response) {
        String lines[] = response.split("\n");
        String lastLine = lines[lines.length - 1];

        return Double.parseDouble(lastLine);
    }
}
