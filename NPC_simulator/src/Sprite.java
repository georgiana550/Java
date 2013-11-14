import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.BitSet;
import javax.swing.ImageIcon;

public class Sprite{
	//sprite�O�_�������Ҧ�
	public static final int SA_KILL   = 0,         //���sprite������ .
							SA_RESTOREPOS = 1,     //���sprite���^��W�Ӧ�m.
    						SA_ADDSPRITE = 2;      //���sprite��sprite. 
	
	//spriteĲ�I�������ɮɪ������Ҧ�
	public static final int BA_STOP = 0,					//����
							BA_WRAP = 1,					//��V
							BA_BOUNCE = 2,					//�ϼu
							BA_DIE =3;						//���`
	
	//Point position,velocity;
Point velocity;
	//int width,height;						   //sprite���j�p
	int boundWidth,boundHeight;				   //sprite���ʪ������
	boolean visible = true;						   //Sprite�O���å�Υi��.	    	                    
	//boolean dying; 		                       //�P�_Sprite�O�_����.
	boolean bOneCycle;						   //�O�_�ۨ��b�����@��Sprite�ʵe��,�ۧڷ���(�z���ʵe).	      
	int boundsAction;						   //�ΨӰO����spriteĲ�I��ɮɪ�����.
    Image image;	
int frame,
	frameInc,
	frameDelay,
	frameTrigger;	
Rectangle position,						   //Sprite���I�����
		  collision,
		  bounds;
//int zOrder;
	//�u���@�ӹϪ�Sprite(�R�A)
	public Sprite(Image img, Point pos, Point vel, int ba, int b_Width, int b_Height){
		this.image = img;
		setPosition(new Rectangle(pos.x, pos.y, img.getWidth(null), img.getHeight(null)));
		//setVelocity(vel);
		this.velocity = vel;
		boundsAction = ba;
		frame = 0;
		frameInc =0;
		frameDelay = frameTrigger = 0;
		//zOrder = z;
		this.boundWidth = b_Width-2;			//frame���~�ؤ]���Z��
		this.boundHeight = b_Height-2; 
	}
/*	
	//���h�ӹϪ�Sprite(�ʺA)
	public Sprite(ImageIcon[] img, int f, int fi, int fd, Point pos, Point vel, int ba, int b_Width, int b_Height){
		this.image = img;
		setPosition(new Rectangle(pos.x, pos.y, img[f].getIconWidth(),img[f].getIconHeight()));
		setVelocity(vel);
		boundsAction = ba;
		frame = f;
		frameInc = fi;
		frameDelay = frameTrigger = fd;
		//zOrder = z;
		this.boundWidth = b_Width-2;			//frame���~�ؤ]���Z��
		this.boundHeight = b_Height-2; 
	}
*/
	
	/*
	public Sprite(int b_Width, int b_Height){
		this.boundWidth = b_Width-2;
		this.boundHeight = b_Height-2;
		position = new Point(0,0);			   //�w�]sprite��l��m.
		velocity = new Point(1,1);			   //�w�]sprite��l�t��.

		//�qJarŪ������
		ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("res/ANGEL_0021.GIF"));

		img = icon.getImage();
		//�w�]sprite��l�j�p
		width = icon.getIconWidth();
		height = icon.getIconHeight();
		visible = true;						   //�w�]sprite���i��.
	}
	*/
	/*
	public int getWidth(){
		return width;
	}
 
	public int getHeight(){
		return height;
	}
	*/
	public boolean isVisible(){
		return visible;
	}
	
	public void setVisible(boolean v){
		visible = v;
	}
	
	public void setVelocity(Point v){
		velocity.setLocation(v);
	}
	/*
	public void setVelocity(int x, int y){
		velocity.setLocation(x,y);
	}
	*/
	public void changeVelocity(int x, int y){
		velocity.translate(x,y);

		//velocity.setLocation(velocity.x+x,velocity.y+y);
	}
	
	public Point getVelocity(){
		return velocity;
	}
	
	public void setPosition(Rectangle p){
		position = p;
		setCollision();
	}
	
	public void setPosition(Point p){
		position.setLocation(p);
		setCollision();
	}
	
	//�]�wsprite���I�����
	public void setCollision(){
		collision = position;
	}
	
	public Rectangle getCollision(){
		return collision;
	}
	
	/*public void setPosition(int x, int y){
		position.setLocation(x,y);
	}*/
	
	public Rectangle getPosition(){
		return position;
	}
	
	//���t�ϼu
	public void setBounce() {
		velocity.x = -velocity.x;
		velocity.y = -velocity.y;
	}
	
	public void setBoundsAction(int ba)
	{ boundsAction = ba; };   						   //�]�w���Ĳ�I�������Ҧ�. 
	
	public BitSet updateState(){
		BitSet action = new BitSet();
		
		//�W�[�e��
		//incFrame();
		
		
		/*//�p�GSprite�Q�R��,�h�^��SA_KILL�T��,�q��GameEngine. 
		 if (dying)
		    return SA_KILL;    */
        
		//Update the position
		Point newPosition = new Point(position.x, position.y); 
		newPosition.translate(velocity.x, velocity.y);            //translate(dx,dy)
		
		/* �P�_sprite������ɮ�, �O�Ӭ�V, �ϼu, ���� �ΰ���. */ 
		//��V. (��z:��Sprite���������b������, ���y�Х���Ϋ������D) 
		if(boundsAction == Sprite.BA_WRAP){
			//Sprite�b����ɧ���������, �q�k��ɶ}�l�X�{.
			if(newPosition.x + position.width < 0)
				newPosition.x = boundWidth;
			//Sprite�b�k��ɧ���������, �q����ɶ}�l�X�{. 
		    else if (newPosition.x > boundWidth)
		      newPosition.x = 0 - position.width;
		    //Sprite�b�W��ɧ���������, �q�U��ɶ}�l�X�{. 
		    if ((newPosition.y + position.height) < 0)
		      newPosition.y = boundHeight;
		    //Sprite�b�U��ɧ���������, �q�W��ɶ}�l�X�{. 
		    else if (newPosition.y > boundHeight)
		      newPosition.y = 0 - position.height;
		}
		//�ϼu
		else if(boundsAction == Sprite.BA_BOUNCE){
			boolean bounce = false;
			Point newVelocity = new Point(velocity);         //�u�ʸI��
			//�I�������
			if(newPosition.x < 0){
				bounce = true;
				newPosition.x = 0;
				newVelocity.x = -newVelocity.x;
			}
			//�I���k���
			else if(newPosition.x + position.width > boundWidth){     
				bounce = true;
				newPosition.x = boundWidth - position.width;
				newVelocity.x = -newVelocity.x;
			}
			//�I���W���
			if(newPosition.y < 0){
				bounce = true;
				newPosition.y = 0;
				newVelocity.y = -newVelocity.y;
			}
			//�I���U���
			else if(newPosition.y + position.height > boundHeight){
				bounce = true;
				newPosition.y = boundHeight - position.height;
				newVelocity.y = -newVelocity.y;
			}
			if(bounce)
				setVelocity(newVelocity);
			
		}
		//����.
		else if (boundsAction == BA_DIE)
		{
		    if ((newPosition.x + position.width) < 0 ||newPosition.x > boundWidth ||
		        (newPosition.y + position.height) < 0 || newPosition.y > boundHeight){
		    	     action.set(Sprite.SA_KILL);
		    	     return action;					//�i��Engine:UpdataSprites()��sprite�w��.
		    }				
		  }
		//����(default)
		else
		{
		    //�b �� or �k��ɰ���. 
		    if (newPosition.x  < 0 || newPosition.x > (boundWidth - position.width))
		    {  
		      newPosition.x = Math.max(0, Math.min(newPosition.x, boundWidth - position.width));
		      setVelocity(new Point(0,0));
		    }
		    //�b �W or �U��ɰ���.  
		    if (newPosition.y  < 0 || newPosition.y > (boundHeight - position.height))
		    {
		      newPosition.y = Math.max(0, Math.min(newPosition.y, boundHeight - position.height));
		      setVelocity(new Point(0,0));
		    }
		 }
		 setPosition(newPosition);                //�M�wSprite�s��m.
		 return action;                           //��(��ɤ���==����), ��l���i��GameEngine��Sprite�޲z�{��, ��Srite���s��.
	}
	
	//�ˬd�~��sprite�O�_�P��sprite�I��.
	public boolean testCollision(Sprite testSprite){
		return collision.intersects(testSprite.getCollision());	
	}
	
	//�ˬd�I�O�_�bSprite����. 
	public boolean isPointInside(Point p)
	{
	  return position.contains(p); 
	}
	
	public void paintSprite(Graphics2D g){
		if(visible){
			//g.fillOval(position.x,position.y,width,height);
			g.drawImage(image, position.x, position.y, null);
		}
	}

/*
	//�W�[�e��
	protected void incFrame(){
		if((frameDelay > 0 )&&(--frameTrigger <= 0)){
			frameTrigger = frameDelay;   //reset the frame trigger.
			frame += frameInc;
			if(frame >= image.length)         //�ѫe���Ἵ,�����q�Y�}�l(frameInc > 0)
				frame = 0;
			else if(frame < 0)				  //�ѫ᩹�e��,�����q��}�l(frameInc < 0)
				frame = image.length-1;
		}
	}
*/
	
	protected Sprite addSprite(BitSet action){
		return null;
	}
}

	