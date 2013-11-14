package redbug.motionPlanning.planner;
import java.util.*;
import java.awt.Point;
import java.awt.geom.*;


public class PotentialField {
	private static PotentialField potentialField = new PotentialField();
	
	int bitMap[][];			   //�s�Ĥ@��controlPoint�Φ���PotentialField
	int bitMap2[][];		   //�s�ĤG��controlPoint�Φ���PotentialField

	ParseFile parseFile;

	Point2D[] vertexs;		   //�Ȧsobstacle��polygon����vertexes
	Rectangle2D bound;		   //�Ȧsobstacle���~��x��
		
	Point[] cp;   //�Ȧsrobot's controlPoint������y��
	   
	//	Planner���@�ɹw�]�j�p128*128	
	final int xPlaner;
	final int yPlaner;
		
	final int unvisited = 254;
	final int obstacleOccupy = 255;
	
	LinkedList<Point> La,Lb,Lt;

	
	private PotentialField(){
		xPlaner = MyCanvas.xPlaner;
		yPlaner = MyCanvas.yPlaner;
	}	 	
	
	public static PotentialField getInstance(){
		return potentialField;
	}
	
	public int[][] getBitmap(){
		return bitMap;
	}
	
	public boolean isCfree(Point p){
		return(bitMap[p.x][p.y] != obstacleOccupy);
	}
	
	public void initialize(){
		bitMap= new int[xPlaner][yPlaner];
		bitMap2 = new int[xPlaner][yPlaner];

		parseFile = ParseFile.getInstance();
		
		for(int i = 0; i<parseFile.robots.length; i++){
			//updating the goal configuration of robot due to the goal configuration maybe have already changed by user.
			parseFile.robots[i].setGoalConfig(parseFile.goalRobots[i].getInitialConfig());  
		}

		cp = parseFile.robots[0].getControlPoint(yPlaner);

		La = new LinkedList<Point>();
		Lb = new LinkedList<Point>();
		
		initailBitMap();				//�N��ê��Ū�JBitMap

		NF1(cp[0].x, cp[0].y, bitMap);		//���ͲĤ@��control point��potential Field
		NF1(cp[1].x, cp[1].y, bitMap2); 	//���ͲĤG��control point��potential Field
	}
	
	public void initailBitMap(){
		//�H�U�Ȧsobstacle��bound���
	    int maxX;
	    int minX;
	    int maxY;
	    int minY;
	    
		MyObstacle obstacles[];
		
		obstacles = parseFile.obstacles;
		
		Double[] initialConfig;    
		
		GeneralPath obstacle = new GeneralPath();
		
		for(int i = 0;i < obstacles.length; i++){
			initialConfig = obstacles[i].getInitialConfig();
			
			Iterator<Point2D[]> it = obstacles[i].getPolygonArrayList().iterator();

			while(it.hasNext()){
				vertexs =it.next();
				MyMath.constructPolygon(vertexs, obstacle, false);
			}	
			
			//���y�Ȥ����n����, �o�u�O��KdrawBitmap Debug�����[, ���P MyRobot�����ogetControlPoint()�ݤ@�P.
			obstacle.transform(MyMath.getP2CMatrix(initialConfig, yPlaner,1f ,1f));

			bound = obstacle.getBounds2D();
			minX = (int)bound.getX();
			minY = (int)bound.getY();
			maxX = minX+(int)bound.getWidth();
			maxY = minY+(int)bound.getHeight();

			//��Obstacle bitmap
			for(int y = minY;y <= maxY;y++){
				if(y >= 0 && y < yPlaner){
					for(int x = minX;x <= maxX;x++){
						if( x >= 0 && x < xPlaner){
							if(obstacle.contains(x,y)){
								bitMap[x][y]= obstacleOccupy;	
							}
						}	
					}
				}	
			}
			obstacle.reset();	
		}		
		
		for(int k=0;k<xPlaner;k++){
			for(int l=0;l<yPlaner;l++){
				if(bitMap[l][k] == 0)
					bitMap[l][k] = unvisited;  
			}
		}
		
		//�ƻsbitMap�@����bitMap2,�]����ê�y�ЬO�ۦP��
		for(int k=0;k<xPlaner;k++){
			for(int l=0;l<yPlaner;l++){
				bitMap2[l][k] = bitMap[l][k];
			}
		}
		
		//�]�wcontrol Point(goal)��potential ��0
		bitMap[cp[0].x][cp[0].y] = 0;
		bitMap2[cp[1].x][cp[1].y] = 0;
	}

	void testNeighbor(int x, int y, int potential, int map[][]){
		if(map[x][y] == unvisited){					
			map[x][y] = potential;			
			Lb.add(new Point(x,y));     			//�[�J��i+1�Ӫi
		}
	}
	
	public void NF1(int cpX, int cpY, int map[][]){
		int potential = map[cpX][cpY];		//��l�_�l����, �Y0
		Point q;
		q = new Point(cpX,cpY);		
		La.add(q); 							//����goal��J��0�i
		int x,y;
		
		while(!La.isEmpty()){        		//����i�e����
			Iterator<Point> it = La.iterator();
			while(it.hasNext()){			//���ժi�e�Ҧ����I
				q = it.next();
				x = q.x;
				y = q.y;
				
				if(x > 0){           						//checking left boundary
					testNeighbor(x-1,y,potential+1, map);	//���ե��F�~�O�_���X
				}
				if(x < xPlaner-1){							//checking right boundary
					testNeighbor(x+1,y,potential+1, map);	//���եk�F�~�O�_���X
				}
				if(y > 0){									//checking top boundary
					testNeighbor(x,y-1,potential+1, map);	//���դU�F�~�O�_���X
				}
				if(y < yPlaner-1){							//checking bottom boundary
					testNeighbor(x,y+1,potential+1, map);	//���եk�F�~�O�_���X
				}
				
			}
			potential++;
			Lt = La;
			La = Lb;
			Lt.clear();
			Lb = Lt; 
		}
		
		//drawBitmapOnConsole(map);
	}

	
	/***********************************
	 * For testing bitmap drawing.
	 ***********************************/
	public void drawBitmapOnConsole(int map[][]){
		for(int k=0;k<128;k++){
			for(int l=0;l<128;l++){
				if(map[l][k] == 255){
					System.out.print("***");
				}
				else if (map[l][k] == 0)
				    System.out.print("@@@");
				else if (map[l][k] == 254){ 
					System.out.print("Err");
				}
				else{
					if(bitMap[l][k] < 10)
						System.out.print("  "+map[l][k]);
					else if (map[l][k] < 100)
						System.out.print(" "+map[l][k]);
					else 
						System.out.print(map[l][k]);
				}	
			}
			System.out.println();
		}
	}
	
	/****************************************************************************
	 * Input: The configuration of moving robot.
	 * Output: The potential value of current configuration of moving robot. 
	 ****************************************************************************/
	public double getPotential(Double[] ic){
		double a = 0.5;
		double b = 0.5;
		
		// To get the control points with respect to the current configuration of moving robot.
		Point[] cp = parseFile.robots[0].getControlPoint(ic,yPlaner);
		double k = 0;
		//To look up the potential values from distinct tables of two control points , and sum them up by different weights.
		try{
			k = a * bitMap[cp[0].x][cp[0].y] + b * bitMap2[cp[1].x][cp[1].y];
		}catch(Exception e){
			System.out.println(cp[0]+","+cp[1]);
		}
		return k;
	}
	
}
