package ch.ethz.inf.vs.a2.sensor;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices.SOAPActivity;

/**
 * Created by simon on 17.10.16.
 * inspired by  http://www.herongyang.com/Web-Services/Java-net-HttpURLConnection-Send-SOAP-Message.html
 *              https://developer.android.com/training/basics/network-ops/xml.html
 */



public class XmlSensor extends AbstractSensor {
    private URL serveradress;
    private HttpURLConnection connection;
    private String soapAction;
    private String reqXML;
    private String spot;
    final String ACTIVITY_TAG = "XmlSensor: ";



    @Override
    public double parseResponse(String response) {
        double temperature = 0;
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(response));
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT){
                if(eventType == XmlPullParser.START_TAG) {
                    if (parser.getName().equals("temperature")) {
                        eventType=parser.next();
                        temperature = Double.valueOf(parser.getText());
                    }
                }
                eventType = parser.next();
            }
        }
        catch (XmlPullParserException | IOException e){
            e.printStackTrace();
        }

        return temperature;
    }

    @Override
    public String executeRequest() throws Exception{
        connection = null;
        boolean spotChoice = SOAPActivity.spotChoice;
        if (spotChoice){
            spot = "spot3";
        }
        else{
            spot = "spot4";
        }
        soapAction = "";
        reqXML ="<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">" +
                "    <S:Header/>" +
                "    <S:Body>" +
                "        <ns2:getSpot xmlns:ns2=\"http://webservices.vslecture.vs.inf.ethz.ch/\">" +
                "            <id>" +spot + "</id>" +
                "        </ns2:getSpot>" +
                "    </S:Body>" +
                "</S:Envelope>";
        serveradress = new URL("http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice");
        connection = (HttpURLConnection) serveradress.openConnection();

        byte [] xmlout = reqXML.getBytes();

        // Set connection properties
        connection.setRequestProperty("Content-Length", String.valueOf(xmlout.length));
        connection.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
        connection.setRequestMethod("POST");
        connection.setRequestProperty("SOAPAction", soapAction);
        connection.setDoOutput(true);

        // wirte request to the network
        OutputStream reqStream = connection.getOutputStream();
        reqStream.write(xmlout);
        reqStream.close();
        connection.connect();

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
