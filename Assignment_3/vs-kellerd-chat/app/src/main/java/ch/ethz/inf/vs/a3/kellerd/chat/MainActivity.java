package ch.ethz.inf.vs.a3.kellerd.chat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.NetworkConsts;

public class MainActivity extends AppCompatActivity {
    private static final String MAINACTIVITY_TAG = "MAINACTIVITY";
    private SharedPreferences mSharedPrefences;
    private EditText mUsername_field;
    private int mPort = 4446;
    private String mServerAddress = "10.2.141.45";
    private DatagramSocket socket;
    private JSONObject messageJson;
    private JSONObject messageHdr;
    private boolean registered = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUsername_field = (EditText) findViewById(R.id.username_field);


    }

    public void onJoinButtonClick(View view) {
        //TODO generate UUID

        String userName = mUsername_field.getText().toString();
        RegistrationThread registrationThread = new RegistrationThread();
        registrationThread.execute(userName);
    }

    public class RegistrationThread extends AsyncTask<String, Integer, Boolean>{
        private final String ACITIVTY_TAG = "Registration Thread";
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(MAINACTIVITY_TAG, "started AsyncTask!");
            boolean success = false;
            String userName = params[0];
            for (int registration_attemps = 0; registration_attemps<5; registration_attemps++){
                success = register(userName);
                if (success){
                    break;
                }
            }
            return success;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result){
                Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                startActivity(chatIntent);
            }
            else {
                Toast.makeText(getApplicationContext(),R.string.registration_fail, Toast.LENGTH_SHORT);
            }
        }

        protected Boolean register(String username){
            messageJson = new JSONObject();
            messageHdr = new JSONObject();
            String userName = username;


            mSharedPrefences = getSharedPreferences("pref_general", Context.MODE_PRIVATE);
            mServerAddress = mSharedPrefences.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, mServerAddress);
            mPort = mSharedPrefences.getInt(SettingsActivity.KEY_PREF_SERVER_PORT, mPort);

            try {
                messageHdr.put("username", userName);
                //TODO set generated UUID
                messageHdr.put("uuid", "ae4e15ff-b589-4e85-a07c-594b16e4e645");
                messageHdr.put("timestamp", "{}");
                messageHdr.put("type", MessageTypes.REGISTER);
                messageJson.put("header", messageHdr);
                messageJson.put("body", null);
                socket = new DatagramSocket();
                Log.d(ACITIVTY_TAG, (mServerAddress) + ":" + Integer.toString(mPort));
                InetAddress address = InetAddress.getByName(mServerAddress);
                int messageLength = messageJson.length();
                byte[] message = messageJson.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(message, messageLength, address, mPort);
                socket.send(packet);
                socket.setSoTimeout(NetworkConsts.SOCKET_TIMEOUT);

                byte[] response = new byte[NetworkConsts.PAYLOAD_SIZE];
                DatagramPacket responsePacket = new DatagramPacket(response,response.length);
                socket.receive(responsePacket);
                String responseString = new String(responsePacket.getData());
                Log.d(ACITIVTY_TAG, responseString);
                JSONObject responseMessage = new JSONObject(responseString);
                if (responseMessage.getJSONObject("header").get("type").equals("ack")){
                    return  true;
                }
            }
            catch (IOException | JSONException e){
                e.printStackTrace();
//              Log.d(Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
            }
            return false;
        }
    }

    public void onSettingsButtonClick(View view) {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        this.startActivity(settingsIntent);
    }
}
