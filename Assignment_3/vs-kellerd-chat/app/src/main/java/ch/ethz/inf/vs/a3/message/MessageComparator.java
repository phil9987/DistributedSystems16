package ch.ethz.inf.vs.a3.message;

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
        int ret = 0;
        if(lhs.getContent().length() < rhs.getContent().length())
            ret = 1;
        else if (lhs.getContent().length() > rhs.getContent().length())
            ret = 2;
        return ret;
    }

}
