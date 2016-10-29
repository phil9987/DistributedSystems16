package ch.ethz.inf.vs.a3.message;

import android.util.Log;

import java.util.Comparator;
//import ch.ethz.inf.vs.a3.solution.message.Message;

/**
 * Message comparator class. Use with PriorityQueue.
 */
public class MessageComparator implements Comparator<Message> {

    @Override
    public int compare(Message lhs, Message rhs) {
        // Returns a negative integer, zero, or a positive integer
        // as the first argument is less than, equal to, or greater than the second
        int i1, i2;
        i1 = Integer.parseInt(lhs.getContent().substring(lhs.getContent().length()-1));
        i2 = Integer.parseInt(rhs.getContent().substring(rhs.getContent().length()-1));
        //Log.d("DEBUG", "i1= " + i1 + " i2= " + i2);
        return i1 - i2;
    }

}
