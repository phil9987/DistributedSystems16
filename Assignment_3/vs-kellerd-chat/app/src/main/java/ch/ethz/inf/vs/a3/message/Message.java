package ch.ethz.inf.vs.a3.message;

import ch.ethz.inf.vs.a3.clock.VectorClock;

/**
 * Created by philipjunker on 28.10.16.
 */
public class Message {

    private VectorClock clock;
    private String content;
    public Message(String timestamp, String content){
        this.clock = new VectorClock();
        clock.setClockFromString(timestamp);
        this.content = content;
    }

    public String getContent(){
        return content;
    }
    public VectorClock getClock(){
        return clock;
    }
    public String getTimestamp(){
        return clock.toString();
    }
}
