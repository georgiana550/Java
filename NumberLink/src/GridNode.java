import java.awt.Point;


public class GridNode {
	private int value;
	private Point position;
	private GridNode parent;
	
	public GridNode(int v, int x, int y){
		this.value = v;
		this.position = new Point(x,y);
		this.parent = null;
	}
	
	public int getValue(){
		return value;
	}
	
	public void setValue(int v){
		this.value = v;
	}
	
	public Point getPosition(){
		return position;
	}
	
	public void setPosition(int x, int y){
		this.position.setLocation(x,y);
	}
	
	public GridNode getParent(){
		return this.parent;
	}
	
	public void setParent(GridNode p){
		this.parent = p;
	}
	
	//印出的的x,y座標都少1.(因為從0開始算)
	public String toString(){
		String s1,s2,s3;
		s1 = "v:";
		s2 = String.valueOf(value);
		s3 = String.valueOf(position);
		return s1+s2+s3;
	}
	
}
