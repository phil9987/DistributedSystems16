package ch.ethz.inf.vs.a3.clock;

/**
 * Created by philipjunker on 02.11.16.
 */
public class VectorClockComparator implements java.util.Comparator<VectorClock> {
    @Override
    public int compare(VectorClock o1, VectorClock o2) {
        // Returns a negative integer, zero, or a positive integer
        // as the first argument is less than, equal to, or greater than the second
        int result = 0;
        if(o1.happenedBefore(o2)){
            result = -1;
        }else if (o2.happenedBefore(o1)){
            result = 1;
        }
        return result;
    }
}
