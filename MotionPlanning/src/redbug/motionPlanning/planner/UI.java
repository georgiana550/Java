package redbug.motionPlanning.planner;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

public class UI implements ActionListener{
	JFrame f = null;                   
	JPanel buttonPanel;
	JPanel rightPanel;
	MyCanvas canvas;
	PFcanvas pfCanvas;
	
	static String[] originFile;			   //�Ȧs�ثeŪ�����ɮ�,�令�U�Ԧ����ɥi��n��g
	JMenuBar menubar;
	JMenu map;
	JMenuItem[] mapItem;
	
	JButton resetButton,
			potentialButton,
			collisionButton,
			showPathButton,
			animationButton;
		
	JLabel messageBoard;
	
	ParseFile pFile = ParseFile.getInstance();
	CollisionDetect cd = CollisionDetect.getInstance();
	
	PotentialField potentialField;				
	Search search;	
	boolean isPotentialReady;				  //�O��Potential�O�_�w�Q�p��.
	
	
	public UI(){				
		isPotentialReady = false;
		
		/* ����������l�� */
		f = new JFrame("Motion Plannning");
		menubar = new JMenuBar();
		map = new JMenu("Maps");
		mapItem = new JMenuItem[4];
		for(int i=0; i<mapItem.length; i++){
			mapItem[i] = new JMenuItem("Map "+(i+1));
			map.add(mapItem[i]);
			mapItem[i].addActionListener(this);
		}
		menubar.add(map);
		f.setJMenuBar(menubar);
	
		Container contentPane = f.getContentPane();
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.X_AXIS));
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(6,1));
		
		canvas = new MyCanvas();			//��parseFile�ᵹCanvasDemoø�s,�S��showPath
		
		//canvas.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		
		resetButton = new JButton("Start / Reset");
		potentialButton = new JButton("Potential Field");
		collisionButton = new JButton("Collision testing");
		showPathButton = new JButton("Show Path");
		animationButton = new JButton("Animation");
		
		messageBoard = new JLabel();
		messageBoard.setHorizontalAlignment(0);
		
		pfCanvas = new PFcanvas();
		pfCanvas.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		
		resetButton.addActionListener(this);
		potentialButton.addActionListener(this);
		collisionButton.addActionListener(this);
		showPathButton.addActionListener(this);
		animationButton.addActionListener(this);
		
		buttonPanel.add(resetButton);
		buttonPanel.add(potentialButton);
		buttonPanel.add(collisionButton);
		buttonPanel.add(showPathButton);
		buttonPanel.add(animationButton);
		buttonPanel.add(messageBoard);
		
		rightPanel.add(buttonPanel);		
		rightPanel.add(pfCanvas);

		contentPane.add(canvas);			
		contentPane.add(rightPanel);		

		
//		f.pack();			
		f.setVisible(true);		
		f.setResizable( false );
		f.setSize(new Dimension(640,562));
		
		f.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
	}
			
	public void actionPerformed(ActionEvent e){
		//�a�ϥ\����
		for(int i=0; i<mapItem.length; i++){
			if(e.getSource() == mapItem[i]){
				String[] source = new String[2];
				source[0] = new String("map/obstacle"+(i+1)+".dat");
				source[1] = new String("map/robot"+(i+1)+".dat");
				
				pFile.initialize(source);
				
				isPotentialReady = false;
				originFile = source;
				canvas.removeMouseListener(canvas.getMouseListeners()[0]);
				canvas.removeMouseMotionListener(canvas.getMouseMotionListeners()[0]);
				canvas.initialize();
			}
		}
		
		//reset
		if(e.getSource()== resetButton){
			pFile.initialize(originFile);
			canvas.removeMouseListener(canvas.getMouseListeners()[0]);
			canvas.removeMouseMotionListener(canvas.getMouseMotionListeners()[0]);
			canvas.initialize();				//���eCanvas
		}
		
		//potential
		else if(e.getSource()== potentialButton){
			canvas.resetCanvas();
			canvas.repaint();
			potentialField =  PotentialField.getInstance();
			potentialField.initialize();
			
			isPotentialReady = true;
			messageBoard.setText("");
			pfCanvas.paint(potentialField);
		}
		
		//��ʴ���CollsionDetect
		else if(e.getSource()== collisionButton){
			if(isPotentialReady){
				
				cd.caculateObstaclePlanarSpace();
				
				if(cd.isCollision(pFile.robots[0].getInitialConfig()))
					messageBoard.setText("Collsion!!");
				else
					messageBoard.setText("No Collision!!");
			}
			else
				messageBoard.setText("Do Potential Field First!!");
		}
		
		//show(search path)
		else if(e.getSource()== showPathButton){
			canvas.resetCanvas();
			if(isPotentialReady){
				cd.caculateObstaclePlanarSpace();
				
				long startTime = new GregorianCalendar().getTimeInMillis();  				
				search = new Search();
				long endTime = new GregorianCalendar().getTimeInMillis();
				if(search.isSearchSuccess()){						
					long elapseTime = endTime - startTime;
					elapseTime /= 1000;
//					int elapseMin = (int)(elapseTime / 60000);
//					int elapseSec = (int)(elapseTime %60000)/1000;
					messageBoard.setText("Elapse " + elapseTime + " Secs.");
				}
				else{	
					messageBoard.setText("Search Failed..");
				}	
				canvas.drawPath(search.getPath());		
			}
			else
				messageBoard.setText("Do Potential Field First!!");
		}
		else if (e.getSource() == animationButton){
			if(!canvas.runAnimation())
				messageBoard.setText("Do Show Path First!!");
		}		
	}
	
	
	public static void main(String[] args) {	
		originFile = new String[2];         //robot & obstacle
		/*
		 * case 1: Run by Eclipse
		 * �]��class�ɲ��ͦb/bin�U, �ҥHClassLoader���۹���|�b/bin, ��O�ncopy�@��map��/bin�U. 
		 * ClassLoader�~Ū�����.
		 * 
		 * case 2: Run by Jar
		 * eclipse�b����jar��, �u�|�N/bin�U��class��export, �ҥH/bin�U��map�ؿ��ä��|�Qexport.
		 * ��O�����bGRA�ؿ��U�]�n���@��map�ؿ�, ����jar�ɤ���, class�ɩMmap�ؿ��N�|�b�P�ťؿ�, 
		 * �ҥHClassLoader���۹���|�@�P, �K�i�H���T�s��.
		 */
		originFile[0] = "/map/robot1.dat";
		originFile[1] = "/map/obstacle1.dat";	
		
		ParseFile.getInstance().initialize(originFile);
		new UI();

	}

}

//ø�sPotentialField���p�e��
class PFcanvas extends JPanel{
	PotentialField pf;
	int bitMap[][];
	
	public PFcanvas(){}
	
	public void paint(PotentialField pf){
		setPreferredSize(new Dimension(pf.xPlaner, pf.yPlaner));
		this.pf = pf;
		bitMap = pf.bitMap;
		repaint();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;      	//�ϥ�Java2D
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);  //�ĥάX��
		
		if(pf == null) return;
		
		for(int k=0;k<pf.yPlaner;k++){
			for(int l=0;l<pf.xPlaner;l++){
				if(bitMap[l][k] == 255){				//obstacle�e���I
					g2.setPaint(Color.blue);
					g2.drawLine(l,k,l,k);
				}
				else if (bitMap[l][k] == 0){			//goal�e���I
				    g2.setPaint(Color.red);
				    g2.drawLine(l,k,l,k);
				}    
				else if (bitMap[l][k] == 254){ 			//�S�����X�L���I�e���I
					g2.setPaint(Color.white);
				    g2.drawLine(l,k,l,k);
				}
				else{
					g2.setPaint(new Color(0,0,0,bitMap[l][k]));		//potential Field�H���,���P�G�ר����
					g2.drawLine(l,k,l,k);
				}	
			}			
		}
	}
}
