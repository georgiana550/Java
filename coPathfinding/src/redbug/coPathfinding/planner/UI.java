package redbug.coPathfinding.planner;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.JPopupMenu.Separator;

import redbug.coPathfinding.prm.PRMplanar;
import redbug.coPathfinding.whca.WHCAstar;

public class UI implements ActionListener, ItemListener{
	JFrame f = null;                   
	JPanel buttonPanel;
	JPanel rightPanel;
	MyCanvas canvas;
	
	static String[] originFile;			   //暫存目前讀取的檔案,改成下拉式選單時可能要改寫
	JMenuBar menubar;
	JMenu map;
	JMenuItem[] mapItem;
	
	JButton resetButton,
			simpleDemoButton,
			samplingButton,
			plotGridButton,
			neighborConnectButton,
			forwardButton,
			backwardButton,
			rocknrollBtton,
			resetFrameButton;
	
	JToggleButton goalFlagTButton,			
				  hcaTButton,
				  whca8TButton,
				  whca16TButton,
				  whca32TButton;
	
	JLabel messageBoard;
	
	ParseFile pFile = ParseFile.getInstance();
		
	PRMplanar prmPlanar;
	WHCAstar whcaStar;
	final int UPPER_LIMITED_DEPTH = 1000;
	
	
	public UI(){	
		prmPlanar = PRMplanar.getInstance();
		
		f = new JFrame("Cooperative Pathfinding");
		menubar = new JMenuBar();
		map = new JMenu("Load Map");
		mapItem = new JMenuItem[5];
		for(int i=0; i<mapItem.length; i++){
			if(i == 0){
				mapItem[i] = new JMenuItem("Empty");
			}else{
				mapItem[i] = new JMenuItem("Map"+(i));
			}
			map.add(mapItem[i]);
			mapItem[i].addActionListener(this);
		}
		menubar.add(map);
		f.setJMenuBar(menubar);
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		
		buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(20,1));
		
		canvas = new MyCanvas();

		//canvas.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));

		Container contentPane = f.getContentPane();
		
		//JButton
		resetButton = new JButton("Reset");
		samplingButton = new JButton("Random Sampling");
		plotGridButton = new JButton("Plot Grid");
		neighborConnectButton = new JButton("Neghbor Connect");
		forwardButton = new JButton("Step Forward");
		backwardButton = new JButton("Step Backward");
		rocknrollBtton = new JButton("Rock 'N' Roll");
		simpleDemoButton = new JButton("Simple Demo");
		resetFrameButton = new JButton("Reset Frame");
		
		//JToggleButton
		goalFlagTButton = new JToggleButton("Node (switch)");
		hcaTButton = new JToggleButton("HCA*");
		whca8TButton = new JToggleButton("WHCA(8)");
		whca16TButton = new JToggleButton("WHCA(16)");
		whca32TButton = new JToggleButton("WHCA(32)");
		
		
		messageBoard = new JLabel("", JLabel.CENTER);
		
		enableAnimation(false);
				
		resetButton.addActionListener(this);
		samplingButton.addActionListener(this);
		plotGridButton.addActionListener(this);
		neighborConnectButton.addActionListener(this);
		forwardButton.addActionListener(this);
		backwardButton.addActionListener(this);
		rocknrollBtton.addActionListener(this);
		simpleDemoButton.addActionListener(this);
		resetFrameButton.addActionListener(this);
		
		goalFlagTButton.addItemListener(this);
		hcaTButton.addItemListener(this);
		whca8TButton.addItemListener(this);
		whca16TButton.addItemListener(this);
		whca32TButton.addItemListener(this);
		
		buttonPanel.add(resetButton);
		buttonPanel.add(simpleDemoButton);
		buttonPanel.add(new JSeparator());
		buttonPanel.add(samplingButton);
		buttonPanel.add(plotGridButton);
		buttonPanel.add(new JSeparator());
		buttonPanel.add(neighborConnectButton);
		buttonPanel.add(goalFlagTButton);
		buttonPanel.add(messageBoard);
		buttonPanel.add(hcaTButton);
		buttonPanel.add(whca8TButton);
		buttonPanel.add(whca16TButton);
		buttonPanel.add(whca32TButton);
		buttonPanel.add(new JSeparator());
		buttonPanel.add(forwardButton);
		buttonPanel.add(backwardButton);
		buttonPanel.add(rocknrollBtton);
		buttonPanel.add(resetFrameButton);


		
		rightPanel.add(buttonPanel, BorderLayout.NORTH);
		
		contentPane.add(rightPanel, BorderLayout.EAST);			//按鍵列在東
		contentPane.add(canvas, BorderLayout.CENTER);			//畫布在中

		
		f.pack();				//視窗自動調整大小
		f.setVisible(true);		//顯示視窗
		f.setResizable( false ); //設定視窗不可改變大小
		f.setSize(new Dimension(640,640));
		
		f.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
	}
		
	
	public void resetAll(){
		canvas.removeMouseListener(canvas.getMouseListeners()[0]);
		canvas.removeMouseMotionListener(canvas.getMouseMotionListeners()[0]);
		canvas.initialize();				//重畫Canvas
		prmPlanar.cleanAll();
		
		enableAnimation(false);
		
		samplingButton.setEnabled(true);
		plotGridButton.setEnabled(true);
		neighborConnectButton.setEnabled(true);
		goalFlagTButton.setEnabled(true);
		
		goalFlagTButton.setSelected(false);			
		hcaTButton.setSelected(false);
		whca8TButton.setSelected(false);
		whca16TButton.setSelected(false);
		whca32TButton.setSelected(false);
	}
	
	
	
	/*************************************************************
	 * To check whether each robot has it's initial and goal
	 *************************************************************/
	private boolean checkValidityOfRobot( ArrayList<Point> specialNodeList ){	
		int n = specialNodeList.size(); 
		if( n % 2 != 0){
			JOptionPane.showMessageDialog(null,
				    "Robot No." + (n/2+1) + " doesn't has a goal!",
				    "Error Message",
				    JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}
	
	
	/****************************************************************
	 * To check whether all robots are connected to the graph or not.
	 ****************************************************************/	
	private boolean checkConnectibleOfRobot(ArrayList<Point> specialNodeList){
		Point iNode, gNode;
		int iId, gId;
		int robotNum = 0;
		boolean valid = true;
		ArrayList<Integer> invalidRobots = new ArrayList<Integer>();
		
		for(Iterator<Point> it = specialNodeList.iterator(); it.hasNext(); ){
			iNode = it.next();
			gNode = it.next();
			iId = MyMath.point2NodeId(iNode);
			gId = MyMath.point2NodeId(gNode);
			
			robotNum++;
			
			if(!prmPlanar.isExistInGraph(iId) || !prmPlanar.isExistInGraph(gId)){
				invalidRobots.add(robotNum);
				valid = false;
			}
		}
		
		if(!valid){
			String str = new String("Connect below robots to the graph before starting search.");
			str += '\n';
			
			for(Integer i: invalidRobots){
				str += i;
				str += " ";
			}		
			str += '\n';
			str += "Do it right now ?";
			
			if(JOptionPane.showConfirmDialog(null, str) == 0){
				canvas.connectAndDrawGraph();
				return true;
			}
			return false;
		}		
		
		return true;
	}

	
	/**********************************************************************
	 * To notice user which robot didn't place at the same component.
	 **********************************************************************/
	private boolean checkIsSameComponent( ArrayList<Point> specialNodeList ){
		Point iNode, gNode;
		int iId, gId;
		int robotNum = 0;
		ArrayList<Integer> invalidRobots = new ArrayList<Integer>();

		
		for(Iterator<Point> it = specialNodeList.iterator(); it.hasNext(); ){
			iNode = it.next();
			gNode = it.next();
			
			iId = MyMath.point2NodeId(iNode);
			gId = MyMath.point2NodeId(gNode);
	
			robotNum++;							
			
			if(!prmPlanar.isSameComponent(iId, gId)){
				invalidRobots.add(robotNum);
			}
		}	
	
		if(!invalidRobots.isEmpty()){
			String str = new String();
			str = "The robots below are not at the same component.";
			str += '\n';
			
			for(Integer i: invalidRobots){
				str += i;
				str += " ";
			}		
			str += '\n';
			
			JOptionPane.showMessageDialog(null,
				    str,
				    "Error Message",
				    JOptionPane.ERROR_MESSAGE);
			
			return false;
		}
		
		return true;
	}
	
	
	private void reportSearchResult(boolean searchResult[]){
		String failedRobots = new String();
		String resultMessage;
		
		for(int i=0; i < searchResult.length; i++){
			if(searchResult[i] == false){
				failedRobots += i+1;
				failedRobots += " ";
			}	
		}
		
		if(!failedRobots.equals("")){
			resultMessage = new String("The robots below are search failure!");
			resultMessage += '\n';
			resultMessage += failedRobots;
		}
		else{
			resultMessage = new String("Cooperative search successfully!!");
		}
		
		JOptionPane.showMessageDialog(null,
			    resultMessage,
			    "Search Result",
			    JOptionPane.WARNING_MESSAGE);
	}
	
	private void enableAnimation(boolean b){
		forwardButton.setEnabled(b);
		backwardButton.setEnabled(b);
		rocknrollBtton.setEnabled(b);
		resetFrameButton.setEnabled(b);
	}
	
	
	public void runWhca(int windowSize){
		WHCAstar.clearReservationTable();
		prmPlanar.clearResultPaths();
		
		boolean flag = false;
		int robotNum = 0;
		int iId, gId;

		ArrayList<Point> specialNodeList = prmPlanar.getSpecialNodeList();
		ArrayList<Point> path;
		Point iNode, gNode;

		
		
		if(!checkValidityOfRobot(specialNodeList)) 
			return ;
		
		if(!checkConnectibleOfRobot(specialNodeList))
			return ;
		
		if(!checkIsSameComponent(specialNodeList))
			return ;
				
		int robotSize = specialNodeList.size() / 2;
		boolean searchResult[] = new boolean[robotSize];
		
		WHCAstar partialPath[] = new WHCAstar[robotSize];
		
		for(int i=0; i< UPPER_LIMITED_DEPTH; i += windowSize){
			robotNum = 0;			
			
			for(Iterator<Point> it = specialNodeList.iterator(); it.hasNext(); ){
				iNode = it.next();
				gNode = it.next();
				
				iId = MyMath.point2NodeId(iNode);
				gId = MyMath.point2NodeId(gNode);
				if(!flag){
					partialPath[robotNum] = new WHCAstar( iNode, gNode);
				}	
				
				path = partialPath[robotNum].STAstar(windowSize);
				if(!path.isEmpty()){
					if(searchResult[robotNum] == false && MyMath.point2NodeId(path.get(path.size()-1)) == gId){
						searchResult[robotNum] = true;
					}
				}
				robotNum++;
				prmPlanar.addOnePath(robotNum, path);
			}
			flag = true;
		}	
		reportSearchResult(searchResult);
						
		enableAnimation(true);
		
		canvas.resetFrame();
		canvas.repaint();
	}
	
	
	
	
	public void actionPerformed(ActionEvent e){
		//地圖功能選單
		for(int i=0; i<mapItem.length; i++){
			if(e.getSource() == mapItem[0]){
				pFile.clearAll();
				originFile = null;
				resetAll();
			}
			else if(e.getSource() == mapItem[i]){
				String[] source = new String[2];
				source[0] = new String("map/obstacle"+(i)+".dat");
				source[1] = new String("map/robot"+(i)+".dat");
				
				pFile.initialize(source);
				originFile = source;
				resetAll();
			}	
		}
		
		//reset
		if(e.getSource()== resetButton){
			if(originFile != null){
				pFile.initialize(originFile);	
			}
			resetAll();
		}
		else if (e.getSource() == samplingButton){
			canvas.drawSimplingPoint();
		}
		else if (e.getSource() == plotGridButton){
			canvas.drawGrid();
		}
		else if (e.getSource() == neighborConnectButton){
			messageBoard.setText(canvas.connectAndDrawGraph() + " components");
		} 
		else if (e.getSource() == rocknrollBtton){
			canvas.animation();
		}else if (e.getSource() == simpleDemoButton){
			pFile.clearAll();
			originFile = null;
			resetAll();
			samplingButton.setEnabled(false);
			plotGridButton.setEnabled(false);
			neighborConnectButton.setEnabled(false);
			goalFlagTButton.setEnabled(false);
			
			canvas.drawTestPoint();
			canvas.connectAndDrawGraph();
			prmPlanar.defaultTestingRobotConfig();
		}else if (e.getSource() == forwardButton){
			canvas.forward();
		}else if (e.getSource() == backwardButton){
			canvas.backward();
		}else if (e.getSource() == resetFrameButton){
			canvas.resetFrame();
		}
		
	}
	
	public void itemStateChanged(ItemEvent e) {
		if (e.getSource() == goalFlagTButton) {
			if(goalFlagTButton.isSelected()){
				goalFlagTButton.setText("Robot (switch)");
				canvas.setGoalFlag(true);
			}else{
				goalFlagTButton.setText("Point (switch)");
				canvas.setGoalFlag(false);
			}		
		}
		else if (e.getSource() == hcaTButton && hcaTButton.isSelected()){
			whca8TButton.setSelected(false);
			whca16TButton.setSelected(false);
			whca32TButton.setSelected(false);
			
			runWhca(UPPER_LIMITED_DEPTH);
		}
		else if (e.getSource() == whca8TButton && whca8TButton.isSelected()){
			hcaTButton.setSelected(false);
			whca16TButton.setSelected(false);
			whca32TButton.setSelected(false);
			
			runWhca(8);
		}
		else if (e.getSource() == whca16TButton && whca16TButton.isSelected()){
			hcaTButton.setSelected(false);
			whca8TButton.setSelected(false);
			whca32TButton.setSelected(false);

			runWhca(16);
		}
		else if (e.getSource() == whca32TButton && whca32TButton.isSelected()){
			hcaTButton.setSelected(false);
			whca8TButton.setSelected(false);
			whca16TButton.setSelected(false);

			runWhca(32);
		}

	}
	
	
	
	
	public static void main(String[] args) {	
		/******************************************************************************************
		 * case 1: Run by Eclipse
		 * 因為class檔產生在/bin下, 所以ClassLoader的相對路徑在/bin, 於是要copy一份map到/bin下. 
		 * ClassLoader才讀的到圖.
		 * 
		 * case 2: Run by Jar
		 * eclipse在產生jar時, 只會將/bin下的class檔export, 所以/bin下的map目錄並不會被export.
		 * 於是必須在GRA目錄下也要有一個map目錄, 產生jar檔之後, class檔和map目錄就會在同級目錄, 
		 * 所以ClassLoader的相對路徑一致, 便可以正確存取.
		 ******************************************************************************************/
//		originFile = new String[2];         //robot & obstacle
//		originFile[0] = "/map/robot1.dat";
//		originFile[1] = "/map/obstacle1.dat";	
//		
		ParseFile.getInstance();
		new UI();

	}

}
