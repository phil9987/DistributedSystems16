package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
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
    private MediaPlayer player;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private static final String acutator1 = "sound"; // actuator to turn on flashlight
    private static final String actuator2 = "vibrate" ; // vibrate for 5 seconds
    private static final String sensor1 = "ambientlight";
    private static final String sensor2 = "barometer";
    private static final String root = "";

    private String a1url = "/" + acutator1 + "/";
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

    private String getHeader(String ip){
        return "<!DOCTYPE html>" +
                "<html> \n" +
                "\t<head>\n" +
                "<base href=\"" + "http://" + ip + "\" />\n" +
                "\t\t<title>";
    }
    private String form ="<form method=\"post\" action=''>"+
    "<label for=\"pattern\">Vibrate pattern <small>(pattern separated by commas)</small></label>"+
    "<input id='pattern' type='text' name='pattern' value='30,10,30' />"+
    "<input type='submit' name='Send' value='Send' />"+
    "</form>"+
    "<form method='post' action=''>"+
    "<select>"+
    "<option value='foo'>Foo</option>"+
    "<option value='bar'>Bar</option>"+
    "<option value='baz'>Baz</option>"+
    "</select>"+
    "<input type='submit' name='Send' value='Send' />"+
    "</form>";


    // builder to generate http response, id=requested sensor/actuator
    private String pageBuilder (String id, Boolean html){
        String title;
        String body;
        if (id.equals(acutator1)) {
            title = "Sound actuator page";
            body = "A sound is being played on the server phone for 30 seconds. Please make sure your sound is turned on."+
                    "\n"+
                    form;
            ;
            if (html) {
                body = paragraph(bold(title)) + "\r\n\r\n" +
                        paragraph(body);
            }
        }
        else if (id.equals(actuator2)){
            title = "Vibration actuator page";
            body = "Vibration activated. The phone vibrates for 5s";
            if(html) {
                body = paragraph(bold(title)) + "\r\n\r\n" +
                        paragraph(body);
            }

        }
        else if (id.equals(sensor1)){
            title = "Ambientlight sensor page";
            body = "Ambient light measured by the phone: " + String.format("%.2f",lightVal) + " lx";
            if(html) {
                body = paragraph(bold(title)) + "\r\n\r\n" +
                        paragraph(body);
            }
        }
        else if (id.equals(sensor2)){
            title = "Barometer sensor page";
            body = "Ambient air pressure measured by the phone: " + String.format("%.2f",barometerVal) + " hPa";
            if(html) {
                body = paragraph(bold(title)) + "\r\n\r\n" +
                        paragraph(body);
            }
        }
        else if (id.equals(root)){
            title = "REST webserver root page";
            if(html) {
                body = paragraph(bold(title)) + "\r\n\r\n" +
                        paragraph("Welcome to our REST server powered by Android") + "\r\n"
                        + paragraph(getLink(a1url, "Actuator 1: Sound")) + "\r\n"
                        + paragraph(getLink(a2url, "Actuator 2: Vibration")) + "\r\n"
                        + paragraph(getLink(s1url, "Sensor 1: Ambient light")) + "\r\n"
                        + paragraph(getLink(s2url, "Sensor 2: Barometer"));
            }else{
                body = "welcome to our REST server powered by Android\r\n"
                        + "Actuator 1: Sound: " + ipAddress + a1url + "\r\n"
                        + "Actuator 2: Vibration: " + ipAddress + a2url + "\r\n"
                        + "Sensor 1: Ambient light: " + ipAddress + s1url + "\r\n"
                        + "Sensor 2: Barometer: " + ipAddress + s2url + "\r\n";
            }
        }
        else {
            title = "404: You (or we) screwed up the internet!";
            body = "Seems like you're trying to access a page that doesn't exist...";
            if(html) {
                body = paragraph(bold(title)) + "\r\n" +
                        paragraph(body);
            }
        }

        StringBuilder builder = new StringBuilder("");
        if(html) {
            builder.append(getHeader(ipAddress));
            builder.append(title);
            builder.append(beginBody);
            // add specific page
            builder.append(body);
            // link to root page
            builder.append("\r\n" + paragraph(getLink("./", "Go back to rootpage")));
            builder.append(endBody);
        }else{
            builder.append(title + "\r\n");
            builder.append(body);
        }

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
            Log.d(SERVICE_TAG, "Listener registered for " + lightSensor.getName() + " Sensor.");
        }else{
            Log.d(SERVICE_TAG, "no light sensor available...");
        }
        barometer = sensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (barometer != null) {
            sensorMgr.registerListener(this,barometer,SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(SERVICE_TAG, "Listener registered for " + barometer.getName() + " Sensor.");
        }else{
            Log.d(SERVICE_TAG, "no barometer sensor available...");
        }
        int sound = R.raw.piano_short;
        player = MediaPlayer.create(getApplicationContext(), sound);
        player.setVolume(1.0f, 1.0f);
        player.setLooping(false);




        Log.d(SERVICE_TAG, "ServerService starting...");
        this.server = new RawHttpServer();
        server.start(SERVERPORT, this);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        sensorMgr.unregisterListener(this);
        server.stop();
        player.stop();
        super.onDestroy();
    }


    private void vibrationAction(String pattern){
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long duration = 0;
        String[] patternElements = pattern.split(",");
        long vibPattern[] = new long[patternElements.length];
        for (int i=0; i<patternElements.length; i++){
            duration = Long.valueOf(patternElements[i]);
            vibPattern[i]= duration;
        }
        vibrator.vibrate(vibPattern,0);
    }


    private void soundAction(){
        if(!player.isPlaying())
            player.start();

    }

    
    //SensorListener functionality
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()){
            case Sensor.TYPE_PRESSURE:
                barometerVal = event.values[0];
                break;
            case Sensor.TYPE_LIGHT:
                lightVal = event.values[0];
                break;
        }
    }

    /*
     * Handles a request
     */
    @Override
    public String handle(String path, String accept, String data) {
        if (path.startsWith("/")) path = path.substring(1);
        if (path.endsWith("/")) path = path.substring(0,path.length()-1);
        switch(path){
            case acutator1:     //temperature
                soundAction();
                break;
            case actuator2:     //vibration
                vibrationAction(data);
                break;
            case sensor1:       //ambient light

                break;
            case sensor2:       //barometer

                break;
            default:
                break;

        }
        String page;

        if (accept.equals("text/plain")) {
            page = pageBuilder(path, false);
        }else{
            page = pageBuilder(path, true);
        }
        return responseBuilder(page);
    }

}