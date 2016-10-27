package ch.ethz.inf.vs.a3.kellerd.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.NetworkConsts;

public class MainActivity extends AppCompatActivity {
    private static final String MAINACTIVITY_TAG = "MAINACTIVITY";
    private SharedPreferences mSharedPreferences;
    private EditText mUsername_field;
    private String mPort;
    private String mServerAddress;
    private DatagramSocket socket;
    private JSONObject messageJson;
    private JSONObject messageHdr;
    private UUID mUUID;

    private boolean registered = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUsername_field = (EditText) findViewById(R.id.username_field);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mUUID = UUID.randomUUID();


    }

    public void onJoinButtonClick(View view) {
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

            Map prefMap = mSharedPreferences.getAll();
            mServerAddress = (String) prefMap.get("server_address_preference");
            mPort = (String) prefMap.get("server_port_preference");
           // mServerAddress = mSharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, mServerAddress);
           // mPort = mSharedPreferences.getInt(SettingsActivity.KEY_PREF_SERVER_PORT, Integer.valueOf(mPort));

            try {
               messageHdr.put("username", userName);
                messageHdr.put("uuid", mUUID.toString());
                messageHdr.put("timestamp", "{}");
                messageHdr.put("type", MessageTypes.REGISTER);
                messageJson.put("header", messageHdr);
                messageJson.put("body", "{}");
                socket = new DatagramSocket();
                Log.d(ACITIVTY_TAG, (mServerAddress) + ":" + mPort);
                InetAddress address = InetAddress.getByName(mServerAddress);
                int messageLength = messageJson.length();
                byte[] message = messageJson.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(message, messageLength, address, Integer.valueOf(mPort));
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
