import java.awt.Dimension;
import java.awt.Graphics2D;

abstract public class Background {
	Dimension size;
	
	
	public Background(int width, int height){
		size = new Dimension(width,height);
	}
	
	public Dimension getSize(){
		return size;
	}
	
	abstract public void draw(Graphics2D g);
}
