import java.awt.Color;
import java.awt.Graphics2D;

public class ColorBackground extends Background {
	protected Color color;
	public ColorBackground(int width, int height, Color c) {
		super(width, height);
		this.color = c;
	}
	
	public Color getColor(){
		return color;
	}
	
	public void setColor(Color c){
		this.color = c;
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);
		g.fillRect(0, 0, size.width, size.height);
		g.setColor(Color.BLACK);
	}
}
