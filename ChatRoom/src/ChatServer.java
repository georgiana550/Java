
import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EtchedBorder;

public class ChatServer implements Runnable, ActionListener
{
	//available user and his request.
    private Map<String, RemoteDestination> htActive = new HashMap<String, RemoteDestination>(); 
    //the request waiting for login 
    private Map<InetAddress, RemoteDestination> htPending = new HashMap<InetAddress, RemoteDestination>();
    
    private int iPort;
	private InetAddress clientAddress;
    
	JFrame frame;
	JPanel p;
	JTextArea messageArea;
	JButton clearButton;
	
    public ChatServer( int iPort ) {
        this.iPort = iPort;
        frame = new JFrame();
    	Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        
		p = new JPanel();
		p.setLayout(new BorderLayout());		
		p.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		clearButton = new JButton("clear");
		clearButton.addActionListener(this);
        p.add(clearButton, BorderLayout.EAST);
        contentPane.add(p, BorderLayout.EAST);
    	
        messageArea=new JTextArea();
        messageArea.setLineWrap(true);
        messageArea.setPreferredSize(new Dimension(800,600));
        messageArea.setForeground(new Color(255,255,255));
        messageArea.setBackground(new Color(20,20,20));
        Font font = new Font("Serif", Font.HANGING_BASELINE, 16);
        messageArea.setFont(font);
        
        
        contentPane.add(messageArea, BorderLayout.CENTER);
        
    	frame.pack();				
    	frame.setVisible(true);
    	//frame.setSize(640, 480);

    	
    	frame.addWindowListener(new WindowAdapter(){
    		public void windowClosing(WindowEvent e){
    			System.exit(0);
    		}
    	});
                
    }
    
    public void run() {
        // Create server socket on the specified port
        ServerSocket socket = null;
        try {
            socket = new ServerSocket( iPort,0, InetAddress.getLocalHost() );
        } catch ( IOException e ) {
            messageArea.append("Can't use port " + iPort+"\n");
            System.exit( 0 );
        }
        messageArea.append( "Server Listening: " + socket.getInetAddress() + ":" + socket.getLocalPort()+"\n");
                
        /* welcome socket */
        while ( true ) {
            // Create new Remote Destination
            try {
                // Wait for connection
                // (accept blocks & returns Socket when connection occurs)
            	Socket childSocket;
                RemoteDestination dest = new RemoteDestination( childSocket = socket.accept(), this );
                // Store this in pending destinations
                htPending.put( childSocket.getInetAddress(), dest );             
            } catch ( Exception e ) {
                messageArea.append( e.getMessage()+"\n");
            }
        }
    }
    
    public void login( String sUser, InetAddress address ) {
        // Get one of waiting request 
        RemoteDestination dest = (RemoteDestination)htPending.remove( address );
        if ( dest == null ) {
            messageArea.append( "No destination with address " + address +"\n");
        } else {
            // add into the queue of available user.
            htActive.put( sUser, dest );
            messageArea.append( "User " + sUser + " logged in." + "\n");
        }
    }
    
    public void logout( String sUser ) {
        htActive.remove( sUser );
        messageArea.append( "User " + sUser + " logged out." + "\n");
        broadcast("I am offline. 881~", sUser);
    }
    
    public void send( String sMessage, String sToUser, String sFromUser ) {
        // Lookup destination
        RemoteDestination dest = (RemoteDestination)htActive.get( sToUser );
        MessageCommand cmd = null;
        if ( dest == null ) {
            // Other user is not logged in, notify sender
            dest = (RemoteDestination)htActive.get( sFromUser );
            //wrong formation, chat server will whisper to you.
            if(sToUser.equals("#$!%^&^")) {
            	cmd = new MessageCommand( "chat server", sFromUser,
                        sMessage );
            }
            //toUser not online now.
            else {
            	cmd = new MessageCommand( "chat server", sFromUser,
                                        "User " + sToUser + " is not logged in." );
            }	
        } else {
            cmd = new MessageCommand( sFromUser, sToUser, sMessage );
        }    
        if ( dest != null ) {
            dest.send( cmd );
        	messageArea.append(sFromUser + " send a whisper to "+ sToUser + "\n");
        }
    }
    
    public void broadcast(String sMessage, String sFromUser) {
    	Set<String> onlineList = getOnlineList();
    	String sToUser;
    	ICommand cmd =null;
    	RemoteDestination dest = null;
    	for(Iterator it = onlineList.iterator(); it.hasNext(); ) {
    		sToUser = (String)it.next();
    		dest = (RemoteDestination)htActive.get( sToUser );

            if ( dest != null ) {
            	cmd = new BroadcastCommand(sFromUser, sMessage);
                dest.send( cmd );
            }
    	}
    	messageArea.append(sFromUser + " broadcast say: "+ sMessage + "\n");
    }
     
    public void showAll(String sFromUser) {
    	Set<String> keySet = getOnlineList();
    	Object[] userList = keySet.toArray();
    	ICommand cmd =null;
    	RemoteDestination dest = null;
   		dest = (RemoteDestination)htActive.get( sFromUser );
        if ( dest != null ) {
        	cmd = new ShowAllCommand(sFromUser, userList);
            dest.send( cmd ); 
            messageArea.append(sFromUser + " show all online user." + "\n");
        }
    }

    public Set<String> getOnlineList() {
    	return htActive.keySet();
    }
    
    public void actionPerformed(ActionEvent e){
    	if(e.getSource()== clearButton){
    		messageArea.setText("");
    	}
    }
    
    
    public static void main( String[] args ) {
        (new Thread( new ChatServer( (args.length == 0) ? 1972 : 
                          Integer.parseInt( args[ 0 ] )))).run();                                            
    }
    
}