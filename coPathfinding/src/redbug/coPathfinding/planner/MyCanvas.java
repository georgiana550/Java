package redbug.coPathfinding.planner;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;


import javax.swing.JOptionPane;
import javax.swing.JPanel;

import redbug.coPathfinding.prm.PRMplanar;


/**********************************************
 * 用來暫存canvas space的物件
 *********************************************/
class ObjectGeneralPath{
	static final byte obstacle 	= 0;
	static final byte iRobot 	= 1;		//initial robot					
	static final byte gRobot    = 2;		//goal robot
	
	GeneralPath polygon;
	byte type;
	int objectId;
	
	
	ObjectGeneralPath(int id, GeneralPath polygon, byte type){
		objectId = id;
		this.polygon = polygon;
		this.type = type; 
	}
	
	public GeneralPath getPolygon(){
		return polygon;
	}
	
	public byte type(){
		return type;
	}
	
	public int getId(){
		return objectId;
	}
}

public class MyCanvas extends JPanel implements Runnable{
	Thread newThread;
	
	//The default size of the work space is a matrix 128 by 128	
	public static final int xPlaner = 128;
	public static final int yPlaner = 128;
	
	final int xWorkSpace = 512;
	final int yWorkSpace = 512;
	
	final int MAX_FRAME = 500;
	
	final double Theta =Math.PI/180;	//角度轉弳度
	
	//planer scales to canvas
	float scaleX;			
	float scaleY;
	
	//canvas's Width and Height
	float cWidth,cHeight;
	
	Point p1; //記錄滑鼠click在canvas的點
	Point p2; //記錄滑鼠拖移到的點
	Point2D p;  //更新的initialConfig
	
	double theta1;	//記錄滑鼠第一次click的角度 	(相對obstacle或robot的中點而言)
	double theta2;	//記錄滑鼠拖移後的角度		(相對obstacle或robot的中點而言)
	
	
	boolean isDrag = false;     	//表示目前狀態是否為拖移中
	boolean isDrawSamplingPoint = false;
	boolean isDrawGraph = false;
	boolean goalFlag = false;
	boolean isLockObstacle = false;
	
	boolean isShowPath;			   	
	boolean isAnimation;
	
	
	Double[] GoalConfig;	   //暫存obstacle或robot的goalConfiguration
	Point2D referencePoint;	   //the reference point of a obstacle or robot.
	
	MyObject clickedObject;   //The object which was clicked by mouse.		
	MyObject allObstacles[];	   //reference to ParseFile中所有的obstacles和robots
	
	ParseFile parseFile;			   //儲存parseFile中所有的資訊

	Shape currentShape;				   //for storing the configuration of robot at current frame in canvas space when animation was played. 
	ArrayList<Double[]> searchPath;	   
	ArrayList<Shape> searchPathShape;  //for storing all robot's configurations on the search path in canvas space. 	
	ArrayList<ObjectGeneralPath> allObjectCSpace;
	
	//for testing
	ArrayList<Point> closedList; 	
	ArrayList<Point> praOpenList;

	int frame = 0;
	
	PRMplanar prmPlanner = PRMplanar.getInstance();
	
	public MyCanvas(){
		initialize();
	}
	
//for testing	
public void setClosedList(ArrayList<Point> o){
	closedList = o;
}

public void setRRAopen(ArrayList<Point> o){
	praOpenList = o;
}

	public void resetFrame(){
		isAnimation = false;
		frame = 0;
		repaint();
	}

	public int forward(){
//		isAnimation = true;
		if(frame < MAX_FRAME){
			frame++;
		}
		repaint();
		return frame;
	}
	
	public int backward(){
		if(frame > 0){
			frame--;
		}
		repaint();
		return frame;
	}
	
	public boolean getGoalFlag(){
		return goalFlag;
	}
	
	public void setGoalFlag(boolean cc){
		goalFlag = cc;
	}
		
	public int connectAndDrawGraph(){
		int componentSize;
		componentSize = prmPlanner.connectCandidateNeighbor();
		isDrawGraph = true;
		repaint();
		return componentSize;
	}
	
	public void drawSimplingPoint(){
		isLockObstacle = true;
		prmPlanner.uniformRandomSimpling(xPlaner, yPlaner);
		isDrawSamplingPoint = true;
		repaint();
	}
	
	public void drawGrid(){
		isLockObstacle = true;
		prmPlanner.plotGrid(xPlaner, yPlaner);
		isDrawSamplingPoint = true;
		repaint();
	}
	
	
	public void drawTestPoint(){
		isLockObstacle = true;
		prmPlanner.plotTestingPoints();
		isDrawSamplingPoint = true;
		repaint();
	}
	
	public void animation(){
			isAnimation = true;
			newThread = new Thread(this);
			newThread.start();
	}
	
	public void run()
	{
		frame = 0;
		if (isAnimation){
			while(frame < MAX_FRAME){
				
				// if animation was interrupted by user.
				if(!isAnimation){
					break;
				}	
				
				frame++;
				repaint();
				try {
					Thread.sleep(300);        
				} catch (InterruptedException e) {
					e.printStackTrace();
				}				
			}
		}
	}
	
	public void initialize(){
		isShowPath = false;
		isAnimation = false;
		isLockObstacle = false;
		
		setPreferredSize(new Dimension(xWorkSpace, yWorkSpace));    
		parseFile = ParseFile.getInstance();														
		
		/*****************************************************************
		 * Copying all robots and obstacles into the array "allObject",
		 * but only dealing with one robot.
		 *****************************************************************/

		allObstacles = new MyObject[parseFile.obstacles.length];     
		
		
		/***********************************************************************************
		 * important! Java always copy by value of object's reference when deals the object.
		 * so, when the array allObject was modified, the source array will be changed too.
		 ***********************************************************************************/
		//copying all obstacles into allObject.
		System.arraycopy(parseFile.obstacles,  0, 
						 allObstacles, 		   0,
						 parseFile.obstacles.length);		
		
		allObjectsTransformP2C();
		repaint();
		
		addMouseListener(new MouseAdapter(){
						
			public void mousePressed(MouseEvent e){				
				p1 = e.getPoint();				//第一次滑鼠任意鍵click在canvas上的點
				int objectId;
				GeneralPath polygon;

				for(Iterator<ObjectGeneralPath> it = allObjectCSpace.iterator();it.hasNext();){
					ObjectGeneralPath object = it.next();
					polygon = object.getPolygon();
					objectId = object.getId();
						
					//檢查取出的polygon是否被click
					if(polygon.contains(p1))
					{
						isDrag = true;			//Beginning to drag.
						clickedObject = allObstacles[objectId];				
						
						//以canvas space中的物件的initial configuration做旋轉中心.
						referencePoint = new Point2D.Double(clickedObject.getInitialConfig()[0]* scaleX, cHeight-(clickedObject.getInitialConfig()[1]* scaleY));
						
						return;
					}
				}
				
				/**********************************************************
				 * click on space.
				 **********************************************************/
				int x = (int)Math.round(p1.x/scaleX);
				int y = (int)Math.round(p1.y/scaleY);
				
				
				/*****************************************************************************
				 * add one normal node or a pair of specific nodes of a robot into the graph.
				 *****************************************************************************/
				if(!goalFlag){
					prmPlanner.addOnePoint(x, y);
				}else{
					prmPlanner.addSpecialNode(x, y);
				}
				
				
				repaint();
			}
			
			public void mouseReleased(MouseEvent e){				
				isDrag= false;        //解除拖移狀態
			}
		});
		
		
		addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){

				if(isDrag == false) return;	//滑鼠任意鍵放開便結束拖移
					
				if(!isLockObstacle){		
					p2 = e.getPoint();				//拖移到的點,在拖移中會持續更新
					
					//drag by pressing the right button of the mouse : rotation
					if(e.getModifiers() == MouseEvent.META_MASK){
						theta1 = Math.toDegrees(Math.atan2(p1.getY()-referencePoint.getY(),p1.getX()-referencePoint.getX()));
						theta2 = Math.toDegrees(Math.atan2(p2.getY()-referencePoint.getY(),p2.getX()-referencePoint.getX()));
						p1 = p2;						  //每次位移的基準點,是上一單位時間拖移後的點,而非MousePressed中第一次click的點
						
						/*
						 * Updating the theta in the initial configuration of the clicked object, 
						 * and the object will be drawn correctly after calling the function repaint().
						 */
						clickedObject.modifyTheta(theta1-theta2);   
						allObjectsTransformP2C();
						repaint();
					}
					
					//drag by pressing the left button of the mouse : translation
					else{ 
						p = new Point2D.Double();
		
						double dx = p2.getX()-p1.getX();	//the offset of x position.	
						double dy = p1.getY()-p2.getY();	//the offset of y position.
						p1 = p2; 							//每次位移的基準點,是上一單位時間拖移後的點,而非MousePressed中第一次click的點
					
						/*
						 * Updating the position x and y in the initial configuration of the clicked object, 
						 * and the object will be drawn correctly after calling the function repaint().
						 */
						clickedObject.modifyPositoin(dx/scaleX, dy/scaleY);           //改變被平移的obstacle或robot的initialconfig's (x,y)
						allObjectsTransformP2C();
						repaint();
					}
				}else{
					JOptionPane.showMessageDialog(null,
						    "All obstacles have been locked after generating nodes on the canvas!!",
						    "Operation Message",
						    JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
	}
	
	
	
	/******************************************************************
	 * Do the transformation between planner space and canvas space to all objects 
	 * and store into the collection searchPathShape.  
	 *****************************************************************/
	private void allObjectsTransformP2C(){
		allObjectCSpace = new ArrayList<ObjectGeneralPath>();
		ArrayList<Point2D[]> polygonArrayList;
		Double[] initialConfig;
		Point2D[] vertexs;
		GeneralPath polygon;
		byte type;
		
		/*************************************************************
		 * Checking which object was clicked.
		 * ideas: 先把每個object從planer space轉為canvas space,
		 *        如此才能正確判斷滑鼠click是否點在object中.
		 *************************************************************/
		for(int i = 0; i < allObstacles.length; i++){
			polygonArrayList = allObstacles[i].getPolygonArrayList();
			initialConfig = allObstacles[i].getInitialConfig();

			if(allObstacles[i] instanceof MyRobot){
				if(((MyRobot)allObstacles[i]).isAGoalRobot()){
					type = ObjectGeneralPath.gRobot;
				}else{
					type = ObjectGeneralPath.iRobot;
				}
				
			}else{
				type = ObjectGeneralPath.obstacle;
			}
			
			/*********************************************************
			 *  把object拆解成一個個convex polygon,
			 *  再一一測試滑鼠是否點在polygon中.
			 *********************************************************/
			Iterator<Point2D[]> it = polygonArrayList.iterator();
			
			while(it.hasNext()){
				vertexs =(Point2D[])it.next();
				polygon = new GeneralPath();
				MyMath.constructPolygon(vertexs, polygon, true);
				
				polygon.transform(
					MyMath.getP2CMatrix(initialConfig, cHeight, scaleX, scaleY)
				);
				allObjectCSpace.add(new ObjectGeneralPath(i, polygon, type));
			}
		}	
	} 
		
	
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;      //使用Java2D
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);  //採用柔邊
		g2.setPaint(Color.red);					

		cWidth = getWidth();
		cHeight = getHeight();
		scaleX = cWidth/xPlaner;			
		scaleY = cHeight/yPlaner;			
				
		GeneralPath polygon;
		
		/**********************************************************************************************
		 * Retrieving every object from the container allObjectCSpace and drawing them on the canvas.
		 **********************************************************************************************/
		for(Iterator<ObjectGeneralPath> it = allObjectCSpace.iterator();it.hasNext();){
			ObjectGeneralPath object = it.next();
			polygon = object.getPolygon();

			//I don't know why jvm doesn't draw any object on the canvas at the beginning.
			g2.fill(polygon);

		}
		

		int x, y;
		g2.setPaint(Color.BLACK);
		g2.setStroke(new BasicStroke(2));
		
		for(Point p: prmPlanner.getSamplingConfigs()){
			x = Math.round(p.x * scaleX);
			y = Math.round(p.y * scaleY);
			g2.drawLine(x, y, x, y);
		}
		

		if(isDrawGraph){
			int sx, sy, nx, ny;
			HashMap<Integer,Color> componetColorMap = prmPlanner.getComponetColorMap();
			HashMap<Integer, Integer> parentSet = prmPlanner.getParentSet();
			
			HashMap<Integer, HashSet<Integer>> adjacentList = prmPlanner.getAdjacentList();
			HashSet<Integer> candidateNeighbor;
			ArrayList<Point> specialNodeList = prmPlanner.getSpecialNodeList();
			
			g2.setStroke(new BasicStroke(1));
					
			/***********************************************************
			 * Graph drawing
			 ***********************************************************/
			for(Integer nodeId: adjacentList.keySet()){
				g2.setPaint(componetColorMap.get(parentSet.get(nodeId)));
			
				sx = nodeId % xPlaner;
				sy = nodeId / xPlaner;
				
				sx = Math.round(sx * scaleX);
				sy = Math.round(sy * scaleY);
				candidateNeighbor = adjacentList.get(nodeId);
	
				for(Integer neighborId: candidateNeighbor){
					g2.setStroke(new BasicStroke(1));
					
					nx = neighborId % xPlaner;
					ny = neighborId / xPlaner;
					
					nx = Math.round(nx * scaleX);
					ny = Math.round(ny * scaleY);

					g2.drawLine(sx,sy,nx,ny);
				}

			}
			
			
			/************************************************
			 * To draw initials and goals of all robots. 
			 ************************************************/
			int i = 1;			
			int robotNum;
			boolean isGoal = false;
			int w, h;
			w = h = 14;
			for(Point p: specialNodeList){
				i++;
				robotNum = i/2;
				
				// draw rectangle
				if(isGoal){
					g2.setPaint(Color.black);
				}else{
					g2.setPaint(new Color(0f, 0f, 0.9f, 0.8f));
				}	
				nx = Math.round(p.x * scaleX);
				ny = Math.round(p.y * scaleY);
				g2.fill3DRect(nx - w/2, ny - h/2, w, h, true);
				
				/************************************************************
				 *  draw the serial number of all robots.
				 ************************************************************/
				g2.setPaint(Color.white);				
			    Font font = new Font("Arial", Font.BOLD, 10);
			    g2.setFont(font);
			    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			    if(robotNum >= 10){
			    	g2.drawString(String.valueOf(robotNum), nx-6, ny+4);
			    }else{
			    	g2.drawString(String.valueOf(robotNum), nx-2, ny+4);
			    }	
			    
			    isGoal = !isGoal;
			}
			
			
			/****************************************
			 * Draw search path
			 ****************************************/
			g2.setPaint(new Color(102,0,0));
			g2.setStroke(new BasicStroke(2));
			Object pathArray[];
			Point p1,p2;
			int x1, x2, y1, y2, pathLen, upperLimit;
			
			HashMap<Integer, ArrayList<Point> > resultPaths  = prmPlanner.getResultPaths();
			
			for(Integer robotId: resultPaths.keySet()){
				pathArray = resultPaths.get(robotId).toArray();
				pathLen = pathArray.length;
					
				upperLimit = frame < pathLen-1? frame: pathLen-1;	

				for(int k=0; k < upperLimit; k++){	
					p1 = (Point)pathArray[k];
					p2 = (Point)pathArray[k+1];
					
					x1 = Math.round(p1.x * scaleX);
					y1 = Math.round(p1.y * scaleY);
					x2 = Math.round(p2.x * scaleX);
					y2 = Math.round(p2.y * scaleY);
					
					g2.drawLine(x1, y1, x2, y2);
				}
			}
			
		
			/**************************************************************
			 * show the positions of all robots at present frame.
			 **************************************************************/
			int j = 1;
	
			for(Integer robotId: resultPaths.keySet()){
				pathArray = resultPaths.get(robotId).toArray();
				
				g2.setPaint(new Color(1f, 1f, 0f, 0.5f));
				if(frame <= pathArray.length-1){
					
					//draw rectangle
					p1 = (Point)pathArray[frame];
					x1 = Math.round(p1.x * scaleX);
					y1 = Math.round(p1.y * scaleY);	
					g2.fill3DRect(x1 - w/2, y1 - h/2, w, h, true);
					g2.setPaint(Color.black);
					g2.draw3DRect(x1 - w/2, y1 - h/2, w, h, true);

					
					//draw number
					g2.setPaint(Color.black);
				    Font font = new Font("Arial", Font.BOLD, 10);
				    g2.setFont(font);
				    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				    if(j >= 10){
				    	g2.drawString(String.valueOf(robotId), x1-6, y1+4);
				    }else{
				    	g2.drawString(String.valueOf(robotId), x1-2, y1+4);
				    }	
				}
				j++;
			}				
		}
		
		/******************************************
		 *  show the current frame
		 ******************************************/
		g2.setPaint(Color.BLACK);
		Font font = new Font("Arial", Font.BOLD, 12);
		g2.setFont(font);
		g2.drawString("frame: " + String.valueOf(frame), xWorkSpace - 80, yWorkSpace + 60);
		
	}
}