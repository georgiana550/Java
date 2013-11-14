import java.util.Iterator;
import java.util.Set;


public class BroadcastCommand implements ICommand
{
    public BroadcastCommand(String sfromUser, String sMessage) {	
        this.sUser = sfromUser;
        this.sMessage = sMessage;
    }

    //reciever will call this function
    public void execute( Object o ) {
        // Since this command can get executed
        // on either client or server, we need to
        // act differently
	
        if ( o instanceof ChatServer ) {        	
        	  ((ChatServer)o).broadcast( sMessage, sUser );		
        }       
        else { // must be client
        	 ((ChatClient)o).getMessageArea().append(sUser + ": " + sMessage +"\n");
        }
    }
    
    public String getDescription() {
        return "Message Command: from " + sUser + " to " + sToUser;
    }
    
    private String sUser, sToUser, sMessage;
}


