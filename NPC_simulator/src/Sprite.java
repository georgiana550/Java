import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.BitSet;
import javax.swing.ImageIcon;

public class Sprite{
	//sprite是否消滅的模式
	public static final int SA_KILL   = 0,         //表示sprite須消滅 .
							SA_RESTOREPOS = 1,     //表示sprite須回到上個位置.
    						SA_ADDSPRITE = 2;      //表示sprite生sprite. 
	
	//sprite觸碰到視窗邊界時的反應模式
	public static final int BA_STOP = 0,					//停止
							BA_WRAP = 1,					//穿越
							BA_BOUNCE = 2,					//反彈
							BA_DIE =3;						//死亡
	
	//Point position,velocity;
Point velocity;
	//int width,height;						   //sprite的大小
	int boundWidth,boundHeight;				   //sprite活動的限制區
	boolean visible = true;						   //Sprite是隱藏亦或可見.	    	                    
	//boolean dying; 		                       //判斷Sprite是否消失.
	boolean bOneCycle;						   //是否自身在播完一次Sprite動畫後,自我毀滅(爆炸動畫).	      
	int boundsAction;						   //用來記錄該sprite觸碰邊界時的反應.
    Image image;	
int frame,
	frameInc,
	frameDelay,
	frameTrigger;	
Rectangle position,						   //Sprite的碰撞方框
		  collision,
		  bounds;
//int zOrder;
	//只有一個圖的Sprite(靜態)
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
		this.boundWidth = b_Width-2;			//frame的外框也有距離
		this.boundHeight = b_Height-2; 
	}
/*	
	//有多個圖的Sprite(動態)
	public Sprite(ImageIcon[] img, int f, int fi, int fd, Point pos, Point vel, int ba, int b_Width, int b_Height){
		this.image = img;
		setPosition(new Rectangle(pos.x, pos.y, img[f].getIconWidth(),img[f].getIconHeight()));
		setVelocity(vel);
		boundsAction = ba;
		frame = f;
		frameInc = fi;
		frameDelay = frameTrigger = fd;
		//zOrder = z;
		this.boundWidth = b_Width-2;			//frame的外框也有距離
		this.boundHeight = b_Height-2; 
	}
*/
	
	/*
	public Sprite(int b_Width, int b_Height){
		this.boundWidth = b_Width-2;
		this.boundHeight = b_Height-2;
		position = new Point(0,0);			   //預設sprite初始位置.
		velocity = new Point(1,1);			   //預設sprite初始速度.

		//從Jar讀取圖檔
		ImageIcon icon = new ImageIcon(this.getClass().getClassLoader().getResource("res/ANGEL_0021.GIF"));

		img = icon.getImage();
		//預設sprite初始大小
		width = icon.getIconWidth();
		height = icon.getIconHeight();
		visible = true;						   //預設sprite為可見.
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
	
	//設定sprite的碰撞方框
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
	
	//等速反彈
	public void setBounce() {
		velocity.x = -velocity.x;
		velocity.y = -velocity.y;
	}
	
	public void setBoundsAction(int ba)
	{ boundsAction = ba; };   						   //設定邊界觸碰的反應模式. 
	
	public BitSet updateState(){
		BitSet action = new BitSet();
		
		//增加畫框
		//incFrame();
		
		
		/*//如果Sprite被刪除,則回傳SA_KILL訊息,通知GameEngine. 
		 if (dying)
		    return SA_KILL;    */
        
		//Update the position
		Point newPosition = new Point(position.x, position.y); 
		newPosition.translate(velocity.x, velocity.y);            //translate(dx,dy)
		
		/* 判斷sprite撞到邊界時, 是該穿越, 反彈, 消滅 或停止. */ 
		//穿越. (原理:當Sprite完全消失在視窗後, 做座標平行或垂直跳躍) 
		if(boundsAction == Sprite.BA_WRAP){
			//Sprite在左邊界完全消失後, 從右邊界開始出現.
			if(newPosition.x + position.width < 0)
				newPosition.x = boundWidth;
			//Sprite在右邊界完全消失後, 從左邊界開始出現. 
		    else if (newPosition.x > boundWidth)
		      newPosition.x = 0 - position.width;
		    //Sprite在上邊界完全消失後, 從下邊界開始出現. 
		    if ((newPosition.y + position.height) < 0)
		      newPosition.y = boundHeight;
		    //Sprite在下邊界完全消失後, 從上邊界開始出現. 
		    else if (newPosition.y > boundHeight)
		      newPosition.y = 0 - position.height;
		}
		//反彈
		else if(boundsAction == Sprite.BA_BOUNCE){
			boolean bounce = false;
			Point newVelocity = new Point(velocity);         //彈性碰撞
			//碰撞左邊界
			if(newPosition.x < 0){
				bounce = true;
				newPosition.x = 0;
				newVelocity.x = -newVelocity.x;
			}
			//碰撞右邊界
			else if(newPosition.x + position.width > boundWidth){     
				bounce = true;
				newPosition.x = boundWidth - position.width;
				newVelocity.x = -newVelocity.x;
			}
			//碰撞上邊界
			if(newPosition.y < 0){
				bounce = true;
				newPosition.y = 0;
				newVelocity.y = -newVelocity.y;
			}
			//碰撞下邊界
			else if(newPosition.y + position.height > boundHeight){
				bounce = true;
				newPosition.y = boundHeight - position.height;
				newVelocity.y = -newVelocity.y;
			}
			if(bounce)
				setVelocity(newVelocity);
			
		}
		//消滅.
		else if (boundsAction == BA_DIE)
		{
		    if ((newPosition.x + position.width) < 0 ||newPosition.x > boundWidth ||
		        (newPosition.y + position.height) < 0 || newPosition.y > boundHeight){
		    	     action.set(Sprite.SA_KILL);
		    	     return action;					//告知Engine:UpdataSprites()該sprite已死.
		    }				
		  }
		//停止(default)
		else
		{
		    //在 左 or 右邊界停止. 
		    if (newPosition.x  < 0 || newPosition.x > (boundWidth - position.width))
		    {  
		      newPosition.x = Math.max(0, Math.min(newPosition.x, boundWidth - position.width));
		      setVelocity(new Point(0,0));
		    }
		    //在 上 or 下邊界停止.  
		    if (newPosition.y  < 0 || newPosition.y > (boundHeight - position.height))
		    {
		      newPosition.y = Math.max(0, Math.min(newPosition.y, boundHeight - position.height));
		      setVelocity(new Point(0,0));
		    }
		 }
		 setPosition(newPosition);                //決定Sprite新位置.
		 return action;                           //除(邊界反應==消滅), 其餘都告知GameEngine的Sprite管理程式, 該Srite仍存活.
	}
	
	//檢查外部sprite是否與本sprite碰撞.
	public boolean testCollision(Sprite testSprite){
		return collision.intersects(testSprite.getCollision());	
	}
	
	//檢查點是否在Sprite之中. 
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
	//增加畫框
	protected void incFrame(){
		if((frameDelay > 0 )&&(--frameTrigger <= 0)){
			frameTrigger = frameDelay;   //reset the frame trigger.
			frame += frameInc;
			if(frame >= image.length)         //由前往後播,播完從頭開始(frameInc > 0)
				frame = 0;
			else if(frame < 0)				  //由後往前播,播完從後開始(frameInc < 0)
				frame = image.length-1;
		}
	}
*/
	
	protected Sprite addSprite(BitSet action){
		return null;
	}
}

	