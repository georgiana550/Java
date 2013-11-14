package redbug.coPathfinding.planner;
import java.awt.Point;
import java.awt.geom.*;


public class MyRobot extends MyObject{
	private Double[] goalConfig;
	private Point2D[] controlPoint;
	Boolean isGoalRobot;    //標示是否為一個goalRobot

	public MyRobot(int numPolygon){
		super(numPolygon);
		goalConfig = null;
		controlPoint = null;
		isGoalRobot = false;
	}
	
	public void setIsGoalRobot(){
		isGoalRobot = true;
	}
	
	public boolean isAGoalRobot(){
		return isGoalRobot;
	}
	
	public void setGoalConfig(Double[] config){
		goalConfig = config.clone(); 
	}
	
	public void setControlPoint(Point2D[] cp){
		controlPoint = cp;
	}
	
	
	/*********************************************************
	 * To get the absolute coordinate of control point  
	 * with respect to the configuration of goal robot.
	 *********************************************************/
	public Point[] getControlPoint(int yPlaner){
		//目前只處理一個robot
		return getControlPoint(this.goalConfig, yPlaner);
	}
	
	
	/******************************************************************************************************************
	 * Purpose: When Robot moving and searching, we need to get the correct absolute coordinate of control point 
	 *          with respect to the new configuration of robot.
	 * input: Giving a configuration
	 * output: The absolute coordinate of control point with respect to the given configuration.
	 * 
	 * by the way, the control points are used for looking up the value of potential field in integer 3d array,
	 * so we just return Point but not Point2D object.
	 ******************************************************************************************************************/
	public Point[] getControlPoint(Double[] iConfig, int yPlaner){
		double tx = iConfig[0];
		double ty = iConfig[1];
		double theta = iConfig[2];
		
		double x,y;
		
		Point[] cp = new Point[2];
		Point2D tmp;
		
		for(int i=0; i<2; i++){
			cp[i] = new Point();
			x = controlPoint[i].getX();
			y = controlPoint[i].getY();
			
			tmp = MyMath.vertexTransform2(x, y, tx, ty, theta, yPlaner);
			cp[i].x = (int)Math.round(tmp.getX());
			cp[i].y = (int)Math.round(tmp.getY());
		}
		return cp;
	}
	
	
	public Double[] getGoalConfig(){
		return goalConfig;
	}
	
}
