package ch.ethz.inf.vs.a2.http;

import ch.ethz.inf.vs.a2.http.HttpRawRequest;

/**
 * Created by David Keller on 17.10.16.
 */

public class HttpRawRequestImpl implements HttpRawRequest {
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
