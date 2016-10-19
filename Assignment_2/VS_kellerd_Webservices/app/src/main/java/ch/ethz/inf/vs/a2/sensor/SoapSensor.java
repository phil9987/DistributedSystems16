package ch.ethz.inf.vs.a2.sensor;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by simon on 17.10.16.
 */

public class SoapSensor extends AbstractSensor implements Sensor {
    private String namespace = "http://webservices.vslecture.vs.inf.ethz.ch/";
    private String method = "getSpot";
    private String xmlTag = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private String url="http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice";
    SoapObject request;


    @Override
    public double parseResponse(String response) {
        Log.d("SOAP Sensor", "response: " + response );
        return 0;
    }

    @Override
    public String executeRequest() throws Exception {
        request = new SoapObject(namespace, method);

        PropertyInfo property =new PropertyInfo();
        property.setName("id");
        property.setValue("spot4");
        request.addProperty(property);
        final SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.setOutputSoapObject(request);
        final HttpTransportSE transportSE = new HttpTransportSE(url);
        transportSE.setXmlVersionTag(xmlTag);
        transportSE.debug = true;

        AsyncTask<SoapSerializationEnvelope,Void,SoapObject> soapTask = new AsyncTask<SoapSerializationEnvelope, Void, SoapObject>() {
            @Override
            protected SoapObject doInBackground(SoapSerializationEnvelope... params) {
                try {
                    transportSE.call("http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice",params[0]);
                    SoapObject result = (SoapObject) params[0].getResponse();
                    return result;
                }
                catch (IOException | XmlPullParserException e){
                    e.printStackTrace();

                }
                return null;
            }

            @Override
            protected void onPostExecute(SoapObject r) {
                Log.i("onPostExecute", " on post Started");

                String result = r.getPropertyAsString("temperature");
                Log.d("onPostExecute", result);
                //super.onPostExecute(result);
                double value = parseResponse(result);

                if (value != Double.NaN) {
                    Log.i("onPostExecute", "value ist double");
                        sendValue(value);
                } else {
                        sendMessage(r.getPropertySafelyAsString("faultstring"));
                }

        }
    };
        SoapSerializationEnvelope[] params = new SoapSerializationEnvelope[1];
        params[0] = envelope;
        soapTask.equals(params);


        return null;
    }



}
