package ch.ethz.inf.vs.a3.clock;

import android.content.Intent;
import android.util.ArrayMap;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by David Keller on 29.10.16.
 */

public class VectorClock implements Clock {

    private Map<Integer, Integer> vector = new HashMap<>();

    /*
     * Interface methods
     */

    @Override
    public void update(Clock other) {
        VectorClock otherVectorClock = (VectorClock) other;
        Map<Integer, Integer> otherVector = otherVectorClock.getVector();
        for (Map.Entry<Integer, Integer> entry : otherVector.entrySet()) {
            Integer otherPid = entry.getKey();
            Integer otherTime = entry.getValue();
            if(vector.containsKey(otherPid)){
                Integer time = this.getTime(otherPid);
                Integer newTime = time < otherTime ? otherTime : time;
                vector.put(otherPid, newTime);
            }else{
                vector.put(otherPid,otherTime);
            }
        }
    }

    @Override
    public void setClock(Clock other) {
        VectorClock otherVectorClock = (VectorClock) other;
        vector = new HashMap<>(otherVectorClock.getVector());
    }

    @Override
    public void tick(Integer pid) {
        Integer oldTime = vector.get(pid);
        vector.put(pid, oldTime + 1);
    }

    @Override
    public boolean happenedBefore(Clock other) {
        VectorClock otherVectorClock = (VectorClock) other;
        Boolean smalleq = false;
        for (Map.Entry<Integer, Integer> entry : vector.entrySet()) {
            Integer time = entry.getValue();
            Integer otherTime;
            if(otherVectorClock.getVector().containsKey(entry.getKey())) {
                 otherTime = otherVectorClock.getTime(entry.getKey());
            }   else{
                otherTime = time;
            }
            if (otherTime - time >= 0) {
                smalleq = true;
            } else {
                smalleq = false;
                break;
            }
        }
        return smalleq;
    }

    @Override
    public String toString() {
        JSONObject clock = new JSONObject();
        if(vector != null){
            if (!vector.isEmpty()){
                for (Map.Entry<Integer, Integer> entry : vector.entrySet()) {
                    String pid = entry.getKey().toString();
                    int time = entry.getValue();
                    try {
                        clock.put(pid, time);
                    } catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
        }
        return clock.toString();
    }

    @Override
    public void setClockFromString(String clock) {
        Map<Integer, Integer> newVector = new HashMap<>();
        Boolean failed = false;
        JSONObject jsonClock;
        try{
            jsonClock = new JSONObject(clock);
        }   catch (JSONException e){
            jsonClock = new JSONObject();
        }
        if (jsonClock != null){
            Iterator<?> keys = jsonClock.keys();

            while(keys.hasNext() && !failed) {
                String key = (String)keys.next();
                int pid = -1;
                int time = -1;
                try {
                    pid = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    failed = true;
                }
                try {
                    time = jsonClock.getInt(key);
                } catch (JSONException e){
                    failed = true;
                }
                if(!failed){
                    newVector.put(pid,time);
                }
            }
        }else{
            failed = true;
        }

        if(!failed){
            /*if (this.vector != null){       // add elements of newVector to this.vector
                for (Map.Entry<Integer, Integer> entry : newVector.entrySet()) {
                    this.vector.put(entry.getKey(), entry.getValue());
                }
            }else{
                this.vector = newVector;
            }*/
            this.vector = newVector;
        }

    }

    /*
     * Additional methods
     */

    int getTime(Integer pid) {
        return vector.get(pid);
    }

    void addProcess(Integer pid, int time) {
        vector.put(pid, time);
    }

    /*
     * Helper methods
     */

    Map<Integer, Integer> getVector() {
        return vector;
    }
}
