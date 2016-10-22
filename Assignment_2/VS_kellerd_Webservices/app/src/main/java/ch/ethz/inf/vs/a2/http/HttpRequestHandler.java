package ch.ethz.inf.vs.a2.http;

/**
 * Created by philipjunker on 22.10.16.
 */

public interface HttpRequestHandler {
    public String handle(String path, String request);
}
