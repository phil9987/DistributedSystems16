package ch.ethz.inf.vs.a3.message;

/**
 * Created by philipjunker on 28.10.16.
 */
public class Message {

    private String timestamp;
    private String content;
    public Message(String timestamp, String content){
        this.timestamp = timestamp;
        this.content = content;
    }

    public String getContent(){
        return content;
    }

    public String getTimestamp(){
        return timestamp;
    }
}
