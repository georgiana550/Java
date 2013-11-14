import java.util.Iterator;
import java.util.Set;


public class ShowAllCommand implements ICommand
{
	Object[] userList;
    public ShowAllCommand(String sfromUser, Object[] userList) {	
        this.sUser = sfromUser;
        this.userList = userList;
    }

    //reciever will call this function
    public void execute( Object o ) {
        // Since this command can get executed
        // on either client or server, we need to
        // act differently
	
        if ( o instanceof ChatServer ) { 	
        	  ((ChatServer)o).showAll(sUser);		
        }       
        else { // must be client
        	((ChatClient)o).getMessageArea().append("**************  online people: ************** \n");
        	for(int i =0; i < userList.length; i++) {
        		((ChatClient)o).getMessageArea().append( (String)userList[i] +" ");
        	}
        	((ChatClient)o).getMessageArea().append("\n");
        }
    }
    
    public String getDescription() {
        return "Message Command: from " + sUser + " to " + sToUser;
    }
    
    private String sUser, sToUser, sMessage;
}


