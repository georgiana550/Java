
import java.util.*;

public class LogoutCommand implements ICommand
{
    public LogoutCommand( String sUser ) {
        this.sUser = sUser;
    }
    
    public String getUser()       { return sUser; }
    
    public void execute( Object o ) throws Exception {
        if ( ! (o instanceof ChatServer) ) {
            throw new Exception( "LogoutCommand can only be executed on ChatServer");
        }
        ((ChatServer)o).logout( sUser );
    }
    
    public String getDescription() {
        return "Logout Command, user = " + sUser;
    }
    
    private String sUser;
}
