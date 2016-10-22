package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerActivity extends AppCompatActivity {

    protected Intent serverService;
    public static final String ACTIVITY_TAG = "### ServerActivity ###";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serverService = new Intent(this, ServerService.class);
        setContentView(R.layout.activity_server);
        setTitle(R.string.server_activity_title);
        ToggleButton buttonStartStop = (ToggleButton) findViewById(R.id.buttonStartStop);
        TextView textViewIP = (TextView) findViewById(R.id.textViewIP);
        buttonStartStop.setChecked(isMyServiceRunning(ServerService.class));
        String ip = getLocalIpAddress();
        textViewIP.setText(ip + ":8034");
        Log.d(ACTIVITY_TAG, "IP = " + ip);

    }

    public void onStartStopClick(View v){
        ToggleButton startstopbutton = (ToggleButton) v;
        if(startstopbutton.isChecked()){
            Log.d(ACTIVITY_TAG, "ToggleButton: start server");
            startService(serverService);

        }else{
            Log.d(ACTIVITY_TAG, "ToggleButton: stop server");
            stopService(serverService);
        }

    }

    public String getLocalIpAddress() {
        String address= null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                String name = intf.getName();
                if(name == "wlan0" || name.equals(""))
                    Log.d(ACTIVITY_TAG, "wlan0 found!!!!!!!!");
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    address = new String(inetAddress.getHostAddress().toString());
                    if (!inetAddress.isLoopbackAddress() && address.length() < 18) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            String msg = ex.getMessage();
            //do nothing
        }
        return null;
    }

    /**
     * http://stackoverflow.com/questions/600207/how-to-check-if-a-service-is-running-on-android
     *
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}