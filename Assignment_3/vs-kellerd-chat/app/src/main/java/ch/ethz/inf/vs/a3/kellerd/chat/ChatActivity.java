package ch.ethz.inf.vs.a3.kellerd.chat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Map;
import ch.ethz.inf.vs.a3.queue.PriorityQueue;
import java.util.UUID;

import ch.ethz.inf.vs.a3.message.Message;
import ch.ethz.inf.vs.a3.message.MessageComparator;
import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.NetworkConsts;

public class ChatActivity extends AppCompatActivity {

    private final String CHATACTIVITY_TAG = "Chat Activity";

    private JSONObject messageJson, messageHdr;
    private String mServerAddress, mPort;
    private SharedPreferences mSharedPreferences;
    private String username;
    private DatagramSocket socket;
    private UUID mUUID;
    private PriorityQueue<Message> msgPriorityQueue;
    private Comparator<Message> msgComparator;
    public TextView chatlog;

    private boolean deregistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        username = getIntent().getStringExtra("username");
        mUUID = UUID.fromString(getIntent().getStringExtra("uuid"));

        msgComparator = new MessageComparator();
        msgPriorityQueue = new PriorityQueue<Message>(msgComparator);

        chatlog = (TextView) findViewById(R.id.textViewChatLog);
    }

    @Override
    protected void onDestroy() {
        if (!deregistered) {
            new DeregistrationThread().execute(username);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!deregistered) {
            new DeregistrationThread().execute(username);
        }
        super.onBackPressed();
    }
    public void onGetChatLogClick(View v){
        ChatLogThread chatlog = new ChatLogThread();
        chatlog.execute(username);
    }

    public class ChatLogThread extends AsyncTask<String, Integer, Boolean> {
        private final String CHATLOG_TAG = "ChatLog Request Thread";

        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(CHATLOG_TAG, "started AsyncTask!");

            String userName = params[0];
            Boolean requestSuccessful = false;
            int request_attempts = 0;

            do {
                requestSuccessful = request_chat_log(userName);
                request_attempts++;
            } while (!requestSuccessful && request_attempts < 5);

            Log.d(CHATLOG_TAG, "request-result: " + requestSuccessful);
            return requestSuccessful;
        }

        private Boolean request_chat_log(String username){
            Boolean result = false;

            messageJson = new JSONObject();
            messageHdr = new JSONObject();

            mServerAddress = mSharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, mServerAddress);
            mPort = mSharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_PORT, mPort);

            try {
                messageHdr.put("username", username);
                messageHdr.put("uuid", mUUID.toString());
                messageHdr.put("timestamp", "{}");
                messageHdr.put("type", MessageTypes.RETRIEVE_CHAT_LOG);

                JSONObject messageBody = new JSONObject();
                messageJson.put("header", messageHdr);
                messageJson.put("body", messageBody);

            }   catch(JSONException je){
                je.printStackTrace();
            }

            try {
                socket = new DatagramSocket();

                Log.d(CHATLOG_TAG, "recipient address: " + (mServerAddress) + ":" + mPort);
                InetAddress address = InetAddress.getByName(mServerAddress);

                byte[] message = messageJson.toString().getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(message, message.length, address, Integer.valueOf(mPort));

                Log.d(CHATLOG_TAG, "send data: " + messageJson);
                socket.send(packet);

            }   catch(IOException e){
                Log.e(CHATLOG_TAG, "send error: ");
                e.printStackTrace();
            }

            try {
                socket.setSoTimeout(NetworkConsts.SOCKET_TIMEOUT / 10);

                byte[] response = new byte[NetworkConsts.PAYLOAD_SIZE];

                while(!result) {
                    DatagramPacket responsePacket = new DatagramPacket(response, response.length);
                    socket.receive(responsePacket);

                    String responseString = new String(responsePacket.getData());
                    //Log.d(CHATLOG_TAG, "response: " + responseString);

                    JSONObject responseMessage = new JSONObject(responseString);
                    Message msg = new Message(responseMessage.getJSONObject("header").get("timestamp").toString(),
                            responseMessage.getJSONObject("body").get("content").toString());

                    //Log.d(CHATLOG_TAG, msg.getContent());
                    msgPriorityQueue.add(msg);
                    if (!responseMessage.getJSONObject("header").get("type").equals("message")) {
                        result = true;
                    }
                }
            }   catch(IOException | JSONException e){
                result = true;
                Log.d(CHATLOG_TAG, "receive timeout occurred.");
            }

            return msgPriorityQueue.size() > 0;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            int i = 0;
            String chatlogString = "";
            while(!msgPriorityQueue.isEmpty()){
                Message m = msgPriorityQueue.poll();
                chatlogString += m.getContent() + "\n";
                //Log.d(CHATACTIVITY_TAG, "element " + i++ + " " +  m.getContent());
            }
            chatlog.setText(chatlogString);
            super.onPostExecute(aBoolean);
        }
    }


    public class DeregistrationThread extends AsyncTask<String, Integer, Boolean> {
        private final String REGISTRATION_TAG = "Deregistration Thread";
        @Override
        protected Boolean doInBackground(String... params) {
            Log.d(REGISTRATION_TAG, "started AsyncTask!");

            String userName = params[0];
            int deregistration_attempts = 0;

            do {
                deregister(userName);
                deregistration_attempts++;
            } while (!deregistered && deregistration_attempts < 5);

            return deregistered;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (deregistered) {
                Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(mainIntent);
            }
            else {
                Toast.makeText(getApplicationContext(),R.string.deregistration_fail, Toast.LENGTH_SHORT).show();
            }
        }

        private void deregister(String username){
            messageJson = new JSONObject();
            messageHdr = new JSONObject();

            mServerAddress = mSharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_ADDRESS, mServerAddress);
            mPort = mSharedPreferences.getString(SettingsActivity.KEY_PREF_SERVER_PORT, mPort);

            try {
                messageHdr.put("username", username);
                messageHdr.put("uuid", mUUID.toString());
                messageHdr.put("timestamp", "{}");
                messageHdr.put("type", MessageTypes.DEREGISTER);

                JSONObject messageBody = new JSONObject();
                messageJson.put("header", messageHdr);
                messageJson.put("body", messageBody);

                socket = new DatagramSocket();

                Log.d(REGISTRATION_TAG, "recipient address: " + (mServerAddress) + ":" + mPort);
                InetAddress address = InetAddress.getByName(mServerAddress);

                byte[] message = messageJson.toString().getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(message, message.length, address, Integer.valueOf(mPort));
                Log.d(REGISTRATION_TAG, "send data: " + messageJson);

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

        }
    }
}
