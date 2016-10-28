package ch.ethz.inf.vs.a3.kellerd.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.NetworkConsts;

public class ChatActivity extends AppCompatActivity {
    private boolean deregistered = false;
    private JSONObject messageJson, messageHdr;
    private String mServerAddress, mPort;
    private SharedPreferences mSharedPreferences;
    private String username;
    private DatagramSocket socket;
    private UUID mUUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = getIntent().getStringExtra("username");
        mUUID = UUID.fromString(getIntent().getStringExtra("uuid"));
    }

    @Override
    public void onBackPressed() {
        new DeregistrationThread().execute(username);
      //  super.onBackPressed();
    }

    public class DeregistrationThread extends AsyncTask<String, Integer, Boolean> {
        private final String REGISTRATION_TAG = "Deregistration Thread";
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(REGISTRATION_TAG, "started AsyncTask!");
            String userName = params[0];
            deregister(userName);
            for (int deregistration_attempts = 1; deregistration_attempts<5; deregistration_attempts++){
                if (deregistered){
                    break;
                }
                else deregister(userName);
            }
            return deregistered;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (deregistered){
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
            }
            else {
                Toast.makeText(getApplicationContext(),R.string.deregistration_fail, Toast.LENGTH_SHORT);
            }
        }

        protected Boolean deregister(String username){
            messageJson = new JSONObject();
            messageHdr = new JSONObject();
            String userName = username;

            Map prefMap = mSharedPreferences.getAll();
            //mServerAddress = (String) prefMap.get("server_address_preference");
            //mPort = (String) prefMap.get("server_port_preference");
            mServerAddress = mSharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, mServerAddress);
            mPort = mSharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_PORT, mPort);

            try {
                messageHdr.put("username", userName);
                messageHdr.put("uuid", mUUID.toString());
                messageHdr.put("timestamp", "{}");
                messageHdr.put("type", MessageTypes.DEREGISTER);
                JSONObject messageBody = new JSONObject();
                messageJson.put("header", messageHdr);
                messageJson.put("body", messageBody);
                socket = new DatagramSocket();
                Log.d(REGISTRATION_TAG, "recipient address: " + (mServerAddress) + ":" + mPort);
                InetAddress address = InetAddress.getByName(mServerAddress);
                int messageLength = messageJson.length();
                Log.d(REGISTRATION_TAG, "messageLength = " + messageLength);
                byte[] message = messageJson.toString().getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(message, message.length, address, Integer.valueOf(mPort));
                Log.d(REGISTRATION_TAG, "data sent: " + messageJson);
                socket.send(packet);
                socket.setSoTimeout(NetworkConsts.SOCKET_TIMEOUT);

                byte[] response = new byte[NetworkConsts.PAYLOAD_SIZE];
                DatagramPacket responsePacket = new DatagramPacket(response,response.length);
                socket.receive(responsePacket);
                String responseString = new String(responsePacket.getData());
                Log.d(REGISTRATION_TAG, "response: " + responseString);
                JSONObject responseMessage = new JSONObject(responseString);
                if (responseMessage.getJSONObject("header").get("type").equals("ack")){
                    deregistered = true;
                }
            }
            catch (IOException | JSONException e){
//                e.printStackTrace();
//              Log.d(Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
            }
            finally {
                return deregistered ;
            }

        }
    }
}
