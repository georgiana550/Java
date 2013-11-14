package redbug.coPathfinding.planner;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

public class MyMath {
	private static MyMath math = new MyMath();
	private static AffineTransform p2C_Matrix;
	
	private static final int XPLANNER = MyCanvas.xPlaner,
	  				  	 	 YPLANNER = MyCanvas.yPlaner;
	
	private MyMath(){
		p2C_Matrix = new AffineTransform();
		
	}
	
	public static MyMath getInstance(){
		return math;
	}
	
	
	
	/***********************************************************************************
	 * Doing rotation and translation for moving a vertex to the correct position on planner space. 
	 * 		formula:
	 * 			x' = xcos@ - ysin@ + tx
	 * 			y' = xsin@ + ycos@ + ty
	 ***********************************************************************************/
	public static Point2D vertexTransform(double x, double y, double tx, double ty, double theta){
		return new Point2D.Double(
				x * Math.cos(Math.toRadians(theta)) - y * Math.sin(Math.toRadians(theta)) + tx,
				x * Math.sin(Math.toRadians(theta)) + y * Math.cos(Math.toRadians(theta)) + ty
		);
		
	}
	
	
	/***********************************************************************************
	 * Doing rotation and translation for moving a vertex to the correct position on planner space. 
	 * 		formula:
	 * 			x' = xcos@ - ysin@ + tx
	 * 			y' = yPlaner - (xsin@ + ycos@ + ty)
	 ***********************************************************************************/
	public static Point2D vertexTransform2(double x, double y, double tx, double ty, double theta, int yPlaner){
		return new Point2D.Double(
				x * Math.cos(Math.toRadians(theta)) - y * Math.sin(Math.toRadians(theta)) + tx,
				yPlaner - (x * Math.sin(Math.toRadians(theta)) + y * Math.cos(Math.toRadians(theta)) + ty)
		);
		
	}
	
	
	
	/*****************************************************************
	 * Coordination transformation from planer space to canvas space.
	 *****************************************************************/
	public static AffineTransform getP2CMatrix(Double[] initialConfig, float cHeight, float scaleX, float scaleY){
		p2C_Matrix.setTransform(1,0,0,-1,0,cHeight); //Cy = cHeight - Py.
		
		/*****************************************************************************************************
		 * KeyPoint: 記住! 座標轉換的操作, 是對"原點"座標系統做轉換.
		 * 可以想像, scale operation是欲把原本128*128的planar space 放大至512 * 512的座標系,
		 * 但tic mark的數目仍保持不變為128*128, 但間距變大4倍.(所以非0的位置,會因scale operation而產生位移.)
		 * 
		 * 接著在scale後的座標系統中位移object的原點座標, 
		 * 雖然位移的單位數不變, 但因為間距變大4倍, 所以原點位移的距離是原本座標系統的4倍.
		 * (有點像到了另一個星球, 由於重力不同, 移動的單位距離便不同了.)
		 * 
		 * object的原點座標位移定位後, object便以新的原點座標為基準做旋轉.
		 *****************************************************************************************************/
		p2C_Matrix.scale(scaleX,scaleY);
		p2C_Matrix.translate(initialConfig[0],initialConfig[1]);
		p2C_Matrix.rotate(Math.toRadians(initialConfig[2]));
		return p2C_Matrix;
	}
	
	
	
	
	/*************************************************************************************************************
	 * Testing whether a point lies on one of the interior of the polygon or not.
	 * 		Solution:
	 * 			If the polygon is "convex" then one can consider the polygon as a "path" from the first vertex.(go around counterclockwise) 
	 * 			A point is on the interior of this polygons if it is always on the same side of all the line 
	 * 			segments making up the path. 
	 * 			
	 *************************************************************************************************************/
	public static boolean isInsideAPolygon(Point2D p, ArrayList<Point2D[]> obstaclePlanarSpace){
		Iterator<Point2D[]> it2 = obstaclePlanarSpace.iterator();
		Point2D[] obPolygon;
		Point2D q1,q2;
		q1 = new Point2D.Double();
		q2 = new Point2D.Double();
		boolean flag = false;
		
		while(it2.hasNext()){
			obPolygon = it2.next();
			//取出polygon中的每個vertex.
			for(int vertex=0; vertex < obPolygon.length;vertex++){
				flag = true;
				
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
				
				//if point lies on the right of a line segment, point doesn't lies on the interior of this this polygon.
				if(crossProduct(vector(q2,q1),vector(p,q1)) < 0){
					flag = false;
					break;
				}
			}
			if(flag == true)
				return flag;	//point lies on one of the interior of polygons.
		}	
		return flag;			//always return false here!
	}
	
	
	/******************************************
	 * Constructing a polygon from vertexes.
	 ******************************************/
	public static void constructPolygon(Point2D[] vertexs, GeneralPath polygon, boolean isReset){
		if(isReset) polygon.reset();
		
		polygon.moveTo((float)(vertexs[0].getX()),(float)(vertexs[0].getY()));
		for(int j = 1; j < vertexs.length; j++){
			polygon.lineTo((float)(vertexs[j].getX()),(float)(vertexs[j].getY()));
		}
		polygon.closePath();
	}
	
	
	
	/*********************************************************************************
	 * Using crossProduct to test line intersection.
	 * Two line intersect each other, if both conditions below are satisfied.
	 * 		p1 and p2 are located in different sides of line from q1 to q2.
	 * 		q1 and q2 are located in different sides of line from p1 to p2.
	 *********************************************************************************/
	public static boolean detectLineIntersection(Point2D p1, Point2D p2, Point2D q1, Point2D q2){
		return (crossProduct(vector(p1,q1),vector(q2,q1))* crossProduct(vector(p2,q1),vector(q2,q1)) <=0 &&
				crossProduct(vector(q1,p1),vector(p2,p1))* crossProduct(vector(q2,p1),vector(p2,p1)) <=0);
	}
	
	
	
	/***************************************************
	 * input: two vector -> p,q
	 * output: p cross q > 0, counterclockwise
	 * 		   p cross q < 0, clock wise 
	 ***************************************************/
	public static double crossProduct(Point2D p, Point2D q){
		return p.getX()* q.getY() - p.getY()* q.getX();
	}
	
	
	
	/***************************************************
	 * Vector p->q.
	 ***************************************************/
	public static Point2D vector(Point2D p ,Point2D q){
		Point2D v = new Point2D.Double();
		v.setLocation(q.getX() - p.getX(), q.getY() - p.getY());
		return v;
	}
	
	/****************************************************
	 * return the length of a vector.
	 ****************************************************/
	public static double vectorLength(Point2D vec){
		double x = vec.getX();
		double y = vec.getY();
		
		return Math.sqrt( x * x + y * y);
	}
	
	/****************************************************
	 * vector normalization 
	 ****************************************************/
	public static Point2D normalize(Point2D vec){
		double length = vectorLength(vec);
		return new Point2D.Double(vec.getX()/length, vec.getY()/length);
	}
	
	/*********************************************************************
	 * finding the normal vector on the clockwise direction of inputing vector.
	 *********************************************************************/
	public static Point2D findRightHandSideNormal(Point2D vec){
		return new Point2D.Double(vec.getY(), -vec.getX());
	}
	
	
	
	/***************************************************
	 * Transforming nodeId to Point.
	 ***************************************************/
	public static Point nodeId2Point(int nodeId){
		return new Point(nodeId % XPLANNER, nodeId / XPLANNER);
	}
	
	
	/***************************************************
	 * Transforming Point to nodeId. 
	 ***************************************************/	
	public static int point2NodeId(Point p){
		return p.y * XPLANNER + p.x;
	}
	
	
}
