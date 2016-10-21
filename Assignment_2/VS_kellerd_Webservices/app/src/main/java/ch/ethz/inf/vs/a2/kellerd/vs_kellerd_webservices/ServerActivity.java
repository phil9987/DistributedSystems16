package ch.ethz.inf.vs.a2.kellerd.vs_kellerd_webservices;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        setTitle(R.string.server_activity_title);
        Button buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        TextView textViewIP = (TextView) findViewById(R.id.textViewIP);

        buttonStartStop.setText(R.string.button_start_server);
        String ip = getLocalIpAddress();
        textViewIP.setText(ip + ":8034");
        Log.d("DEBUG: ", "IP = " + ip);

    }

    public void onStartStopClick(View v){
        Intent intent = new Intent(this, ServerService.class);
        Log.d("DEBUG","start server clicked...");
        startService(intent);
    }

    public String getLocalIpAddress() {
        String address= null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
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
}