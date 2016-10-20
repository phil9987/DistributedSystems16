package ch.ethz.inf.vs.a2.sensor;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices.SOAPActivity;

/**
 * Created by simon on 17.10.16.
 * inspired by  http://mashtips.com/call-soap-with-request-xml-and-get-response-xml-back/
 */

public class SoapSensor extends AbstractSensor implements Sensor {
    final String ACTIVITY_TAG = "Soap Sensor";
    private final static String namespace = "http://webservices.vslecture.vs.inf.ethz.ch/";
    private final static String method = "getSpot";
    private final static String xmlTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private final static String url="http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice";
    private final static String SoapAction = "";
    private SoapObject request;


    @Override
    public double parseResponse(String response) {
        return Double.valueOf(response);
    }

    @Override
    public String executeRequest() throws Exception {
        boolean spotChoice = SOAPActivity.spotChoice;
        String temperature;

        request = new SoapObject(namespace, method);

        //set properties of the soap request
        PropertyInfo property =new PropertyInfo();
        property.setName("id");
        if (spotChoice){
            property.setValue("spot3");
        }
        else {
            property.setValue("spot4");
        }

        //add propoerties to the request
        request.addProperty(property);
        //create envelope
        final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);

        //get connection to url
        final HttpTransportSE transportSE = new HttpTransportSE(url);
        transportSE.setXmlVersionTag(xmlTag);
        transportSE.debug = true;

        //send request to the network
        transportSE.call(SoapAction,envelope);

        SoapObject response = (SoapObject) envelope.getResponse();

        //read temperature from response
        temperature = response.getPropertySafelyAsString("temperature");

        return temperature;
    }



}
