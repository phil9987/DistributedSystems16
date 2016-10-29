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
import java.util.PriorityQueue;
import java.util.UUID;

import ch.ethz.inf.vs.a3.message.Message;
import ch.ethz.inf.vs.a3.message.MessageComparator;
import ch.ethz.inf.vs.a3.message.MessageTypes;
import ch.ethz.inf.vs.a3.udpclient.NetworkConsts;

public class ChatActivity extends AppCompatActivity {
    private final String CHATACTIVITY_TAG = "Chat Activity";
    private boolean deregistered = false;
    private JSONObject messageJson, messageHdr;
    private String mServerAddress, mPort;
    private SharedPreferences mSharedPreferences;
    private String username;
    private DatagramSocket socket;
    private UUID mUUID;
    private PriorityQueue<Message> msgPriorityQueue;
    private Comparator<Message> msgComparator;
    public TextView chatlog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        username = getIntent().getStringExtra("username");
        mUUID = UUID.fromString(getIntent().getStringExtra("uuid"));
        msgComparator = new MessageComparator();
        msgPriorityQueue = new PriorityQueue<Message>(25, msgComparator);
        chatlog = (TextView) findViewById(R.id.textViewChatLog);
    }

    @Override
    protected void onDestroy() {
        new DeregistrationThread().execute(username);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        new DeregistrationThread().execute(username);
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
            Boolean requestResult = request_chat_log(userName);
            for (int request_attmpts = 1; request_attmpts<5; request_attmpts++){
                if (requestResult){
                    break;
                }
                requestResult = request_chat_log(userName);
            }
            Log.d(CHATLOG_TAG, "request-result: " + requestResult);
            return requestResult;
        }

        protected Boolean request_chat_log(String username){
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
                int messageLength = messageJson.length();
                Log.d(CHATLOG_TAG, "messageLength = " + messageLength);
                byte[] message = messageJson.toString().getBytes(StandardCharsets.UTF_8);
                DatagramPacket packet = new DatagramPacket(message, message.length, address, Integer.valueOf(mPort));
                Log.d(CHATLOG_TAG, "data sent: " + messageJson);
                socket.send(packet);
            }   catch(IOException e){
                Log.e(CHATLOG_TAG, "send error: ");
                e.printStackTrace();
            }
            try {
                socket.setSoTimeout(NetworkConsts.SOCKET_TIMEOUT);
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
                Log.d(CHATLOG_TAG, "receive timeout occurred.");
                result = true;
            }

            return result ;

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            int i = 0;
            String chatlogString = "";
            while(!msgPriorityQueue.isEmpty()){
                Message m = msgPriorityQueue.remove();
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
