package ch.ethz.inf.vs.a2.sensor;

import android.util.Log;
import android.util.Xml;
import android.view.animation.AccelerateInterpolator;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.AttributedCharacterIterator;

/**
 * Created by simon on 17.10.16.
 */


// http://www.herongyang.com/Web-Services/Java-net-HttpURLConnection-Send-SOAP-Message.html
public class XmlSensor extends AbstractSensor {
    private URL serveradress;
    private HttpURLConnection connection;
    private String soapAction;
    private String reqXML;
    final String ACTIVITY_TAG = "XmlSensor: ";



    @Override
    public double parseResponse(String response) {
        double temperature = 0;
            Log.d(ACTIVITY_TAG, "response: " + response.toString());
// https://developer.android.com/training/basics/network-ops/xml.html
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(response));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                if(eventType == XmlPullParser.START_TAG) {
                    Log.d(ACTIVITY_TAG,parser.getName());
                    if (parser.getName().equals("temperature")) {
                        Log.d(ACTIVITY_TAG,parser.getName());
                        eventType=parser.next();
                        temperature = Double.valueOf(parser.getText());
                        Log.d(ACTIVITY_TAG, String.valueOf(temperature));
                    }
                }
                eventType = parser.next();
            }
        }
        catch (XmlPullParserException | IOException e){
            Log.d(ACTIVITY_TAG, "parser exception: " + e.toString());
        }


        return temperature;

    }

    @Override
    public String executeRequest() throws Exception{
        connection = null;
        soapAction = ""; //http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice/getSpot";
        reqXML ="<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "    <S:Header/>" +
                "    <S:Body>" +
                "        <ns2:getSpot xmlns:ns2=\"http://webservices.vslecture.vs.inf.ethz.ch/\">" +
                "            <id>Spot3</id>" +
                "        </ns2:getSpot>" +
                "    </S:Body>" +
                "</S:Envelope>";
        Log.d(ACTIVITY_TAG, "Open Connection");
        serveradress = new URL("http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice");
        connection = (HttpURLConnection) serveradress.openConnection();

        byte [] xmlout = reqXML.getBytes();



        connection.setRequestProperty("Content-Length", String.valueOf(xmlout.length));
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("SOAPAction", soapAction);
        connection.setDoOutput(true);

        OutputStream reqStream = connection.getOutputStream();
        reqStream.write(xmlout);
        reqStream.close();
        connection.connect();
        Log.d(ACTIVITY_TAG, "connection status: " + connection.getResponseMessage());

        String result="";
        BufferedReader in = new BufferedReader(new InputStreamReader(
                connection.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            result = result + inputLine;
        in.close();
        reqStream.close();
        return result;

    }

}
