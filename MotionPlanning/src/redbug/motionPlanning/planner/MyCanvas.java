package redbug.motionPlanning.planner;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.JPanel;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



/**********************************************
 * 用來暫存canvas space的物件
 *********************************************/
class ObjectGeneralPath{
	static final byte obstacle 	= 0;
	static final byte initialRobot = 1;
	static final byte goalRobot    = 2;
	
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
	
	public byte getType(){
		return type;
	}
	
	public int getId(){
		return objectId;
	}
}


public class MyCanvas extends JPanel implements Runnable{

	private static final long serialVersionUID = 1L;

	Thread newThread;
	
	//Planner的世界預設大小128*128	
	public static final int xPlaner = 128;
	public static final int yPlaner = 128;
	
	final int xWorkSpace = 512;
	final int yWorkSpace = 512;
	
	final int NO_ANIMATION =0,
			  ONE_ROBOT_ANIMATION = 1,
			  MULTI_ROBOT_ANIMATION = 2;
	final int MAX_FRAME = 500;
	
	final double Theta =Math.PI/180;	//角度轉弳度

	byte clickedObjectType;
	
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
	double deltaTheta;
	
	boolean isDrag;
	boolean isShowPath;			   	
	boolean isAnimation;

	Point2D referencePoint;	   //the reference point of a obstacle or robot.
	
	MyObject clickedObject;   //The object which was clicked by mouse.		
	MyObject allObject[];	   //reference to ParseFile中所有的obstacles和robots
	
	ParseFile parseFile;			   //儲存parseFile中所有的資訊

	Shape currentShape;				   //for storing the configuration of robot at current frame in canvas space when animation was played. 
	ArrayList<Double[]> searchPath;	   
	ArrayList<Shape> searchPathShape;  //for storing all robot's configurations on the search path in canvas space. 	
	ArrayList<ObjectGeneralPath> allObjectCSpace; 

	static Logger logger = Logger.getLogger(Search.class);
	
	
	public MyCanvas(){
		setPreferredSize(new Dimension(xWorkSpace, yWorkSpace));    //預設畫布的大小
		
		cWidth  = xWorkSpace;
		cHeight = yWorkSpace;
		scaleX  = cWidth/xPlaner;			
		scaleY  = cHeight/yPlaner;	
		
		initialize();
		
		/*********************************************
		 * log4j
		 *********************************************/
	    Properties logp = new Properties();
	    try {
	      logp.load(Search.class.getClassLoader()
	        .getResourceAsStream( "log4j.properties"));
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    
	    PropertyConfigurator.configure(logp);
		logger.setLevel(Level.OFF); 
	}
		
	public void drawPath(ArrayList<Double[]> path){
		this.isShowPath = true;
		this.searchPath = path;
		searchPathShape = new ArrayList<Shape>();
		
		ArrayList<Point2D[]> polygonArrayList;
		Point2D[] vertexs;
		Double[] initialConfig;
		GeneralPath polygonPath = new GeneralPath();
		
		polygonArrayList = parseFile.robots[0].getPolygonArrayList();
		Iterator<Point2D[]> it = polygonArrayList.iterator();
		
		while(it.hasNext()){
			vertexs =(Point2D[])it.next();
			MyMath.constructPolygon(vertexs, polygonPath, false);
		}
		
		Iterator<Double[]> itShowPath = this.searchPath.iterator();
		
		while(itShowPath.hasNext()){
			initialConfig = (Double[])itShowPath.next();
			
			searchPathShape.add(polygonPath.createTransformedShape(	
					MyMath.getP2CMatrix(initialConfig, cHeight, scaleX, scaleY)
			));
			
		}
		repaint();
	}
	
	public boolean runAnimation(){
		if(!this.isShowPath){
			return false;
		}
		else{
			isAnimation = true;
			newThread = new Thread(this);
			newThread.start();
			return true;
		}	
	}
	
	public void resetCanvas(){
		isShowPath = false;			   	
		isAnimation = false;
	}
	
	public void run()
	{
		for(Shape s:searchPathShape){
			this.currentShape = s;
			repaint();
			try {
				Thread.sleep(50);        
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public void initialize(){ 
		isDrag = false;
		isShowPath = false;			   	
		isAnimation = false;
		
		parseFile = ParseFile.getInstance();														
		
		/*****************************************************************
		 * Copying all robots and obstacles into the array "allObject",
		 * but only dealing with one robot.
		 *****************************************************************/

		allObject = new MyObject[parseFile.obstacles.length+2];     
		
		
		/***********************************************************************************
		 * important! Java always copy by value of object's reference when deals the object.
		 * so, when the array allObject was modified, the source array will be changed too.
		 ***********************************************************************************/
		//copying all obstacles into allObject.
		System.arraycopy(parseFile.obstacles,  0, 
						 allObject, 		   0,
						 parseFile.obstacles.length);		
		
		//copying a robots into allObject.
		System.arraycopy(parseFile.robots,     0, 
				         allObject,            parseFile.obstacles.length,
				         1);
		
		//copying a goal robots into allObject.
		System.arraycopy(parseFile.goalRobots, 0, 
				         allObject,            parseFile.obstacles.length+1, 
				         1);			            
		
		allObjectsTransformP2C();

		
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
						isDrag = true;
						clickedObject = allObject[objectId];
						clickedObjectType = object.type;
						
						//以canvas space中的物件的initial configuration做旋轉中心.
						referencePoint = new Point2D.Double(clickedObject.getInitialConfig()[0]* scaleX, cHeight-(clickedObject.getInitialConfig()[1]* scaleY));
						
						return;
					}
				}
				
			}
			
			public void mouseReleased(MouseEvent e){			
				isDrag = false;
				long q;
				double oldTheta, diff;
				
				/****************************************************************************
				 * To tune the initial configuration or the goal configuration of robot 
				 * after user modifies one of them.  
				 ****************************************************************************/
				if(clickedObjectType != ObjectGeneralPath.obstacle){
					if(clickedObjectType == ObjectGeneralPath.initialRobot){
						oldTheta = parseFile.robots[0].initialConfig[2]; 
					}else{
						oldTheta = parseFile.goalRobots[0].initialConfig[2];              
					}
					q = Math.round(oldTheta / Search.rotateUnit);
					diff = oldTheta - Search.rotateUnit * q;
					clickedObject.modifyTheta(-diff);
					clickedObject.roundPosition();
					allObjectsTransformP2C();
					
					//for debugging
					logger.debug("iTheta:"+parseFile.robots[0].initialConfig[2]);
					logger.debug("gTheta:"+parseFile.goalRobots[0].initialConfig[2]);
					diff = parseFile.robots[0].initialConfig[2] - parseFile.goalRobots[0].initialConfig[2];
					logger.debug("diff:" + diff);
					logger.debug("mod:" + diff % Search.rotateUnit);
					logger.debug("================================");
				}
				
				repaint();
			}
		});
		
		
		addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseDragged(MouseEvent e){			 	
				
				if(!isDrag) return;
				
				p2=e.getPoint();				//拖移到的點,在拖移中會持續更新
				
				//如果使用者是用右鍵拖移,便處理旋轉
				if(e.getModifiers() == MouseEvent.META_MASK){
					theta1 = Math.toDegrees(Math.atan2(p1.getY()-referencePoint.getY(),p1.getX()-referencePoint.getX()));
					theta2 = Math.toDegrees(Math.atan2(p2.getY()-referencePoint.getY(),p2.getX()-referencePoint.getX()));
					deltaTheta = theta1 - theta2;
					deltaTheta = Math.round(deltaTheta);	
					
					p1 = p2;						  //每次位移的基準點,是上一單位時間拖移後的點,而非MousePressed中第一次click的點
					
					/********************************************************************************
					 * Updating the theta in the initial configuration of the clicked object, 
					 * and the object will be drawn correctly after calling the function repaint().
					 ********************************************************************************/
					//deltaTheta = Search.rotateUnit * Math.round(deltaTheta / Search.rotateUnit);
					clickedObject.modifyTheta(deltaTheta);   
					allObjectsTransformP2C();
					repaint();
				}
				
				//如果使用者是用左鍵拖移,便處理平移
				else{ 	
					double dx = p2.getX()-p1.getX();	//計算x的offset	
					double dy = p1.getY()-p2.getY();	//計算y的offset
					
					p1 = p2; 							//每次位移的基準點,是上一單位時間拖移後的點,而非MousePressed中第一次click的點
				
					/*************************************************************************************
					 * Updating the position x and y in the initial configuration of the clicked object, 
					 * and the object will be drawn correctly after calling the function repaint().
					 *************************************************************************************/
					clickedObject.modifyPositoin(dx/scaleX, dy/scaleY);           //改變被平移的obstacle或robot的initialconfig's (x,y)
					allObjectsTransformP2C();
					repaint();
				}	
			}
		});
		repaint();
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
		for(int i = 0; i < allObject.length; i++){
			polygonArrayList = allObject[i].getPolygonArrayList();
			initialConfig = allObject[i].getInitialConfig();

			if(allObject[i] instanceof MyRobot){
				if(((MyRobot)allObject[i]).isAGoalRobot()){
					type = ObjectGeneralPath.goalRobot;
				}else{
					type = ObjectGeneralPath.initialRobot;
				}
				
			}else{
				type = ObjectGeneralPath.obstacle;
			}
			

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
		Graphics2D g2 = (Graphics2D)g;     
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			
		g2.setPaint(Color.red);							
				
		GeneralPath polygon;
		/**********************************************************************************************
		 * Retrieving every object from the collection allObjectCSpace and drawing them on the canvas.
		 **********************************************************************************************/
		for(Iterator<ObjectGeneralPath> it = allObjectCSpace.iterator();it.hasNext();){
			ObjectGeneralPath object = it.next();
			polygon = object.getPolygon();
			
			if(object.type == ObjectGeneralPath.goalRobot){
				g2.setPaint(Color.blue);
			}
			else if(object.type == ObjectGeneralPath.initialRobot){
				g2.setPaint(Color.green);
			}
			//I don't know why jvm doesn't draw any object on the canvas at the beginning.
			g2.fill(polygon);
		}
	
		//繪製Search路徑	
		if(isShowPath){
			g2.setPaint(Color.BLACK);
			g2.setStroke(new BasicStroke(1));
			for(Shape s:searchPathShape){
				g2.draw(s);
			}
			
			if(isAnimation){
				g2.setPaint(Color.ORANGE);
				g2.fill(currentShape);
			}			
			//polygon.reset();
		}	
	}
}