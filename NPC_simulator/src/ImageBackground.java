import java.awt.Graphics2D;
import java.awt.Image;

import javax.swing.ImageIcon;

public class ImageBackground extends Background {
	Image image;
	public ImageBackground(int width, int height, Image img) {
		super(width, height);
		this.image = img;
	}
	
	public Image getImage(){
		return this.image;
	}
	
	public void setImage(Image img){
		this.image = img;
	}
	
	@Override
	public void draw(Graphics2D g) {
		g.drawImage(image,0,0,null);
	}

}
