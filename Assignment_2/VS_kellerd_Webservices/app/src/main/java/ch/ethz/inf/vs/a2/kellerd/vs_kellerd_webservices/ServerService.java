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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by philipjunker on 19.10.16.
 */

public class ServerService extends Service implements SensorEventListener{
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    public static final int SERVERPORT = 8034;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String acutator1 = "flashlight"; // actuator to turn on flashlight for 5 seconds
    private String actuator2 = "vibrate" ; // vibrate for 5 seconds
    private String sensor1 = "ambientlight";
    private String sensor2 = "barometer";
    private String root = "Root page";

    private String a1url = "/" + acutator1 + "/";
    private String a2url = "/" + actuator2 + "/";
    private String s1url = "/" + sensor1 + "/";
    private String s2url = "/" + sensor2 + "/";

    private String header = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd>" +
            "<html xmlns=\"http://www.w3.org/1999/xhtml\"> \n" +
            "\t<head>\n" +
            "\t\t<title>";
    private String beginBody = "</title></head><body>\n";
    private String endBody = "</body></html>\r\n\r\n";
    private String rootLink = "";

    private String http200Hdr = "HTTP/1.1 200 OK\n" +
            "Content-Type: text/html; charset=UTF-8" +
            "\r\n\r\n";

    private String http404Hdr = "HTTP/1.1 404 OK\n" +
            "Content-Type: text/html; charset=UTF-8" +
            "\r\n\r\n";

    private SensorManager sensorMgr;
    private Sensor lightSensor;
    private Sensor barometer;

    private double ligthVal;
    private double barometerVal;

    private android.hardware.Camera cam;
    private android.hardware.Camera.Parameters camParams;

    // builder to generate http response, id=requested sensor/actuator
    private String pageBuilder (String id){
        String title = null;
        String body = null;
        if (id.equals(acutator1)){
            title = "Flashlight actuator page";
            body = paragraph(bold(title)) +
                    paragraph("Flashlight is now activated for 5s. It gets deactivated automatically");
        }
        else if (id.equals(actuator2)){
            title = "Vibration actuator page";
            body = paragraph(bold(title)) +
                    paragraph("Vibration activated. The phone vibrates for 5s");
        }
        else if (id.equals(sensor1)){
            title = "Ambientlight sensor page";
            body = paragraph(bold(title)) +
                    paragraph("Ambient light measured by the phone: " + ligthVal + "lx");
        }
        else if (id.equals(sensor2)){
            title = "Barometer sensor page";
            body = paragraph(bold(title)) +
                    paragraph("Ambient air pressure measured by the phone: " + barometerVal + "hPa");
        }
        else if (id.equals(root)){
            title = "REST webserver root page";
            body = paragraph(bold(title))+
                    paragraph("Welcome to our REST server powered by Android")
                    + paragraph(getLink(a1url, "Actuator 1: Vibration"))
                    + paragraph(getLink(a2url, "Actuator 2: Flashlight"))
                    + paragraph(getLink(s1url, "Sensor 1: Ambient light"))
                    + paragraph(getLink(s2url, "Sensor 2: Barometer"));
        }
        else {
            title = "404: You (or we) screwed up the internet!";
            body = paragraph(bold(title)) +
                    paragraph("Seems like you're trying to access a page that doesn't exist...") +
                    paragraph(getLink("/", "Go back to rootpage"));

        }

        StringBuilder builder = new StringBuilder("");
        builder.append(header);
        builder.append(title);
        builder.append(beginBody);
        // add specific page
        builder.append(body);
        // link to root page
        builder.append(getLink("/", "Go back to rootpage"));
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

    /*@Override
    public void onCreate() {
        super.onCreate();
        Log.d("DEBUG", "onCreate Called!");
    }*/

    //Service functionality
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("DEBUG", "onStartCommand Called!");
        sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorMgr.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorMgr.registerListener(this,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
        barometer = sensorMgr.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorMgr.registerListener(this,barometer,SensorManager.SENSOR_DELAY_NORMAL);


// TODO get incoming initialize socket... / get incoming HTTP requests / build&send response (pageBuilder, responseBuilder
        Log.d("DEBUG", "ServerService starting...");
        updateConversationHandler = new Handler();
        this.serverThread = new Thread(new ServerThread());
        this.serverThread.start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        sensorMgr.unregisterListener(this);
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

    class ServerThread implements Runnable{
        @Override
        public void run() {
            Socket socket = null;
            try{
                Log.d("DEBUG: ", "starting server on port " + Integer.toString(SERVERPORT));
                serverSocket = new ServerSocket(SERVERPORT);
            }   catch (IOException e){
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()){
                try {
                    socket = serverSocket.accept();
                    Log.d("DEBUG: ", "incoming connection! starting communication thread!");
                    CommunicationThread commThread = new CommunicationThread(socket);
                    new Thread(commThread).start();
                }   catch (IOException e){
                    e.printStackTrace();
                }

            }
        }
    }

    class CommunicationThread implements Runnable{
        private Socket clientSocket;
        private BufferedReader input;
        public CommunicationThread(Socket clientSocket){
            this.clientSocket = clientSocket;
            try{
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                Log.d("DEBUG: ", "starting communication thread...");
            }   catch (IOException e){
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()){
                try{
                    String read = input.readLine();
                    Log.d("RECEIVED MSG: ", " " + read);

                }   catch(IOException e){
                    e.printStackTrace();
                }
            }

        }
    }

    /*protected class ConnectionThread implements Runnable {
        protected Socket socket;
        protected SimpleHttpRequestHandler handler;

        public ConnectionThread(Socket socket, SimpleHttpRequestHandler handler) {
            this.socket = socket;
            this.handler = handler;
        }


        @Override
        public void run() {
            Log.d(SERVER_TAG, "client here: wait for request");

            HttpRawRequest req = new HttpRawRequestImpl();
            try {
                req.parseRequest(socket.getInputStream());
                Log.d(SERVER_TAG, "request parsed");

                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                writer.write(handler.handle(req).generateResponse());
                writer.flush();
                Log.d(SERVER_TAG, "response sent");

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/
}