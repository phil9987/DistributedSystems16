package ch.ethz.inf.vs.a3.clock;

import android.content.Intent;
import android.util.ArrayMap;

import java.util.HashMap;
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
        for (Map.Entry<Integer, Integer> entry : vector.entrySet()) {
            Integer time = entry.getValue();
            Integer otherTime = otherVectorClock.getTime(entry.getKey());
            Integer newTime = time < otherTime ? otherTime : time;
            entry.setValue(newTime);
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
        return false;
    }

    @Override
    public String toString() {
        // TODO: map to json string
        return "";
    }

    @Override
    public void setClockFromString(String clock) {
        // TODO: parse form json string
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
