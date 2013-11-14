import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;


import javax.swing.JPanel;

class SIMCanvas extends JPanel {//implements Runnable{
	int width;
	int height;
	SpriteADM spriteADM;
	boolean current;				//記錄目前是否拖移中.
	Sprite targetSprite;
	Point clickPoint;
	Point dragPoint;
	static Random rand = new Random(System.currentTimeMillis());
	Engine engine;
	
	public SIMCanvas(Engine engine){
		this.engine = engine;
		this.width = engine.canvasWidth;
		this.height = engine.canvasHeight;
		setPreferredSize(new Dimension(width, height));    //預設畫布的大小
		this.spriteADM = engine.getSpriteADM();
		initial();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;      //使用Java2D
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);  //採用柔邊
		g2.setPaint(Color.red);
		engine.drawBackround(g2);
		if(spriteADM.getSpriteList() != null){
			spriteADM.drawSprites(g2);
		}
	}
	public void initial(){
		
		addMouseListener(new MouseAdapter(){	
			public void mousePressed(MouseEvent e){	
				targetSprite = spriteADM.isPointInSprite(clickPoint = e.getPoint());
				//右鍵
				if(e.getModifiers() == MouseEvent.META_MASK){
					//右鍵點空白處,可改變所有點的方向.
					if(targetSprite == null)
						spriteADM.changeAllSpriteVel(clickPoint);
					else
						current = true;		   //啟動拖移
				}
				//左鍵
				else{
					//左鍵點空白處,可生sprite.
					if(targetSprite == null){
						//由使用者選擇的velocity,及BA狀態生成Sprite.
						Point velocity = new Point(Engine.customize_V,Engine.customize_V);
						Sprite s = new DirectionalSprite(engine.getSprite(), clickPoint, velocity, 
								Engine.customize_BA, width, height, Math.abs(rand.nextInt()%8));
						spriteADM.addSprite(s);
						repaint();
					}
					else{
						current = true;    //啟動拖移
					}
				}
			}
			
			public void mouseReleased(MouseEvent e){
				current= false;        //解除拖移狀態
			}
			
		});
		
		addMouseMotionListener(new MouseMotionAdapter(){
			int dx,dy;
			public void mouseDragged(MouseEvent e){
				if(current == false) return;	//滑鼠任意鍵放開便結束拖移
				Rectangle targetP = targetSprite.position;
				Point targetV = targetSprite.velocity;
				dragPoint = e.getPoint();
				//如果使用者是用右鍵拖移,便調整速度.
				if(e.getModifiers() == MouseEvent.META_MASK){
					if(dragPoint.x > clickPoint.x)
						targetSprite.changeVelocity(1,0);
					else if(dragPoint.x < clickPoint.x)
						targetSprite.changeVelocity(-1,0);
					if(dragPoint.y > clickPoint.y)
						targetSprite.changeVelocity(0,1);
					else if (dragPoint.y < clickPoint.y)
						targetSprite.changeVelocity(0,-1);
					clickPoint = dragPoint;
					repaint();
				}
				//如果使用者是用左鍵拖移,便調整平移.
				else{
					dx = dragPoint.x - clickPoint.x;
					dy = dragPoint.y - clickPoint.y; 
					clickPoint = dragPoint;
					targetSprite.setPosition(new Point(targetP.x+dx, targetP.y+dy));
					repaint();
				}
			}		
		});
	}
	
}
