package redbug.coPathfinding.prm;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import redbug.coPathfinding.planner.MyCanvas;
import redbug.coPathfinding.planner.MyMath;
import redbug.coPathfinding.planner.MyObstacle;
import redbug.coPathfinding.planner.ParseFile;


public class PRMplanar {
	private static PRMplanar prmPlanar = new PRMplanar();
                                                             
	private final int 
		MAX_DIST 		= 3,				//this is the maximum range for NF1 to find nearestNeighbor.
		MAX_NEIGHBORS 	= 4,				//this is the upper bound number of neighbors for NF1 to find nearestNeighbor.
		XPLANNER = MyCanvas.xPlaner,
		YPLANNER = MyCanvas.yPlaner,
		POINT_OCCUPY = 99,					// sampling points 
		OBSTACLE_OCCUPY = 100,
		NUM_SAMPLINGPOINT = 1000,
		EXPAND_FACTOR = 3;					//using for computing expanded geometry
	
	//public Random rand = new Random(System.currentTimeMillis());
	public Random rand = new Random(3);
	
	byte[][]  	bitMap;			//for recording the location of obstacles and sampling points.
	boolean[][] visitedMap;
	
	HashMap<Integer, HashSet<Integer>> adjacentList;  //nodeId -> neighbors (graph structure)	
	
	/***************************************************
	 * The structure for Find-Union algorithm
	 **************************************************/
	HashMap<Integer, Integer> parentSet;  			  //self -> parent(component root)		
	HashMap<Integer, Integer> rankSet;				  //level
	HashSet<Integer> componentSet;					  //a set of graph components
	HashMap<Integer,Color> componetColorMap;
		
	ArrayList<Point> pointList;  				  // it's not necessary to exist because it could be replaced by the bitMap[][].  	
	ArrayList<Point> specialNodeList;
	
	HashMap<Integer, ArrayList<Point> > resultPaths;  //robot's id -> the path of robot's movement.
	
	ParseFile pf = ParseFile.getInstance();
	
	
	private PRMplanar(){
		bitMap = new byte[XPLANNER][YPLANNER];
		adjacentList = new HashMap<Integer, HashSet<Integer>>();
		parentSet = new HashMap<Integer, Integer>();
		rankSet   = new HashMap<Integer, Integer>();
		componentSet = new HashSet<Integer>();
		componetColorMap = new HashMap<Integer, Color>();
		pointList = new ArrayList<Point>();
		specialNodeList = new ArrayList<Point>();
		resultPaths = new HashMap<Integer, ArrayList<Point>>(); 
	}

	public static PRMplanar getInstance(){
		return prmPlanar;
	}
	
	public void clearResultPaths(){
		resultPaths.clear();
	}
	
	/******************************************
	 * simple demo case
	 ******************************************/
	public void defaultTestingRobotConfig(){
		Point p;
		int n = 0;
		int i = 0;
		Object nIdArray[] = adjacentList.keySet().toArray();
		while(n++ < 60){
			i += 43;
			if(i >= nIdArray.length){
				i -= nIdArray.length;
			}
			p = MyMath.nodeId2Point((Integer)nIdArray[i]);
			addSpecialNode(p.x, p.y);
		}
	}	
	
	/******************************************
	 * for testing case
	 ******************************************/	
	public void plotTestingPoints(){
		initailBitMap();
		for(int i=2; i < 120; i+=2){
			for(int j=2; j<60; j+=2){    
					bitMap[i][j] = POINT_OCCUPY;
					pointList.add(new Point(i,j));
			}
		}	
	}
	
	
	public void addOnePath(int robotId, ArrayList<Point> p){
		if(!resultPaths.containsKey(robotId)){
			resultPaths.put(robotId, p);
		}
		else{
			resultPaths.get(robotId).addAll(p); 
		}
	} 
	
	public boolean isExistInGraph(int nodeId){
		return adjacentList.containsKey(nodeId);
	}
	
	public boolean isSameComponent(int nid1, int nid2){
		return parentSet.get(nid1).equals(parentSet.get(nid2));
	}
	
	public HashMap<Integer, ArrayList<Point> > getResultPaths() {
		return resultPaths;
	}
	
	public ArrayList<Point> getSpecialNodeList(){
		return specialNodeList;
	}
	
	public HashMap<Integer,Color> getComponetColorMap(){
		return componetColorMap;
	}
	
	public HashMap<Integer, Integer> getParentSet(){
		return parentSet;
	} 
	
	public ArrayList<Point> getSamplingConfigs(){
		return pointList;
	}
	
	public HashMap<Integer, HashSet<Integer>> getAdjacentList(){
		return adjacentList;
	}
	
	public void resetMap(){
		adjacentList.clear();	
		parentSet.clear();
		rankSet.clear();
		componentSet.clear();
		componetColorMap.clear();
	}
	
	public void cleanAll(){
		resetMap();
		pointList.clear();
		specialNodeList.clear();
		resultPaths.clear();
		
		for(int i=0; i < XPLANNER; i++){
			for(int j=0; j <YPLANNER; j++){
				bitMap[i][j]=0;
			}
		}
	}


	
	/****************************************************************************************************
	 * Computing the expanding vector for each point for the purpose that getting the expanded geometry.
	 * There are two vectors contribute to each expanding vector.
	 * (you can refer the illustration on the prmplaner.ppt)
	 ***************************************************************************************************/
	private Point2D[] caculateExpandVector(Point2D[] obstacle){	
		Point2D q1,q2;
		int numVertex = obstacle.length;
		int nextVertex;
		Point2D[][] twoVectorOfVertex = new Point2D[numVertex][2];	// there are two vectors which contribute to the expanded vector.
		
		q1 = new Point2D.Double();
		q2 = new Point2D.Double();
		
		/****************************************************************************************************
		 * Tracing the each edge of a polygon and finding out the right hand side normal vector of each edge.  
		 ****************************************************************************************************/
		for(int vertex = 0; vertex < numVertex; vertex++){
			if(vertex == 0){												//loop1 取polygon第一個vertex
				q1 = obstacle[vertex];
			}else{
				q1 = q2;												//loop2之後都跑這.
			}
			
			//若q1是最後一個vertex,則q2返回第一個點..即檢查最後一個點,與第一個點的連接線段.
			if(vertex == numVertex - 1){
				nextVertex = 0;
				q2 = obstacle[0];
			}
			else{
				nextVertex = vertex + 1;
				q2 = obstacle[vertex+1];
			}
			
			twoVectorOfVertex[vertex][0]   
			= twoVectorOfVertex[nextVertex][1]
			= MyMath.findRightHandSideNormal(MyMath.vector(q1, q2));
		}
	
		
		Point2D[] expandedVectors = new Point2D[numVertex];
		Point2D vec1, vec2;
		for(int vertex = 0; vertex < numVertex; vertex++){
			vec1 = MyMath.normalize(twoVectorOfVertex[vertex][0]);
			vec2 = MyMath.normalize(twoVectorOfVertex[vertex][1]);
			expandedVectors[vertex] = MyMath.normalize(new Point2D.Double(vec1.getX()+vec2.getX(), vec1.getY()+vec2.getY()));
		}	
		return expandedVectors;
	}
	
	
	public void initailBitMap(){
		//以下暫存obstacle的bound邊界
	    int maxX;
	    int minX;
	    int maxY;
	    int minY;
	    
		MyObstacle obstacles[];
		obstacles = new MyObstacle[pf.getAllObstacles().length];
		System.arraycopy(pf.getAllObstacles(),0, obstacles,0, pf.getAllObstacles().length);	   
		
		Double[] initialConfig;    
		
		GeneralPath obstacle = new GeneralPath();
		
		Point2D[] 		vertexs, expandVector;
		Rectangle2D 	bound;				//bouding box of obstacle.
		float vx, vy;
		
		/***********************************************
		 * To retrieve all obstacles
		 ***********************************************/
		for(int i = 0;i < obstacles.length; i++){
			initialConfig = obstacles[i].getInitialConfig();
			
			Iterator<Point2D[]> it = obstacles[i].getPolygonArrayList().iterator();
			
			/******************************************************
			 * To retrieve all polygon of an obstacle.
			 ******************************************************/
			while(it.hasNext()){
				vertexs =it.next();
				
				/**************************************************************************
				 * To compute all expanding vertexes and getting the expanded geometries.
				 **************************************************************************/
				expandVector = caculateExpandVector(vertexs);
				vx = (float)(vertexs[0].getX() + EXPAND_FACTOR * expandVector[0].getX());
				vy = (float)(vertexs[0].getY() + EXPAND_FACTOR * expandVector[0].getY());
				obstacle.moveTo(vx,vy);
							
				for(int j = 1; j < vertexs.length; j++){
					vx = (float)(vertexs[j].getX() + EXPAND_FACTOR * expandVector[j].getX());
					vy = (float)(vertexs[j].getY() + EXPAND_FACTOR * expandVector[j].getY());
					obstacle.lineTo(vx,vy);
  				}
				obstacle.closePath();
			}	
			
			//其實y值不必要反轉, 這只是方便drawBitmap Debug較直觀, 但與 MyRobot中取得getControlPoint()需一致.
			obstacle.transform(MyMath.getP2CMatrix(initialConfig, YPLANNER,1f ,1f));

			
			bound = obstacle.getBounds2D();
			minX = (int)bound.getX();
			minY = (int)bound.getY();
			maxX = minX+(int)bound.getWidth();
			maxY = minY+(int)bound.getHeight();

			
			//填Obstacle bitmap
			for(int y = minY;y <= maxY;y++){
				for(int x = minX;x <= maxX;x++){
					if(obstacle.contains(x,y)){
						if(x >= 0 && x < XPLANNER && y >= 0 && y < YPLANNER){
							bitMap[x][y]= OBSTACLE_OCCUPY;
						}	
					}
				}
			}
			obstacle.reset();	
		}		
//		drawBitmapOnConsole(bitMap);
	}
	
	
	
	/****************************************************
	 * Add a normal node into the graph.
	 ****************************************************/
	public void addOnePoint(int x, int y){
		Point p;			
		//To look up the bitmap table directly for checking whether the point is located in Cfree or not.
		if(bitMap[x][y] != OBSTACLE_OCCUPY){	
			if(bitMap[x][y] != POINT_OCCUPY){
				bitMap[x][y] = POINT_OCCUPY;
				pointList.add( p = new Point(x,y));
			}else{
				System.out.println("There already was a point in this position.");
			}
		}else{
			System.out.println("you can't add one node in obstacle!!");
		}	
	}
	
	
	/****************************************************
	 * Add a special node into the graph.
	 ****************************************************/	
	public void addSpecialNode(int x, int y){
		Point p = new Point(x, y);			
		//To look up the bitmap table directly for checking whether the point is located in Cfree or not.
		if(bitMap[x][y] != OBSTACLE_OCCUPY){	
			
			if(bitMap[x][y] != POINT_OCCUPY){
				bitMap[x][y] = POINT_OCCUPY;
				pointList.add(p);
			}
			specialNodeList.add(p);		
		}else{
			System.out.println("you can't add one node in obstacle!!");
		}
	}
	
	
	
	/**********************************************************************
	 * Doing node sampling on the canvas by uniform distribution.
	 **********************************************************************/
	public void uniformRandomSimpling(int xRange, int yRange){
		Point p;
		initailBitMap();
		for(int i=0; i<NUM_SAMPLINGPOINT; ++i){
			p = new Point(rand.nextInt(xRange), rand.nextInt(yRange));
			
			//To look up the bitmap table directly for checking whether the point is located in Cfree or not.
			if(bitMap[p.x][p.y]!=OBSTACLE_OCCUPY){	
				if(bitMap[p.x][p.y] != POINT_OCCUPY){
					bitMap[p.x][p.y] = POINT_OCCUPY;
					pointList.add(p);
				}
			}	
		}
	}
	
	/**********************************************************************
	 * Doing node sampling on the canvas by uniform distribution.
	 **********************************************************************/
	public void plotGrid(int xRange, int yRange){
		Point p;
		initailBitMap();
		for(int i=0; i<xRange; ++i){
			for(int j=0; j< yRange; ++j){
				p = new Point(i, j);	
				//To look up the bitmap table directly for checking whether the point is located in Cfree or not.
				if(bitMap[p.x][p.y]!=OBSTACLE_OCCUPY){	
					if(bitMap[p.x][p.y] != POINT_OCCUPY){
						bitMap[p.x][p.y] = POINT_OCCUPY;
						pointList.add(p);
					}
				}
			}	
		}
	}
	
		
	/*************************************
	 * Find Union Algorithm
	 *************************************/
	//回傳該node所屬的集合的代表node.
	int Find_Set(int x)
	{			
	   int parent = parentSet.get(x);
	   //若x不是root, 便將parent指向root, 並回傳root node.
	   if(x != parent){
	      parentSet.put(x, Find_Set(parent)); //recursive find root.
	   }
	   return parentSet.get(x);   
	}

	//相連兩個集合樹. 
	boolean Link(int x, int y)
	{ 
	  if( x == y)     //x,y屬同一個Set, 不需相連. 
	    return false;
	  
	  int rankX = rankSet.get(x);
	  int rankY = rankSet.get(y);
	  
	  //小樹串到大樹下.     
	  if(rankX > rankY){
		  //parentSet.put(y, x);
		  changeParentSet(y, x);
	  }  
	  else{
		  //parentSet.put(x, y);
		  changeParentSet(x, y);
		  if(rankX == rankY){     //兩棵樹一樣高時, 合成樹的高度會長高一個單位. 
			  rankSet.put(y, ++rankY);
		  }  
	  }           
	  return true;
	}

	
	
	/****************************************************************
	 * updating the parentSet and compoentSet, simultaneously.
	 ****************************************************************/
	private void changeParentSet(int child, int parent){
		parentSet.put(child, parent);
		if(child != parent){
			componentSet.remove(child);
			componentSet.add(parent);
		}else{
			componentSet.add(parent);
		}
	}
	
	
	/**************************************************
	 * 聯集(呼叫前兩個function)
	 * Return Value:
	 * 	True: no Cycle
	 *  False: Cycle
	 **************************************************/
	boolean Union(int x, int y)
	{ 
	   return Link(Find_Set(x),Find_Set(y));  //找到兩個node分屬的Set, 並將兩個set相連. 
	}	
	
	
	
	
	/**********************************************************************
	 * Discretize the line segment into a number of configurations.
	 **********************************************************************/
	public ArrayList<Point> generateMidPoint(int x0, int y0, int x1, int y1){
		ArrayList<Point> midPoint = new ArrayList<Point>();
		double y, x, dx, dy, m;
		
		dy = y1 - y0;
		dx = x1 - x0;
		m = dy/dx;
		
		int min,max;
		if(x0 == x1){
			if(y0 < y1){
				min = y0;
				max = y1;
			}else{
				min = y1;
				max = y0;
			}
			while(min < max){
				midPoint.add(new Point(x0,min++));
			}
		}else{
			if(Math.abs(m) <=1){
				if(x0 < x1){
					min = x0;
					max = x1;
					y = y0;
				}else{
					min = x1;
					max = x0;
					y = y1;
				}

				for(x = min; x < max; x++){
					midPoint.add(new Point((int)x, (int)Math.round(y)));
					y += m;
				}
			}else{
				if(y0 < y1){
					min = y0;
					max = y1;
					x = x0;
				}else{
					min = y1;
					max = y0;
					x = x1;
				}
				
				for(y = min; y < max; y++){
//					System.out.println("x:"+(int)(Math.round(x))+" y:"+ (int)y);
					midPoint.add(new Point((int)(Math.round(x)), (int)y));
					x += 1/m;
				}
			}
		}	
		return midPoint;
	}
	
		
	/************************************************************
	 *  The helper function of NF1
	 ************************************************************/
	private void testNeighbor(int x, int y, LinkedList<Point> waveFront){
		if(bitMap[x][y]!=OBSTACLE_OCCUPY && visitedMap[x][y] == false){
			visitedMap[x][y] = true;
			waveFront.add(new Point(x,y));     			//加入第i+1個波
		}
	}
	
	
	
	/**************************************************************************************************
	 * The fixed version of NF1 for finding nearest neighbors.
	 * Look, the number of neighbors of the any node is not necessary as the same as the MAX_NEIGHBORS.
	 * (it may less or larger than the MAX_NEIGHBORS)  
	 *************************************************************************************************/
	public boolean NF1(Point p, int nodeId, HashSet<Integer> candidateNeighbor){

		//if the number of neighbor of this node is larger or equal to MAX_NEIGHBORS, don't add any more neighbor.
		int neighborCounter = candidateNeighbor.size();
		if( neighborCounter >= MAX_NEIGHBORS){
			return true;
		}
		
		LinkedList<Point> waveBack,waveFront,Lt;
		waveBack = new LinkedList<Point>();
		waveFront = new LinkedList<Point>();
		
		int distance = 0;
		Point q;
		q = new Point(p);		
		waveBack.add(q); 							//先把goal放入第0波
		int x, y, neighborNodeId;
		
		boolean flag = false;
		boolean obstacleIntersect;
		
		HashSet<Integer> tmpSet;
		ArrayList<Point> midPoint;
		
		visitedMap = new boolean[XPLANNER][YPLANNER];
		
		visitedMap[p.x][p.y] = true;				//自己本身為已拜訪.
		
		while(distance <= MAX_DIST && !waveBack.isEmpty()){        		//直到波前死光
			Iterator<Point> it = waveBack.iterator();
			obstacleIntersect = false;
			
			while(it.hasNext()){			//測試波前所有的點
				q = it.next();
				x = q.x;
				y = q.y;
				
				//find candidate neighbor!
				if(bitMap[x][y] == POINT_OCCUPY){				
					if(!q.equals(p)){		//except the point p itself.					
						/***********************************************************
						 * Detecting obstacle intersection (2008/12/14)
						 ***********************************************************/
						midPoint = generateMidPoint(p.x, p.y, q.x, q.y);		

						for(Point midp: midPoint){			
							if(bitMap[midp.x][midp.y] == OBSTACLE_OCCUPY){					
								obstacleIntersect = true;							
								break;
							}	
						}						
						
						if(obstacleIntersect == false){
							/***************************************************************************************************
							 * If this node adding neighbor was failed, that means the neighbor has already count in before. 
							 * Thus, we don't need to deal with the adjacentList. 
							 * Otherwise, we need to check whether the neighbor has existed in adjacent list or not 
							 * ,and add this node into the candidate neighbor set of the neighbor.
							 ***************************************************************************************************/
							neighborNodeId = MyMath.point2NodeId(q);
							
							//if the number of neighbor of neighbor node is larger or equal than MAX_NEIGHBORS, don't connect to it.
							if(adjacentList.get(neighborNodeId) != null && 
							   adjacentList.get(neighborNodeId).size() >= MAX_NEIGHBORS){
								continue;
							}
							
							flag = candidateNeighbor.add(neighborNodeId);			//neighbor was added by this node.
							
							if(flag){		
								if(!adjacentList.containsKey(neighborNodeId)){		//the neighbor hasn't count in adjacent list.
									tmpSet = new HashSet<Integer>();
									adjacentList.put(neighborNodeId, tmpSet);
									changeParentSet(neighborNodeId, neighborNodeId);
									rankSet.put(neighborNodeId, 0);
				
								/*************************************************************************************** 
								 * the neighbor has already exist in adjacent list but hasn't connected with this node. 
								 ****************************************************************************************/
								}else{								
									tmpSet = adjacentList.get(neighborNodeId);
								}	
	
								Union(nodeId, neighborNodeId);				//using Find-Union algorithm for checking cycle.
																	
								tmpSet.add(nodeId);									//this node was added by neighbor.
								adjacentList.put(neighborNodeId, tmpSet);
								++neighborCounter;
								
								if( neighborCounter >= MAX_NEIGHBORS){
									return true;
								}
									
							}
						}	
					}
				}
					
				
				if(x > 0){           	//checking left boundary
					testNeighbor(x-1,y,waveFront);	//測試左鄰居是否拜訪
				}
				if(x < XPLANNER-1){		//checking right boundary
					testNeighbor(x+1,y,waveFront);	//測試右鄰居是否拜訪
				}
				if(y > 0){				//checking top boundary
					testNeighbor(x,y-1,waveFront);	//測試下鄰居是否拜訪
				}
				if(y < YPLANNER-1){		//checking bottom boundary
					testNeighbor(x,y+1,waveFront);	//測試右鄰居是否拜訪
				}
				
			}
			distance++;
			Lt = waveBack;
			waveBack = waveFront;
			Lt.clear();
			waveFront = Lt; 
		}
		return false;
	}

	
	/**********************************************************
	 * Make every node connected to it's candidate neighbors.
	 * return the number of components 
	 **********************************************************/
	public int connectCandidateNeighbor(){
		int nodeId;
		HashSet<Integer> candidateNeighbor;
		resetMap();			//2009/1/2
		
		for(Point p:pointList){		
			nodeId = MyMath.point2NodeId(p);
			if(!adjacentList.containsKey(nodeId)){
				candidateNeighbor = new HashSet<Integer>();
				adjacentList.put(nodeId, candidateNeighbor);
				changeParentSet(nodeId, nodeId);
				rankSet.put(nodeId, 0);
			}else{
				candidateNeighbor = adjacentList.get(nodeId);
			}	

			NF1(p, nodeId, candidateNeighbor);
		}
		
		// updating the parent of each nodes to display component correctly. 
		for(Integer a: parentSet.keySet()){
			parentSet.put(a,Find_Set(a));
		}
		
				
		int red,green, blue, alpha, t;
		red = 0;
		green = 51;
		blue = 204;
		alpha = 114;
		t = 0;

		for(Integer a : componentSet){
			if((t %= 3) == 0){
				red += 97;
				red %= 255;
			}else if (t == 1){
				green += 97;
				green %= 255;
			}else if (t == 2){
				blue += 97;
				blue %= 255;
			}
			componetColorMap.put(a, new Color(red, green, blue, alpha));
			t++;
		}		
		return componentSet.size();
	}

	
	/******************************************
	 * for testing bitmap
	 ******************************************/
	public void drawBitmapOnConsole(byte map[][]){
		for(int k=0;k<128;k++){
			for(int l=0;l<128;l++){
				if(map[l][k] == OBSTACLE_OCCUPY){
					System.out.print("*");
				}else if (map[l][k] == 50){
					System.out.print("X");
				}/*else if (map[l][k] == POINT_OCCUPY){
					System.out.print("P");
				}*/
				else{
					System.out.print("0");
				}
			}
			System.out.println();
		}
	}
	
}
