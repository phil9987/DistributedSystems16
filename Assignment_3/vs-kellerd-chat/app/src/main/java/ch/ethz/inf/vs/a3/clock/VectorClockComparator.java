package ch.ethz.inf.vs.a3.clock;

import java.util.Comparator;

import ch.ethz.inf.vs.a3.clock.VectorClock;


public class VectorClockComparator implements Comparator<VectorClock> {

    @Override
    public int compare(VectorClock lhs, VectorClock rhs) {
        // Returns a negative integer, zero, or a positive integer
        // as the first argument is less than, equal to, or greater than the second
        int result = 0;
        if(lhs.happenedBefore(rhs)){
            result = -1;
        }else if (rhs.happenedBefore(lhs)){
            result = 1;
        }
        return result;
    }
}
