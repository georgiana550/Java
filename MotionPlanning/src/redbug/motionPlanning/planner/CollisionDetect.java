package redbug.motionPlanning.planner;
import java.awt.geom.*;
import java.util.*;


public class CollisionDetect {
	private static CollisionDetect collisionDetect = new CollisionDetect();
	private ParseFile f;
	private MyObstacle[] obstacles;
	private ArrayList<Point2D[]> robot;
	
	private ArrayList<Point2D[]> obstacleCSpace;				//�ȦsObstacle[i]���Ҧ�polygon
	private Point2D[] robotPolygonCSpace;						//�x�srobot[0]���Ҧ�vertexes

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
		robot = f.robots[0].getPolygonArrayList(); //�u�w��Ĥ@��robot���B�z
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
	
	
	
	//���լO�_�I��. ��k:����robot,obstacle�U�@�u�q���u�q��e����,�Y����e�Y�I��. robot������ɥ�return�I��.
	public boolean isCollision(Double[] robotConfig){	
		double x, y, xPlanarSpace, yPlanarSpace;
		
		double tX = robotConfig[0],
		       tY = robotConfig[1],
		       theta = robotConfig[2];
		
		Point2D p1,p2,q1,q2;					//p1,p2�Orobot������Ӭ۾F�I ; q1,q2�Oobstacle������Ӭ۾F�I
		
		p1 = new Point2D.Double();
		p2 = new Point2D.Double();
		q1 = new Point2D.Double();
		q2 = new Point2D.Double();
		
		
		Point2D vertexPlanarSpace;
		Iterator<Point2D[]> it = robot.iterator();
		//���Xrobots[0]���C��polygon
		
		//Retrieving every polygons from an robot.
		while(it.hasNext()){
			robotPolygonCSpace = it.next();
			
			//Getting every vertex and doing transformation between C space and Planar space. 
			for(int i = 0; i < robotPolygonCSpace.length; i++){
				if(i == 0){												//loop1 ��robot�Ĥ@��vertex.
					x = robotPolygonCSpace[i].getX();
					y = robotPolygonCSpace[i].getY();	
					
					vertexPlanarSpace = MyMath.vertexTransform(x, y, tX, tY, theta);
					
					xPlanarSpace = vertexPlanarSpace.getX();
					yPlanarSpace = vertexPlanarSpace.getY();
					
					//������ɤ]��I��.
					if(xPlanarSpace<0 ||xPlanarSpace >= 128 ||yPlanarSpace<0 || yPlanarSpace >= 128){
						return true;
					}
					p1.setLocation(xPlanarSpace,yPlanarSpace);
				}
				else{				
					p1.setLocation(p2.getX(),p2.getY());				//loop2 ���᳣�]�o
				}

				//�Yp1�O�̫�@��vertex,�hp2��^�Ĥ@���I..�Y�ˬd�̫�@���I,�P�Ĥ@���I���s���u�q.
				if(i == robotPolygonCSpace.length - 1){
					x = robotPolygonCSpace[0].getX();
					y = robotPolygonCSpace[0].getY();
				}
				//�_�hp2�Op1���U�@�Ӭ۾F�I
				else{
					x = robotPolygonCSpace[i+1].getX();
					y = robotPolygonCSpace[i+1].getY();
				}
				
				//Doing transformation for vertex p2.
				vertexPlanarSpace = MyMath.vertexTransform(x, y, tX, tY, theta);
				xPlanarSpace = vertexPlanarSpace.getX();
				yPlanarSpace = vertexPlanarSpace.getY();
				
				//�����O�_�������,������ɥ�^�ǸI��
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
					//���Xpolygon�����C��vertex.
					for(int vertex=0; vertex < obPolygon.length;vertex++){
						
						if(vertex == 0){									//loop1 ��polygon�Ĥ@��vertex
							q1 = obPolygon[vertex];
						}
						else{
							q1=q2;											//loop2���᳣�]�o.
						}	
						//�Yq1�O�̫�@��vertex,�hq2��^�Ĥ@���I..�Y�ˬd�̫�@���I,�P�Ĥ@���I���s���u�q.
						if(vertex == obPolygon.length-1){
							q2 = obPolygon[0];
						}
						else{
							q2 = obPolygon[vertex+1];
						}	

						if(MyMath.detectLineIntersection(p1,p2,q1,q2))	return true;					//collision!!
					}
				}	
			} 			  //end of for(��robot's vertex)
		} 				  //end of while(��robot's polygon)
		return false;											//not collision!!
	}

	
	//for testing
	private void print(Point2D d, String str){
		System.out.println(str+"�y�� x:"+d.getX()+" y:"+d.getY());
	}
	
}
