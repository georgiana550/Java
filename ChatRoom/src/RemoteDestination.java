
import java.io.*;
import java.util.*;
import java.net.*;

// Remote Destination is used by both client and server
// It encapsulates the aschynchrounous nature of communication
// by having threaded sender and receiver inner classes
public class RemoteDestination 
{
    
    private InetAddress address;
    private int port;
    private Sender sender;
    private Receiver receiver;
    private Socket socket;
    private Object oTarget;
    private ObjectOutputStream os;
    private ObjectInputStream is;
    private List lErrors = new ArrayList<Exception>();
	
    // Create RemoteDestination with address, port and target object
    // This constructor would typically be used by clients
    // The target is the object on which commands are invoked when 
    // they arive to this destination
    public RemoteDestination( InetAddress address, int port, Object oTarget ) throws IOException {
    	this( new Socket( address, port ), oTarget );
    }
    
    // Create RemoteDestination with the socket and target object
    // This constructor would typically be used by server
    public RemoteDestination( Socket socket, Object oTarget ) {
        this.socket = socket;
        this.address = socket.getInetAddress();
        this.port = socket.getPort();
        this.oTarget = oTarget;
        
        // Start sender and receiver threads
        sender = new Sender();
        (new Thread( sender )).start();
        receiver = new Receiver();
        (new Thread( receiver )).start();
    }
    
    public void send( ICommand cmd ) {
        sender.send( cmd );
    }
    
    public InetAddress getAddress() {
        return address;
    }
    
    public int getPort() {
        return port;
    }
    
    // Error handling
    // We store all exceptions in a list and offer
    // simple API to access them
    public int getNumberOfErrors() {
        return lErrors.size();
    }
     
    public Exception getError( int i ) {
        return (Exception)lErrors.get( i );
    }
    
    public void clearErrors() {
        //lErrors = new ArrayList();
    	lErrors.clear();
    }
    
    public String toString() {
        return  "" + getAddress() + ":" + getPort();
    }
    
    //write out 
    private ObjectOutputStream getOutputStream() throws IOException {
        if ( socket == null ) {
            socket = new Socket( address, port );
        }
        if ( os == null ) {
            os = new ObjectOutputStream( 
                 new BufferedOutputStream( socket.getOutputStream() ) );
        }
        return os;
    }
    
    //read in
    private ObjectInputStream getInputStream() throws IOException {
        if ( socket == null ) {
            socket = new Socket( address, port );
        }
        if ( is == null ) {
            is = new ObjectInputStream( 
                 new BufferedInputStream( socket.getInputStream() ) );
        }
        return is;
    }
    
    private void addError( Exception e ) {
        lErrors.add( e );
    }
    
    // Inner class for sending messages out
    class Sender implements Runnable {
    
        // When we get the send call, just place the command
        // on the queue and wake up the thread that actually
        // delivers messages. Notice that this method is called
        // by another thread.
        public synchronized void send( ICommand cmd ) {       	
            queue.enqueue( cmd );        
            notifyAll();
        }
        
        public void run() {
            while ( true ) {
                // Wait until there are messages on the queue...
                while ( queue.getNumberPendingMessages() == 0 ) {
                    try {
                        synchronized ( this ) {
                            wait();
                        }
                    } catch ( InterruptedException e ) {
                        addError( e );
                    }
                }
                
                // Now just dequeue message and send it
                try {           	
                    getOutputStream().writeObject( queue.dequeue() );                
                    getOutputStream().flush();             
                } catch ( IOException e ) {
                    System.out.println( "Error sending.. " + e.getMessage() );
                    addError( e );
                } 
            }
        }
        private Queue queue  = new Queue();
    }
    
    
    // Inner class for receiving messages out
    class Receiver implements Runnable {
    
        public void run() {
            while ( true ) {
                
                try {
                    // this will block until command arrives
                    ICommand cmd = (ICommand)getInputStream().readObject();
                    if ( cmd != null ) {
                        // Just run the command on the target
                        cmd.execute( oTarget );
                    }
                } catch ( Exception e ) {
                    System.out.println( "Error receiving.. " + e.getMessage() );
                    addError( e );
                    break;
                } 
            }
        }
    }
   
   
    protected void finalize() throws Throwable {
        try {
            if ( socket != null ) socket.close();
            if ( os != null )     os.close();
            if ( is != null )     is.close();
        } catch ( Exception e ) {}
        super.finalize();
        
    }
            

}