import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.util.Random;

import javax.swing.ImageIcon;

public class Simulator implements Runnable{
	private static Simulator simulator = new Simulator();
	
	Engine engine;
	Thread newThread; 


	public Simulator(){
		engine = new Engine();
		start();
	}
	
	public static Simulator getInstance(){
		return simulator;
	}
	

		
	public void run(){
		while(newThread!=null){
			engine.update();
		}
	}
	
	
	public void start(){
		//一開始隨機產生i個agent
		for(int i=0; i<2;i++){
			Random rand = new Random(System.currentTimeMillis());	
			Point position = new Point(Math.abs(rand.nextInt()%473), Math.abs(rand.nextInt()%417));
			Point velocity = new Point(Math.abs(rand.nextInt()%5), Math.abs(rand.nextInt()%7));
			int direct = Math.abs(rand.nextInt()%8);
			Sprite s = new DirectionalSprite(engine.getSprite(), position, velocity, Sprite.BA_BOUNCE, 
					engine.canvasWidth, engine.canvasHeight,direct);
			engine.addSprite(s);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}	
		newThread  = new Thread(this);

		newThread.start();
	}
	
	public static void main(String[] args) {
		Simulator.getInstance();
	}

}
