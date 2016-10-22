package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import ch.ethz.inf.vs.a2.http.HttpRequestHandler;
import ch.ethz.inf.vs.a2.http.RawHttpServer;

/**
 * Created by philipjunker on 19.10.16.
 */

public class ServerService extends Service implements SensorEventListener, HttpRequestHandler{

    public static final String SERVICE_TAG = "### ServerService ###";
    public static final int SERVERPORT = 8034;
    protected static RawHttpServer server = null;
    private String ipAddress;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String acutator1 = "flashlight"; // actuator to turn on flashlight for 5 seconds
    private static final String actuator2 = "vibrate" ; // vibrate for 5 seconds
    private static final String sensor1 = "ambientlight";
    private static final String sensor2 = "barometer";
    private static final String root = "";

    private String a1url = "/" + acutator1 + "/";
    private String a2url = "/" + actuator2 + "/";
    private String s1url = "/" + sensor1 + "/";
    private String s2url = "/" + sensor2 + "/";

    private String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd>" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\"> \n" +
            "\t<head>\n" +
            "<base href=\"" + ipAddress + "\" />\n" +
            "\t\t<title>";
    private String beginBody = "</title></head><body>\n";
    private String endBody = "</body></html>\r\n\r\n";

    private String http200Hdr = "HTTP/1.1 200 OK\r\n" +
            "Content-Type: text/html; charset=UTF-8\r\n" +
            "Connection: close" +
            "\r\n\r\n";

    private String http404Hdr = "HTTP/1.1 404 OK\n" +
            "Content-Type: text/html; charset=UTF-8" +
            "Connection: close" +
            "\r\n\r\n";

    private SensorManager sensorMgr;
    private Sensor lightSensor;
    private Sensor barometer;

    private double ligthVal;
    private double barometerVal;

    private android.hardware.Camera cam;
    private android.hardware.Camera.Parameters camParams;
    private String getHeader(String ip){
        return "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd>" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\"> \n" +
                "\t<head>\n" +
                "<base href=\"" + "http://" + ip + "\" />\n" +
                "\t\t<title>";
    }
    // builder to generate http response, id=requested sensor/actuator
    private String pageBuilder (String id){
        String title = null;
        String body = null;
        if (id.equals(acutator1)){
            title = "Flashlight actuator page";
            body = paragraph(bold(title)) + "\r\n\r\n" +
                    paragraph("Flashlight is now activated for 5s. It gets deactivated automatically");
        }
        else if (id.equals(actuator2)){
            title = "Vibration actuator page";
            body = paragraph(bold(title)) + "\r\n\r\n" +
                    paragraph("Vibration activated. The phone vibrates for 5s");
        }
        else if (id.equals(sensor1)){
            title = "Ambientlight sensor page";
            body = paragraph(bold(title)) + "\r\n\r\n" +
                    paragraph("Ambient light measured by the phone: " + ligthVal + "lx");
        }
        else if (id.equals(sensor2)){
            title = "Barometer sensor page";
            body = paragraph(bold(title)) + "\r\n\r\n" +
                    paragraph("Ambient air pressure measured by the phone: " + barometerVal + "hPa");
        }
        else if (id.equals(root)){
            title = "REST webserver root page";
            body = paragraph(bold(title))+ "\r\n\r\n" +
                    paragraph("Welcome to our REST server powered by Android") + "\r\n"
                    + paragraph(getLink(a1url, "Actuator 1: Flashlight")) + "\r\n"
                    + paragraph(getLink(a2url, "Actuator 2: Vibration")) + "\r\n"
                    + paragraph(getLink(s1url, "Sensor 1: Ambient light")) + "\r\n"
                    + paragraph(getLink(s2url, "Sensor 2: Barometer"));
        }
        else {
            title = "404: You (or we) screwed up the internet!";
            body = paragraph(bold(title)) + "\r\n" +
                    paragraph("Seems like you're trying to access a page that doesn't exist...");

        }

        StringBuilder builder = new StringBuilder("");
        builder.append(getHeader(ipAddress));
        builder.append(title);
        builder.append(beginBody);
        // add specific page
        builder.append(body);
        // link to root page
        builder.append("\r\n" + paragraph( getLink("./", "Go back to rootpage")));
        builder.append(endBody);

        return builder.toString();
    }

    //builder to create response to the requests, adds HTTP header...
    private String responseBuilder(String page){
        StringBuilder response = new StringBuilder();
        if (page.contains("404")){
            response.append(http404Hdr);
        }
        else {
            response.append(http200Hdr);
        }
        response.append(page);
        return response.toString();
    }


    // helpers
    private String bold(String text) {
        return "<b><font size=\"16\">"+text+"</font></b>";
    }

    private String paragraph(String text){
        return "<p>" + text + "</p";
    }

    private String getLink(String url, String text){
        return " <a href=\"" + url + "\">"+text + "</a> ";
    }


    //Service functionality
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(SERVICE_TAG, "onStartCommand Called!");
        ipAddress = intent.getStringExtra("ip");
        Log.d(SERVICE_TAG, ipAddress);
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
        Log.d(SERVICE_TAG, lightSensor.getName());
        sensorMgr.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        barometer = sensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE);
        Log.d(SERVICE_TAG, barometer.getName());
        sensorMgr.registerListener(this,barometer,SensorManager.SENSOR_DELAY_NORMAL);


        Log.d(SERVICE_TAG, "ServerService starting...");
        this.server = new RawHttpServer();
        server.start(SERVERPORT, this);
        // TODO build&send response (pageBuilder, responseBuilder)
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        sensorMgr.unregisterListener(this);
        server.stop();
        super.onDestroy();
    }

    // helpers to trigger actuators

    private void flashlightAction(){
        try {
            cam = android.hardware.Camera.open();
            camParams = cam.getParameters();
            camParams.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
            cam.setParameters(camParams);
            cam.startPreview();

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    camParams = cam.getParameters();
                    camParams.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_OFF);
                    cam.stopPreview();
                }
            },5000);
        }
        catch (RuntimeException e){
            Log.e("Error", e.getMessage());
        }
    }

    private void vibrationAction(){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(5000);
    }




    //SensorListener functionality
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.equals(Sensor.TYPE_PRESSURE)){
            barometerVal = event.values[0];
        }
        else if (event.sensor.equals(Sensor.TYPE_LIGHT)){
            ligthVal = event.values[0];
        }
    }

    @Override
    public String handle(String path, String request) {
        if (path.startsWith("/")) path = path.substring(1);
        if (path.endsWith("/")) path = path.substring(0,path.length()-1);
        String page = pageBuilder(path);
        //Log.d(SERVICE_TAG, page);
        switch(path){
            case acutator1:     //flashlight
                flashlightAction();
                break;
            case actuator2:     //vibration
                vibrationAction();
                break;
            case sensor1:       //ambient light

                break;
            case sensor2:       //barometer

                break;
            default:
                break;

        }
        String response = responseBuilder(page);
        Log.d(SERVICE_TAG, response);
        return response;
    }

}