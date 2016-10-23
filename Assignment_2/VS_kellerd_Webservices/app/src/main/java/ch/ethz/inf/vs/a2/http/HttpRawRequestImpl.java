package ch.ethz.inf.vs.a2.http;

import java.io.InputStream;
import java.util.Scanner;

import ch.ethz.inf.vs.a2.http.HttpRawRequest;

/**
 * Created by David Keller on 17.10.16.
 * Extended by Philip Junker on 22.10.16.
 */

public class HttpRawRequestImpl implements HttpRawRequest {

    public static final String HTTP_NL = "\r\n";
    public static final String HTTP_SP = " ";
    public static final String HTTP_COL = ":";
    public static final String HTTP_PORT_COL = ":";
    public static final String HTTP_VERSION = "HTTP/1.1";


    @Override
    public String generateRequest(String host, int port, String path) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("GET " + path + " HTTP/1.1\r\n");
        stringBuilder.append("Host: " + host + ":" + port + "\r\n");
        stringBuilder.append("Accept: text/plain\r\n");
        stringBuilder.append("Connection: close\r\n");

        stringBuilder.append("\r\n");
        return stringBuilder.toString();
    }
}
