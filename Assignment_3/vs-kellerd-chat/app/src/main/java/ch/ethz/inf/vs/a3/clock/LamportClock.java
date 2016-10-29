package ch.ethz.inf.vs.a3.clock;

/**
 * Created by David Keller on 28.10.16.
 */

public class LamportClock implements Clock {

    private int time;

    /*
     * Interface methods
     */
    @Override
    public void update(Clock other) {
        LamportClock otherLamportClock = (LamportClock) other;
        time = other.happenedBefore(this) ? time : otherLamportClock.getTime();
    }

    @Override
    public void setClock(Clock other) {
        LamportClock otherLamportClock = (LamportClock) other;
        this.setTime(otherLamportClock.getTime());
    }

    @Override
    public void tick(Integer pid) {
        this.setTime(this.getTime() + 1);
    }

    @Override
    public boolean happenedBefore(Clock other) {
        LamportClock otherLamportClock = (LamportClock) other;
        return this.time < otherLamportClock.getTime();
    }

    @Override
    public String toString() {
        return Integer.toString(this.getTime());
    }

    @Override
    public void setClockFromString(String clock) {
        Integer newTime;

        try {
            newTime = Integer.parseInt(clock);
        } catch (NumberFormatException e) {
            return;
        }

        if (newTime != null) {
            this.setTime(Integer.parseInt(clock));
        }
    }

    /*
     * Additional methods
     */

    void setTime(int time) {
        this.time = time;
    }

    int getTime() {
        return time;
    }
}
