package ch.ethz.inf.vs.a2.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by philipjunker on 22.10.16.
 */

public class RawHttpServer implements Runnable{
    ServerSocket serverSocket;
    Thread serverThread;
    int port;
    public static final String SERVER_TAG = "### RawHttpServer ###";

    public void start(int port) {
        this.stop();
        this.port = port;

        try {
            Log.d(SERVER_TAG, "starting server...");
            // bind the server
            serverSocket = new ServerSocket(port);

            Log.d(SERVER_TAG, "server running on port " + this.port);
            // start the server thread
            serverThread = new Thread(this);
            serverThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (serverSocket == null) return;

        try {
            serverSocket.close();
            serverThread.interrupt();
            serverSocket = null;
            serverThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(SERVER_TAG, "server stopped.");
    }

    @Override
    public void run() {
        Socket socket = null;
        while (!Thread.currentThread().isInterrupted()){
            try {
                socket = serverSocket.accept();
                Log.d("DEBUG: ", "incoming connection! starting communication thread!");
                CommunicationThread commThread = new CommunicationThread(socket);
                new Thread(commThread).start();
            }   catch (IOException e){
                //e.printStackTrace();
                //happens when socket is closed, output not needed...
            }

        }

    }

    protected class CommunicationThread implements Runnable{
        private volatile boolean running = true;
        private Socket clientSocket;
        private BufferedReader input;
        private PrintWriter output;


        public CommunicationThread(Socket clientSocket){
            this.clientSocket = clientSocket;
            try{
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
                this.output = new PrintWriter(clientSocket.getOutputStream(),true);     // printwriter with auto-flush
                Log.d("DEBUG: ", "starting communication thread...");
            }   catch (IOException e){
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try{
                String request = input.readLine(); //GET path HTTP/1.1
                String[] requestParam = request.split(" ");
                String path = requestParam[1];
                Log.d("DEBUG request_param", path);
                output.write("HTTP 404");

                input.close();
                output.close();

            }   catch(IOException e){
                e.printStackTrace();
            }
            Log.d("DEBUG", "stopping communication thread.");

        }
    }
}
