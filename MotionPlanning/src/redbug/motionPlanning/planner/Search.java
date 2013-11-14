package redbug.motionPlanning.planner;
import java.io.IOException;
import java.util.*;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


class Node{
	
	private Double[] config;
	private Node previous;
	
	public Node(Double[] config){
		this.config = config;
		this.previous = null;
	}
	
	public Double[] getConfig(){
		return this.config;
	}
	
	public void setPrevious(Node preNode){
		this.previous = preNode;
	}
	
	public Node getPrevious(){
		return this.previous;
	}
	
}

//Best First Search
public class Search {
	private Double[] robotIC;					//Robot's IntialConfig
	private PotentialField potential;
	private CollisionDetect cd;

	ArrayList<Node>[] open;						//The Open List(priority queue) in the Best First Search algorithm.
	
	/***********************************************
	 * The Close List
	 * x:0 ~ 128 
	 * y:0 ~ 128 
	 * theta: 0 ~ 360/rotateUnit
	 ***********************************************/
	boolean[][][] visted;						
	
	int minIndex;								//The index point to the node with the minimum potential value (The highest priority) in the open list.
	boolean isSuccess;							//The flag of the search result
	static final int rotateUnit = 3;			//The minimum rotation unit.
//	final float tolorentError = 0;				//the tolerance of the search result.
	Node goalNode;								//point to the goal node.

	ArrayList<Double[]> path;	

	static Logger logger = Logger.getLogger(Search.class);
	
	public Search(){
		this.potential = PotentialField.getInstance();
		this.cd = CollisionDetect.getInstance();
		
		this.robotIC = ParseFile.getInstance().robots[0].getInitialConfig();

		this.open = new ArrayList[255];
		this.visted = new boolean[MyCanvas.xPlaner][MyCanvas.yPlaner][360/rotateUnit];    
		minIndex = 254;

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
		
		
		BFS();
	}

	private int[] double2int(Node currentNode){
		int[] cNode = new int[3];
		for(int i=0; i<currentNode.getConfig().length; i++){
				cNode[i] = (int)Math.round(currentNode.getConfig()[i]);
		}
		cNode[2] %= 360;		//0 ~ 359 degree
		return cNode;
	}

	/**********************************
	 * Best First Search
	 **********************************/
	public void BFS(){					
		int[] cNode;								
		Node currentNode;							
		isSuccess = false;
		path = new ArrayList<Double[]>();
		Node initialNode = new Node(robotIC);						//to install Xinit in T
		initialNode.setPrevious(null);
		
		int[] config = double2int(initialNode);
		insertOPEN(potential.getPotential(robotIC), initialNode, config);	
		
		int counter = 300000;
		int minPotential = 1000;
		Node minNode = new Node(new Double[]{0.0, 0.0, 0.0});
		
		while(!isSuccess && !isOpenEmpty()){
			/*****************************************************************************
			 * open[min]: The node with the highest priority in the open list; 
			 * in other words, the node has the minimum potential value in the open list.
			 *****************************************************************************/
			while(open[minIndex]== null || open[minIndex].isEmpty()){		
				minIndex++;
			} 
			
			currentNode = (Node)open[minIndex].remove(0);		//the best node in the current run.
			
			if(minPotential >= minIndex){
				minPotential = minIndex;
				minNode = currentNode;
			}			

			if(--counter <= 0){ 
				goalNode = minNode;
				logger.debug("Minimum Potential:" + minPotential);
				break;
			}

			cNode = double2int(currentNode);
			
			//to explore the neighborhood.
			if(cNode[1]<127){	
			   testNeighbor(cNode[0]  ,cNode[1]+1, cNode[2], currentNode);
			}
			if(cNode[1]>0){
			   testNeighbor(cNode[0]  ,cNode[1]-1, cNode[2], currentNode);
			}
			if(cNode[0]<127){
			   testNeighbor(cNode[0]+1,cNode[1]  , cNode[2], currentNode);
			}
			if(cNode[0]>0){
			   testNeighbor(cNode[0]-1,cNode[1]  , cNode[2], currentNode);
			}			
			testNeighbor(cNode[0]  ,cNode[1]  ,cNode[2] + rotateUnit, currentNode);
			
			testNeighbor(cNode[0]  ,cNode[1]  ,cNode[2] - rotateUnit, currentNode);

		}

		if(isSuccess){
			//Back tracing the path from the goal to the initial configuration.    
			while(goalNode != null){
				path.add(0,goalNode.getConfig());
				goalNode = goalNode.getPrevious();
			}
		}
		else{
			if(minPotential <= 1){
				logger.debug("Search success, but resolution uncomplete!");
			}			
			while(goalNode != null){
				path.add(0,goalNode.getConfig());
				goalNode = goalNode.getPrevious();
			}
		}		
	}
	
	public boolean isSearchSuccess(){
		return isSuccess;
	}
	
	public ArrayList<Double[]> getPath(){
		return path;
	}
	
	//測試是否未拜訪&&不碰撞, 若是便插入OPEN(呼叫insertOpen())
	private void testNeighbor(int x, int y , int theta, Node parentNode){
		if(theta < 0)
			theta +=360;
		else if(theta >=360)
			theta -=360;
		
		
		int[] config = {x, y, theta};
		Double[] config2 = new Double[3];;
		for(int i=0; i<config.length; ++i){
			config2[i] = (double)config[i];
		} 
		
		int thetaUnit;
		thetaUnit = theta/rotateUnit;
		
		
		//測試是否拜訪過 或 碰撞
		if (visted[x][y][thetaUnit] == true || cd.isCollision(config2)){
			return;
		}	
		else{
			Node neighbor = new Node(config2);
			neighbor.setPrevious(parentNode);								//x' point toward to x
			insertOPEN(potential.getPotential(config2), neighbor, config);		//未拜訪過也不碰撞的點就加入OPEN

			/**********************************
			 * for debug
			 **********************************/
			double gx, gy, gt;
			gx = ParseFile.getInstance().goalRobots[0].getInitialConfig()[0];
			gy = ParseFile.getInstance().goalRobots[0].getInitialConfig()[1];
			gt = ParseFile.getInstance().goalRobots[0].getInitialConfig()[2];

			//if(potential.getPotential(config2) <= tolorentError){
			if(gx ==x && gy ==y && gt == theta){
				isSuccess = true;
				goalNode = neighbor;
				
				logger.debug("gNode equal to bestNode");
				logger.debug("gNode:" + gx+", "+gy+", "+gt);
				logger.debug("bestNode:" + x+", "+y+", "+theta);
				logger.debug("*********************************");
			}
		}	
	}
	

	private boolean isOpenEmpty(){
		for(ArrayList<Node> a:open){
			if(a != null){
				if(!a.isEmpty()){
					return false;				//return Open is not Empty
				}
			}
		}
		return true;						   //return Open is Empty
	}
	
	
	//將node插入OPEN,設為已拜訪,並更新min的index
	private void insertOPEN(double potential ,Node node, int[] config ){
		int x = config[0],
			y = config[1],
			theta = config[2]/rotateUnit,
		    key = (int)Math.round(potential);
		
		if (open[key] == null){
			open[key] = new ArrayList();
		}
		open[key].add(0,node);		//插在List的第一個element
		
		if (key <= this.minIndex)
			this.minIndex = key;
    
		visted[x][y][theta] = true;				
	}
}