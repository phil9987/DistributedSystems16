package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
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

    private static final String acutator1_on = "flashlightOn"; // actuator to turn on flashlight
    private static final String actuator1_off = "flashlightOff";    // actuator to turn off flashlight
    private static final String actuator2 = "vibrate" ; // vibrate for 5 seconds
    private static final String sensor1 = "ambientlight";
    private static final String sensor2 = "barometer";
    private static final String root = "";

    private String a1onurl = "/" + acutator1_on + "/";
    private String a1offurl = "/" + actuator1_off + "/";
    private String a2url = "/" + actuator2 + "/";
    private String s1url = "/" + sensor1 + "/";
    private String s2url = "/" + sensor2 + "/";
    
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

    private double lightVal;
    private double barometerVal;

    private android.hardware.Camera cam;
    private android.hardware.Camera.Parameters camParams;
    private String getHeader(String ip){
        return "<!DOCTYPE html>" +
                "<html> \n" +
                "\t<head>\n" +
                "<base href=\"" + "http://" + ip + "\" />\n" +
                "\t\t<title>";
    }
    // builder to generate http response, id=requested sensor/actuator
    private String pageBuilder (String id){
        String title = null;
        String body = null;
        if (id.equals(acutator1_on)){
            title = "Flashlight activated";
            body = paragraph(bold(title)) + "\r\n\r\n" +
                    paragraph("Flashlight is now activated. To turn it off, " + getLink(a1offurl, "click here."));
        }
        else if (id.equals(actuator1_off)){
            title = "Flashlight deactivated";
            body = paragraph(bold(title)) + "\r\n\r\n" +
                    paragraph("Flashlight is now deactivated. To turn it on, " + getLink(a1onurl, "click here."));
        }
        else if (id.equals(actuator2)){
            title = "Vibration actuator page";
            body = paragraph(bold(title)) + "\r\n\r\n" +
                    paragraph("Vibration activated. The phone vibrates for 5s");
        }
        else if (id.equals(sensor1)){
            title = "Ambientlight sensor page";
            body = paragraph(bold(title)) + "\r\n\r\n" +
                    paragraph("Ambient light measured by the phone: " + String.format("%.2f",lightVal) + " lx");
        }
        else if (id.equals(sensor2)){
            title = "Barometer sensor page";
            body = paragraph(bold(title)) + "\r\n\r\n" +
                    paragraph("Ambient air pressure measured by the phone: " + String.format("%.2f",barometerVal) + " hPa");
        }
        else if (id.equals(root)){
            title = "REST webserver root page";
            body = paragraph(bold(title))+ "\r\n\r\n" +
                    paragraph("Welcome to our REST server powered by Android") + "\r\n"
                    + paragraph(getLink(a1onurl, "Actuator 1: Flashlight on")) + "\r\n"
                    + paragraph(getLink(a1offurl, "Actuator 1: Flashlight off")) + "\r\n"
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
        if (lightSensor != null) {
            sensorMgr.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_UI);
            Log.d(SERVICE_TAG, lightSensor.getName());
        }else{
            Log.d(SERVICE_TAG, "no light sensor available...");
        }
        barometer = sensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (barometer != null) {
            sensorMgr.registerListener(this,barometer,SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(SERVICE_TAG, barometer.getName());
        }else{
            Log.d(SERVICE_TAG, "no barometer sensor available...");
        }



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

    private void flashlightAction(Boolean activate){
        try {
            if (activate) {
                cam = android.hardware.Camera.open();
                camParams = cam.getParameters();
                camParams.setFlashMode(android.hardware.Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(camParams);
                cam.startPreview();
            }else{
                camParams = cam.getParameters();
                camParams.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                cam.setParameters(camParams);
                cam.stopPreview();
            }
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
        if (event.sensor.getType() == Sensor.TYPE_PRESSURE){
            barometerVal = event.values[0];
            Log.d(SERVICE_TAG, "PRESSURE SENSOR CHANGED!!!");
            Log.d(SERVICE_TAG, Double.toString(barometerVal));
        }
        else if (event.sensor.getType() == Sensor.TYPE_LIGHT){
            lightVal = event.values[0];
        }
    }

    @Override
    public String handle(String path, String request) {
        if (path.startsWith("/")) path = path.substring(1);
        if (path.endsWith("/")) path = path.substring(0,path.length()-1);
        String page = pageBuilder(path);
        //Log.d(SERVICE_TAG, page);
        switch(path){
            case acutator1_on:     //flashlight on
                flashlightAction(true);
                break;
            case actuator1_off:
                flashlightAction(false);
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