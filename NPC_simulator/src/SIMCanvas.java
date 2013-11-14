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
	boolean current;				//�O���ثe�O�_�첾��.
	Sprite targetSprite;
	Point clickPoint;
	Point dragPoint;
	static Random rand = new Random(System.currentTimeMillis());
	Engine engine;
	
	public SIMCanvas(Engine engine){
		this.engine = engine;
		this.width = engine.canvasWidth;
		this.height = engine.canvasHeight;
		setPreferredSize(new Dimension(width, height));    //�w�]�e�����j�p
		this.spriteADM = engine.getSpriteADM();
		initial();
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;      //�ϥ�Java2D
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);  //�ĥάX��
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
				//�k��
				if(e.getModifiers() == MouseEvent.META_MASK){
					//�k���I�ťճB,�i���ܩҦ��I����V.
					if(targetSprite == null)
						spriteADM.changeAllSpriteVel(clickPoint);
					else
						current = true;		   //�Ұʩ첾
				}
				//����
				else{
					//�����I�ťճB,�i��sprite.
					if(targetSprite == null){
						//�ѨϥΪ̿�ܪ�velocity,��BA���A�ͦ�Sprite.
						Point velocity = new Point(Engine.customize_V,Engine.customize_V);
						Sprite s = new DirectionalSprite(engine.getSprite(), clickPoint, velocity, 
								Engine.customize_BA, width, height, Math.abs(rand.nextInt()%8));
						spriteADM.addSprite(s);
						repaint();
					}
					else{
						current = true;    //�Ұʩ첾
					}
				}
			}
			
			public void mouseReleased(MouseEvent e){
				current= false;        //�Ѱ��첾���A
			}
			
		});
		
		addMouseMotionListener(new MouseMotionAdapter(){
			int dx,dy;
			public void mouseDragged(MouseEvent e){
				if(current == false) return;	//�ƹ����N���}�K�����첾
				Rectangle targetP = targetSprite.position;
				Point targetV = targetSprite.velocity;
				dragPoint = e.getPoint();
				//�p�G�ϥΪ̬O�Υk��첾,�K�վ�t��.
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
				//�p�G�ϥΪ̬O�Υ���첾,�K�վ㥭��.
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
