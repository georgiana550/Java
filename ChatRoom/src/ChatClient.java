
import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.net.*;
import java.util.*;


import javax.swing.BorderFactory;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import javax.swing.ScrollPaneConstants;
import javax.swing.border.EtchedBorder;



public class ChatClient  implements ActionListener
{
    private RemoteDestination destination;					//client's windows
    private String sUser, sHost, sPort;
    
	JFrame frame;
	JPanel connectPane;
    JScrollPane typeAreaPane, messagePane;
    
	JTextArea messageArea, typeArea;
	JTextField userTField, hostTField, portTField;
    JButton connect; 
    
	boolean isConnect=false;
   
	private ChatClient() {
	        frame = new JFrame();
	    	Container contentPane = frame.getContentPane();
	        contentPane.setLayout(new BorderLayout());
	        

	        /**************************************************
	         * 		Connect Area		
	         * ************************************************/
			connectPane = new JPanel();
			connectPane.setLayout(new FlowLayout(FlowLayout.LEFT));		
	        
			userTField = new JTextField("redbug",15);
	    	hostTField = new JTextField("localhost", 20);
	    	portTField = new JTextField("1972",10);
			
	    	JLabel argName = new JLabel("User:");
		    connectPane.add(argName);
		    connectPane.add(userTField);
	    	
		    argName = new JLabel("Host:");
		    connectPane.add(argName);
		    connectPane.add(hostTField);
		    
		    argName = new JLabel("Port:");
		    connectPane.add(argName);
		    connectPane.add(portTField);
		    
		    connect = new JButton("Connect"); 
		    connect.addActionListener(this);
		    
			connectPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	        connectPane.add(connect);
	        
	        contentPane.add(connectPane, BorderLayout.NORTH);
	 
	        
	        /**************************************************
	         * 		Message Area		
	         * ************************************************/
	        messageArea=new JTextArea();
	        messageArea.setLineWrap(true);
	        messageArea.setEditable(false);
	        messageArea.setForeground(new Color(77,222,35));
	        messageArea.setBackground(new Color(20,20,20));
	        Font font = new Font("Serif", Font.ITALIC, 16);
	        messageArea.setFont(font);
	        
	        messagePane = new JScrollPane(messageArea);
	        messagePane.setPreferredSize(new Dimension(400,300));
	        messagePane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	        messagePane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			messagePane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			messagePane.setPreferredSize(new Dimension(600,400));
			
	        contentPane.add(messagePane, BorderLayout.CENTER);
	        
	        
	
	        /**************************************************
	         * 		Typing Area		
	         * ************************************************/
	        typeArea=new JTextArea();
	        typeArea.setLineWrap(true);
	        typeArea.setEnabled(false);
	        
	        
	       // mapping the enter key to the action of clear the typeArea. 
	        InputMap inputMap = typeArea.getInputMap();
	        ActionMap actionMap =typeArea.getActionMap();

	        Object transferTextActionKey = "TRANSFER_TEXT";
	        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0),transferTextActionKey);
	        actionMap.put(transferTextActionKey,new AbstractAction()
	        {
				public void actionPerformed(ActionEvent e)
		          {
		             typeArea.setText("");
		             typeArea.requestFocus();
		          }
	          
	        });
	        
	        
	        typeArea.addKeyListener(new KeyAdapter(){
				public void keyPressed(KeyEvent e){
					//enter to send message.
					if(e.getKeyCode() == KeyEvent.VK_ENTER){
						if (!e.isShiftDown()){
							try{
								destination.send( readCommand( typeArea.getText()));						
					        } catch ( Exception ex ) {
					            System.out.println( "Client Error: " + ex.getMessage() );
					        } finally {				            
					        }
						}
						//shift + enter = move to next line.
						else{
							typeArea.append("\n");
						}
					}
				}
			});
	        
	        
	        typeAreaPane = new JScrollPane(typeArea); 
	        typeAreaPane.setPreferredSize(new Dimension(600,150));
	        typeAreaPane.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
	        font = new Font("Serif", Font.TRUETYPE_FONT, 20);
	        typeArea.setFont(font);
	        
	        contentPane.add(typeAreaPane, BorderLayout.SOUTH);
	        
	    	frame.pack();				
	    	frame.setVisible(true);
	    	
	    	frame.addWindowListener(new WindowAdapter(){
	    		public void windowClosing(WindowEvent e){
	    			if(isConnect) {
	    				destination.send(new LogoutCommand( sUser ));
	    			}	
	    			System.exit(0);
	    		}
	    	});
	    	
	}
    
    // Prompt user for a message. The entry should be of the form
    // <to user>,<message body>. or logout. 
    private ICommand readCommand( String str ) throws IOException {
        String sToUser = "nobody";
        String sMessage = "";
        StringBuffer sb = new StringBuffer();
        sb.append( str );
        int index1, index2;
        
        //whisper to one user.
        if (str.startsWith("/w")){
        	index1 = str.indexOf(" ");				//first space 
        	index2 = str.substring(index1+1).indexOf(" ");		//second space
        	
        	boolean isValid = index1 !=-1 &&
        								   index2 !=-1 &&
        								   !str.substring(index1+index2+2).equals("");

        	if(isValid) {       		
        		sToUser = str.substring(3, index1+index2+1);
        		sMessage = str.substring(index1+index2+2);
        		messageArea.append("you whisper to "+sToUser+ ": "+sMessage+"\n");
        	}
        	//format error.
        	else {
        		sToUser = "#$!%^&^";        		
        		sMessage = "\n**************************************" +
        							 "\nUsage: /w reciever_name message" +
        							 "\n**************************************";
        	}
            return new MessageCommand( sUser, sToUser, sMessage );
        }
        
        else if (str.equals("/all")){
        	 return new ShowAllCommand( sUser, null );
        }
        
        //logout
        else if (str.startsWith("/logout")){
        	typeArea.setEnabled(false);
        	isConnect = false;
        	messageArea.append("You has logged out.\n");
	        userTField.setEnabled(true);
	        hostTField.setEnabled(true);
	        portTField.setEnabled(true);
	        userTField.requestFocus();
            return new LogoutCommand( sUser );
        }
        
        //broadcast
        else {	   	
        	sMessage = str;
        	return new BroadcastCommand(sUser, sMessage);
        }
    }
    
    public JTextArea getMessageArea() {
    	return messageArea;
    }
    
    public void actionPerformed(ActionEvent e) {
    	boolean connectFlag = true;
		if(e.getSource()== connect){
			if(!isConnect) {
		        sUser = userTField.getText();
		        sHost = hostTField.getText();
		        sPort =  portTField.getText();

		        try {
		        	InetAddress adress; 
		        	if(sHost.length() == 0 || sHost.equals("localhost")){
		        			adress = InetAddress.getLocalHost();
		        	}else {
		        			adress = InetAddress.getByName(sHost); 
		        	}
		        	
			        destination = new RemoteDestination(adress, ( sPort.length() > 0 ) ? Integer.parseInt( sPort ) : 1972, this );
			    }catch(IOException ioe) {
			    		messageArea.append("===========================\n");
			    		messageArea.append("Connect  failed!! Check follow list.\n");
			    		messageArea.append("1. Either Hostname or Port doesn't valid.\n");
			    		messageArea.append("2. Sever didn't set up yet.\n");
			    		messageArea.append("===========================\n");
			        	connectFlag = false;
			    }
			    if(connectFlag) {
			        isConnect=true;
			        //clearAll
			        messageArea.setText("");
			        typeArea.setText("");
			        typeArea.setEnabled(true);
			        typeArea.requestFocus();

			        messageArea.append("Connect to sever successfully!!\n");
			        try {
			        	destination.send( new LoginCommand( sUser, InetAddress.getLocalHost()));
			        }catch(Exception o) {System.out.println("fuck you1!");}	
			        messageArea.append("Welcome! "+sUser+"." +"\n"); 

			        userTField.setEnabled(false);
			        hostTField.setEnabled(false);
			        portTField.setEnabled(false);
			    }else {return;}    
			}
			else {
					messageArea.append("You have already online!!\n");
			}
		}
    }
    
    public static void main( String[] args ) {
       //(new ChatClient()).run();
    	new ChatClient();
    }
    

}
            