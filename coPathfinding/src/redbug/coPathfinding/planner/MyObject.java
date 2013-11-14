package redbug.coPathfinding.planner;
import java.awt.geom.*;
import java.util.*;

public class MyObject implements Cloneable{
	ArrayList<Point2D[]> polygonArrayList;	       //The polygons which form this object.		
	Double[] initialConfig;

	public MyObject(int numPolygon){
		this.polygonArrayList = new ArrayList<Point2D[]>(numPolygon);
		this.initialConfig = null;
	}
	
	public Object clone(){
		Object o = null;
		try{
			o = super.clone();
		}catch(CloneNotSupportedException e){
			System.err.println("MyObject can't clone");
		}
		return o;
	}
	
	public void setInitailConfig(Double[] config){
		initialConfig = config.clone(); 
	}
	
	public void addPolygon(Point2D[] vertex){
		polygonArrayList.add(vertex);
	}
	
	public ArrayList<Point2D[]> getPolygonArrayList(){
		return polygonArrayList;
	}
	
	public Double[] getInitialConfig(){
		return initialConfig;
	}
	
	public void modifyPositoin(double x, double y){
		this.initialConfig[0] += x;
		this.initialConfig[1] += y;
	}
	
	public void modifyTheta(double t){
		this.initialConfig[2] = this.initialConfig[2]+ t;
		if (this.initialConfig[2] < 0)
			this.initialConfig[2] += 360;
		else if (this.initialConfig[2] >= 360)
			this.initialConfig[2] -= 360;
	}
	
}

