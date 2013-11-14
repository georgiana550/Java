package redbug.motionPlanning.planner;
import java.awt.geom.*;
import java.util.*;


public class CollisionDetect {
	private static CollisionDetect collisionDetect = new CollisionDetect();
	private ParseFile f;
	private MyObstacle[] obstacles;
	private ArrayList<Point2D[]> robot;
	
	private ArrayList<Point2D[]> obstacleCSpace;				//暫存Obstacle[i]的所有polygon
	private Point2D[] robotPolygonCSpace;						//儲存robot[0]的所有vertexes

	private Double[] obIC;          					//Obstacle's InitialConfig
	private ArrayList<Point2D[]> obstaclePlanarSpace;	//store a copy of all vertexes of all obstacle in planar space.
	
	private CollisionDetect(){}
	
	public static CollisionDetect getInstance(){
		return collisionDetect;
	}
	
	/**********************************************************************************
	 * For testing collision detection.
	 * Firstly, every obstacle do transformation between C space and planar space.
	 **********************************************************************************/
	public void caculateObstaclePlanarSpace(){
		f = ParseFile.getInstance();
		obstacles = f.obstacles;
		robot = f.robots[0].getPolygonArrayList(); //只針對第一個robot做處理
		obstaclePlanarSpace = new ArrayList<Point2D[]>();
		
		Point2D[] polygonPlanarSpace;										
		double tX, tY, theta;
		double xCSpace, yCSpace;
		
		//Retrieving every obstacle to transform.
		for(int i = 0;i < obstacles.length; i++){	
			obstacleCSpace = obstacles[i].getPolygonArrayList();
			obIC = obstacles[i].getInitialConfig();
			tX = obIC[0];
			tY = obIC[1];
			theta = obIC[2];
			
			//Retrieving every polygons from an obstacle. 
			Iterator<Point2D[]> it2 = obstacleCSpace.iterator();
			Point2D[] polygonCSpace;						//The polygon which haven't transformed yet. 
			while(it2.hasNext()){
				polygonCSpace = it2.next();
				polygonPlanarSpace = new Point2D[polygonCSpace.length];
				//Getting every vertex and doing transformation between C space and Planar space. 
				for(int k = 0; k < polygonCSpace.length; k++){
						xCSpace = polygonCSpace[k].getX();
						yCSpace = polygonCSpace[k].getY();	
						
						polygonPlanarSpace[k] = MyMath.vertexTransform(xCSpace, yCSpace, tX, tY, theta);
				}
				obstaclePlanarSpace.add(polygonPlanarSpace);				
			}
		}
	}
	
	
	
	//測試是否碰撞. 方法:任取robot,obstacle各一線段做線段交叉測試,若有交叉即碰撞. robot撞到邊界亦return碰撞.
	public boolean isCollision(Double[] robotConfig){	
		double x, y, xPlanarSpace, yPlanarSpace;
		
		double tX = robotConfig[0],
		       tY = robotConfig[1],
		       theta = robotConfig[2];
		
		Point2D p1,p2,q1,q2;					//p1,p2是robot的任兩個相鄰點 ; q1,q2是obstacle的任兩個相鄰點
		
		p1 = new Point2D.Double();
		p2 = new Point2D.Double();
		q1 = new Point2D.Double();
		q2 = new Point2D.Double();
		
		
		Point2D vertexPlanarSpace;
		Iterator<Point2D[]> it = robot.iterator();
		//取出robots[0]的每個polygon
		
		//Retrieving every polygons from an robot.
		while(it.hasNext()){
			robotPolygonCSpace = it.next();
			
			//Getting every vertex and doing transformation between C space and Planar space. 
			for(int i = 0; i < robotPolygonCSpace.length; i++){
				if(i == 0){												//loop1 取robot第一個vertex.
					x = robotPolygonCSpace[i].getX();
					y = robotPolygonCSpace[i].getY();	
					
					vertexPlanarSpace = MyMath.vertexTransform(x, y, tX, tY, theta);
					
					xPlanarSpace = vertexPlanarSpace.getX();
					yPlanarSpace = vertexPlanarSpace.getY();
					
					//撞到邊界也算碰撞.
					if(xPlanarSpace<0 ||xPlanarSpace >= 128 ||yPlanarSpace<0 || yPlanarSpace >= 128){
						return true;
					}
					p1.setLocation(xPlanarSpace,yPlanarSpace);
				}
				else{				
					p1.setLocation(p2.getX(),p2.getY());				//loop2 之後都跑這
				}

				//若p1是最後一個vertex,則p2返回第一個點..即檢查最後一個點,與第一個點的連接線段.
				if(i == robotPolygonCSpace.length - 1){
					x = robotPolygonCSpace[0].getX();
					y = robotPolygonCSpace[0].getY();
				}
				//否則p2是p1的下一個相鄰點
				else{
					x = robotPolygonCSpace[i+1].getX();
					y = robotPolygonCSpace[i+1].getY();
				}
				
				//Doing transformation for vertex p2.
				vertexPlanarSpace = MyMath.vertexTransform(x, y, tX, tY, theta);
				xPlanarSpace = vertexPlanarSpace.getX();
				yPlanarSpace = vertexPlanarSpace.getY();
				
				//偵測是否撞到邊界,撞到邊界亦回傳碰撞
				if(xPlanarSpace<0 ||xPlanarSpace >= 128 ||yPlanarSpace<0 || yPlanarSpace >= 128){
					return true;
				}
				
				p2.setLocation(xPlanarSpace,yPlanarSpace);
				
				
				/**********************************************************
				 * Getting every transformed obstacles for testing collision detection.
				 **********************************************************/
				Iterator<Point2D[]> it2 = obstaclePlanarSpace.iterator();
				Point2D[] obPolygon;
				
				while(it2.hasNext()){
					obPolygon = it2.next();
					//取出polygon中的每個vertex.
					for(int vertex=0; vertex < obPolygon.length;vertex++){
						
						if(vertex == 0){									//loop1 取polygon第一個vertex
							q1 = obPolygon[vertex];
						}
						else{
							q1=q2;											//loop2之後都跑這.
						}	
						//若q1是最後一個vertex,則q2返回第一個點..即檢查最後一個點,與第一個點的連接線段.
						if(vertex == obPolygon.length-1){
							q2 = obPolygon[0];
						}
						else{
							q2 = obPolygon[vertex+1];
						}	

						if(MyMath.detectLineIntersection(p1,p2,q1,q2))	return true;					//collision!!
					}
				}	
			} 			  //end of for(取robot's vertex)
		} 				  //end of while(取robot's polygon)
		return false;											//not collision!!
	}

	
	//for testing
	private void print(Point2D d, String str){
		System.out.println(str+"座標 x:"+d.getX()+" y:"+d.getY());
	}
	
}
