
import java.io.Serializable;

// Interface for serializable commands which are send between
// client and server and facilitate communication
public interface ICommand extends Serializable
{
    // this method is invoked once command arrives
    // on its destination
    public void execute( Object oTarget ) throws Exception;
    public String getDescription();
}