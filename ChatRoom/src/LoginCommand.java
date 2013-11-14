import java.net.InetAddress;

public class LoginCommand implements ICommand
{
    public LoginCommand( String sUser, InetAddress address ) {
        this.sUser = sUser;
        this.address = address;
    }
    
    public void execute( Object o ) throws Exception {
        if ( ! (o instanceof ChatServer) ) {
            throw new Exception( "LoginCommand can only be executed on ChatServer");
        }
        // Simply call login method on chat server
        ((ChatServer)o).login( sUser, address );
    }
    
    public String getDescription() {
        return "Login Command: user is " + sUser + " address is " + address;
    }
    
    private String sUser;
    private InetAddress address;
}

