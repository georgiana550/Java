import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

public class SpriteADM {
	ArrayList<Sprite> spriteList;
	//protected Background background;
	
	public SpriteADM(){
		spriteList = new ArrayList();
		//this.background = back;
	}
/*	也許將搬至SIM
	public Background  getBackground(){
		return background;
	}
	
	public void setBackground(Background back){
		this.background = back;
	}
*/	
	public ArrayList<Sprite> getSpriteList(){
		return spriteList;
	}
	
	public void addSprite(Sprite s){
		if(s != null)
			spriteList.add(s);
		else
			System.out.println("sprite是空的!");
	}
	
	public void changeAllSpriteVel(Point p){
		Sprite s;
		Iterator it = spriteList.iterator();
		while(it.hasNext()){
			s = (Sprite)it.next();
			if(p.x > s.position.x)
				s.changeVelocity(1, 0);
			else if(p.x < s.position.x)
				s.changeVelocity(-1, 0);
			if(p.y > s.position.y)
				s.changeVelocity(0,1);
			else if (p.y < s.position.y)
				s.changeVelocity(0,-1);	
		}
	}
	
	//檢查點是否在任一sprite中.
	public Sprite isPointInSprite(Point p)
	{
		Sprite s;
		Iterator it = spriteList.iterator();
		while(it.hasNext()){
			s = (Sprite)it.next();
			if(s.isVisible() && s.isPointInside(p)){
				return s; 
			}	
		}
	    return null;
	}    	
	
	
	//清除所有sprites.
	public void cleanupSprites()
	{
	  spriteList.clear();
	}
	
	
	//逐一檢查Sprite清單, 檢視Sprite之間是否發生碰撞. 
	public boolean checkSpriteCollision(Sprite testSprite)
	{
	  for(int i=0;i<spriteList.size();i++){
		if(spriteList.get(i) == testSprite)				//這裡比的是reference,可能有誤.
			continue;
		if(testSprite.testCollision(spriteList.get(i)))
			return true;                                //這裡可以安插client端的碰撞處理function.
	  }
	  // 沒有碰撞發生. 
	  return false;
	}
	
	
	//此處暫不處理Sprite死亡 及 新生Sprite
    public void updateSprites()
	{
	  Rectangle oldPosition;						//  記錄更新前Sprite的位置. 
	  Sprite s;
	  //int newSpriteAction;
	  
	  Iterator it = spriteList.iterator();
	  while(it.hasNext()){
		  s = (Sprite)it.next();
		  oldPosition = s.getPosition();			//暫存更新前Sprite的位置.
		  BitSet action = s.updateState();			//儲存Sprite更新後的新狀態.
		
		  //若Sprite的新狀態是"新生"
		  if(action.get(Sprite.SA_ADDSPRITE)){
			  Sprite newSprite = s.addSprite(action);
			  if(newSprite != null){
				  spriteList.add(newSprite);
			  }
		  }
		  //若Sprite的新狀態是"返回原位"
		  else if(action.get(Sprite.SA_RESTOREPOS)){
			  s.setPosition(oldPosition);
		  }
		  
		  //若Sprite的新狀態是"消滅". 
		  else if (action.get(Sprite.SA_KILL))
		  {
			  spriteList.remove(s);            //critical section
		      break;
		   }  
		  
		  //檢查Sprite本身是否與其它Sprite碰撞. 
	      if (checkSpriteCollision(s)){
	    	  s.setPosition(oldPosition);			//發生碰撞則將Sprite本身退回原位置.
	    	  s.setBounce();						//反彈.
	      }  
	  }
	}
	
	public void drawSprites(Graphics2D g)
	{
	  Iterator it = spriteList.iterator();
	  while(it.hasNext()){
		  ((Sprite)it.next()).paintSprite(g);
	  }
	}
	
	/*
	public void draw(Graphics2D g){
		background.draw(g);
		drawSprites(g);
	}
	*/
}
