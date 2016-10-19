package ch.ethz.inf.vs.a2.sensor;

import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

// http://mashtips.com/call-soap-with-request-xml-and-get-response-xml-back/

/**
 * Created by simon on 17.10.16.
 */

public class SoapSensor extends AbstractSensor implements Sensor {
    final String ACTIVITY_TAG = "Soap Sensor";
    private final static String namespace = "http://webservices.vslecture.vs.inf.ethz.ch/";
    private final static String method = "getSpot";
    private final static String xmlTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private final static String url="http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice";
    private final static String SoapAction = "";
    SoapObject request;


    @Override
    public double parseResponse(String response) {
        return Double.valueOf(response);
    }

    @Override
    public String executeRequest() throws Exception {
        String temperature = null;
        request = new SoapObject(namespace, method);

        PropertyInfo property =new PropertyInfo();
        property.setName("id");
        property.setValue("spot4");
        request.addProperty(property);
        final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.setOutputSoapObject(request);
        final HttpTransportSE transportSE = new HttpTransportSE(url);
        transportSE.setXmlVersionTag(xmlTag);
        transportSE.debug = true;

        transportSE.call(SoapAction,envelope);
        Log.d(ACTIVITY_TAG, transportSE.requestDump);
        SoapObject response = (SoapObject) envelope.getResponse();
        Log.d(ACTIVITY_TAG, transportSE.responseDump);

        temperature = response.getPropertySafelyAsString("temperature");


        return temperature;
    }



}
