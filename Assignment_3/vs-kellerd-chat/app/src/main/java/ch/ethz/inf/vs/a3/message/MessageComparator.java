package ch.ethz.inf.vs.a3.message;

import android.util.Log;

import java.util.Comparator;

import ch.ethz.inf.vs.a3.clock.VectorClockComparator;
//import ch.ethz.inf.vs.a3.solution.message.Message;

/**
 * Message comparator class. Use with PriorityQueue.
 */
public class MessageComparator implements Comparator<Message> {

    @Override
    public int compare(Message lhs, Message rhs) {
        VectorClockComparator vectorClockComparator = new VectorClockComparator();
        return vectorClockComparator.compare(lhs.getClock(), rhs.getClock());
    }

}
