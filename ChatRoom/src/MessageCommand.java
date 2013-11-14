import javax.swing.JTextArea;


public class MessageCommand implements ICommand
{
    public MessageCommand( String sUser, String sToUser, String sMessage) {
        this.sUser = sUser;
        this.sToUser = sToUser;
        this.sMessage = sMessage;
    }
    
    //reciever will call this function
    public void execute( Object o ) {
        // Since this command can get executed
        // on either client or server, we need to
        // act differently
        if ( o instanceof ChatServer ) {
            ((ChatServer)o).send( sMessage, sToUser, sUser );			
        } else { // must be client
        	 ((ChatClient)o).getMessageArea().append(sUser + " whisper to you: " + sMessage +"\n");
        }
    }
    
    public String getDescription() {
        return "Message Command: from " + sUser + " to " + sToUser;
    }
    
    private String sUser, sToUser, sMessage;
}

