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
	
	static String[] originFile;			   //暫存目前讀取的檔案,改成下拉式選單時可能要改寫
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
	boolean isPotentialReady;				  //記錄Potential是否已被計算.
	
	
	public UI(){				
		isPotentialReady = false;
		
		/* 視窗介面初始化 */
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
		
		canvas = new MyCanvas();			//把parseFile丟給CanvasDemo繪製,沒有showPath
		
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
		//地圖功能選單
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
			canvas.initialize();				//重畫Canvas
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
		
		//手動測試CollsionDetect
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
		 * 因為class檔產生在/bin下, 所以ClassLoader的相對路徑在/bin, 於是要copy一份map到/bin下. 
		 * ClassLoader才讀的到圖.
		 * 
		 * case 2: Run by Jar
		 * eclipse在產生jar時, 只會將/bin下的class檔export, 所以/bin下的map目錄並不會被export.
		 * 於是必須在GRA目錄下也要有一個map目錄, 產生jar檔之後, class檔和map目錄就會在同級目錄, 
		 * 所以ClassLoader的相對路徑一致, 便可以正確存取.
		 */
		originFile[0] = "/map/robot1.dat";
		originFile[1] = "/map/obstacle1.dat";	
		
		ParseFile.getInstance().initialize(originFile);
		new UI();

	}

}

//繪製PotentialField的小畫布
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
		Graphics2D g2 = (Graphics2D)g;      	//使用Java2D
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);  //採用柔邊
		
		if(pf == null) return;
		
		for(int k=0;k<pf.yPlaner;k++){
			for(int l=0;l<pf.xPlaner;l++){
				if(bitMap[l][k] == 255){				//obstacle畫藍點
					g2.setPaint(Color.blue);
					g2.drawLine(l,k,l,k);
				}
				else if (bitMap[l][k] == 0){			//goal畫紅點
				    g2.setPaint(Color.red);
				    g2.drawLine(l,k,l,k);
				}    
				else if (bitMap[l][k] == 254){ 			//沒有拜訪過的點畫白點
					g2.setPaint(Color.white);
				    g2.drawLine(l,k,l,k);
				}
				else{
					g2.setPaint(new Color(0,0,0,bitMap[l][k]));		//potential Field以單色,不同亮度來顯示
					g2.drawLine(l,k,l,k);
				}	
			}			
		}
	}
}
