package ch.ethz.inf.vs.a3.kellerd.chat;

//https://kb.vmware.com/selfservice/microsites/search.do?language=en_US&cmd=displayKC&externalId=2006955

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
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.NetworkConsts;

public class MainActivity extends AppCompatActivity {
    private static final String MAINACTIVITY_TAG = "MAINACTIVITY";
    private EditText mUsername_field;
    private int mPort;
    private String mServerAddress;
    private DatagramSocket socket;
    private UUID mUUID;

    public SharedPreferences sharedPreferences;
    private String username;

    private boolean registered = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(MAINACTIVITY_TAG, "start main activity");

        mUsername_field = (EditText) findViewById(R.id.username_field);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mServerAddress = NetworkConsts.SERVER_ADDRESS;
        mPort = NetworkConsts.UDP_PORT;

        mUUID = UUID.randomUUID();
    }

    public void onJoinButtonClick(View view) {
        username = mUsername_field.getText().toString();
        RegistrationThread registrationThread = new RegistrationThread();
        registrationThread.execute(username);
    }

    public void onSettingsButtonClick(View view) {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        this.startActivity(settingsIntent);
    }

    public class RegistrationThread extends AsyncTask<String, Integer, Boolean> {
        private final String REGISTRATION_TAG = "Registration Thread";

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(REGISTRATION_TAG, "started AsyncTask!");

            String userName = params[0];
            int registration_attempts = 0;

            do {
                register(userName);
                registration_attempts++;
            } while (!registered && registration_attempts < 5);

            return registered;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (registered){
                Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                chatIntent.putExtra("username", username);
                chatIntent.putExtra("uuid", mUUID.toString());
                startActivity(chatIntent);
            }
            else {
                Toast.makeText(getApplicationContext(),R.string.registration_fail, Toast.LENGTH_SHORT).show();
            }
        }

        private void register(String username){
            JSONObject messageJson = new JSONObject();
            JSONObject messageHdr = new JSONObject();
            JSONObject messageBody = new JSONObject();

            mServerAddress = sharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, mServerAddress);
            mPort = Integer.valueOf(sharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_PORT, Integer.toString(mPort)));

            try {
                messageHdr.put("username", username);
                messageHdr.put("uuid", mUUID.toString());
                messageHdr.put("timestamp", "{}");
                messageHdr.put("type", MessageTypes.REGISTER);

                messageJson.put("header", messageHdr);
                messageJson.put("body", messageBody); // Body is empty for registration

                socket = new DatagramSocket();

                Log.d(REGISTRATION_TAG, "recipient address: " + (mServerAddress) + ":" + mPort);
                InetAddress address = InetAddress.getByName(mServerAddress);
                byte[] message = messageJson.toString().getBytes(StandardCharsets.UTF_8);

                Log.d(REGISTRATION_TAG, "messageLength = " + message.length);
                DatagramPacket packet = new DatagramPacket(message, message.length, address, mPort);

                Log.d(REGISTRATION_TAG, "send data: " + messageJson);
                socket.send(packet);
                socket.setSoTimeout(NetworkConsts.SOCKET_TIMEOUT);

                byte[] response = new byte[NetworkConsts.PAYLOAD_SIZE];
                DatagramPacket responsePacket = new DatagramPacket(response, response.length);
                socket.receive(responsePacket);

                String responseString = new String(responsePacket.getData());
                Log.d(REGISTRATION_TAG, "response: " + responseString);

                JSONObject responseMessage = new JSONObject(responseString);
                if (responseMessage.getJSONObject("header").get("type").equals("ack")){
                    registered = true;
                }
            }
            catch (IOException | JSONException e){
//                e.printStackTrace();
//              Log.d(Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT);
            }
        }
    }
}
