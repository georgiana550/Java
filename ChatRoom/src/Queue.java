
import java.util.*;

public class Queue
{
    private LinkedList list = new LinkedList();
    
    public synchronized void enqueue( Object o ) {
        list.addLast( o );
    }
    
     public int getNumberPendingMessages() {
        return list.size();
     }
     
     public synchronized Object dequeue() {
        return list.removeFirst();
    }
    

}