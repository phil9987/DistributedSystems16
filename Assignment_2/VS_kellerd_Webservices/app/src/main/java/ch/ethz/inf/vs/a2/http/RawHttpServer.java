package ch.ethz.inf.vs.a2.http;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by philipjunker on 22.10.16.
 */

public class RawHttpServer implements Runnable{
    ServerSocket serverSocket;
    Thread serverThread;
    HttpRequestHandler handler;
    int port;
    public static final String SERVER_TAG = "### RawHttpServer ###";

    public void start(int port, HttpRequestHandler handler) {
        this.stop();
        this.handler = handler;
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
                Log.d(SERVER_TAG, "incoming connection! starting communication thread!");
                socket.setSoTimeout(100);
                CommunicationThread commThread = new CommunicationThread(socket, handler);
                new Thread(commThread).start();
            }   catch (IOException e){
                //e.printStackTrace();
                //happens when socket is closed, output not needed...
            }

        }

    }

    protected class CommunicationThread implements Runnable{
        private BufferedReader input;
        private PrintWriter output;
        protected HttpRequestHandler handler;


        public CommunicationThread(Socket clientSocket, HttpRequestHandler handler){
            try{
                this.handler = handler;
                this.input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.output = new PrintWriter(clientSocket.getOutputStream(),true);     // printwriter with auto-flush
                Log.d(SERVER_TAG, "starting communication thread...");
            }   catch (IOException e){
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            Boolean end;
            String nextLine;
            String path = "";
            String[] requestParam;
            String accept = "";
            String request;
            try {
                request = input.readLine(); //GET path HTTP/1.1
                if (request != null) {
                    requestParam = request.split(" ");
                    path = requestParam[1];
                }
                end = false;
            } catch(IOException e) {
                end = true;
                request = "";
            }

                while (!end) {
                    try {
                        nextLine = input.readLine();
                        Log.d(SERVER_TAG, nextLine);
                        if (nextLine == null) {
                            end = true;
                            Log.d(SERVER_TAG, "request ended!");
                        } else {
                            request += "\r\n" + nextLine;
                            requestParam = nextLine.split(" ");
                            int index = 0;
                            for (String a : requestParam) {
                                Log.d(SERVER_TAG, "el" + index++ + ": " + a.trim());
                            }
                            if (requestParam[0].toLowerCase().equals("accept:")) {
                                accept = requestParam[1].split(",")[0];
                                Log.d(SERVER_TAG, "accept-encoding detected: " + accept);
                            }

                        }
                    } catch (IOException e) {
                        Log.d(SERVER_TAG, "readline timeout occured, stopping communication thread");
                        end = true;
                    }
                }
            try{
                Log.d(SERVER_TAG, path);
                output.write(handler.handle(path, accept));
                output.flush();
                input.close();
                output.close();
            } catch(IOException e){
                e.printStackTrace();
            }
            Log.d(SERVER_TAG, "stopping communication thread.");

        }
    }
}
