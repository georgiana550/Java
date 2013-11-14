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
/*	�]�\�N�h��SIM
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
			System.out.println("sprite�O�Ū�!");
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
	
	//�ˬd�I�O�_�b���@sprite��.
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
	
	
	//�M���Ҧ�sprites.
	public void cleanupSprites()
	{
	  spriteList.clear();
	}
	
	
	//�v�@�ˬdSprite�M��, �˵�Sprite�����O�_�o�͸I��. 
	public boolean checkSpriteCollision(Sprite testSprite)
	{
	  for(int i=0;i<spriteList.size();i++){
		if(spriteList.get(i) == testSprite)				//�o�̤񪺬Oreference,�i�঳�~.
			continue;
		if(testSprite.testCollision(spriteList.get(i)))
			return true;                                //�o�̥i�H�w��client�ݪ��I���B�zfunction.
	  }
	  // �S���I���o��. 
	  return false;
	}
	
	
	//���B�Ȥ��B�zSprite���` �� �s��Sprite
    public void updateSprites()
	{
	  Rectangle oldPosition;						//  �O����s�eSprite����m. 
	  Sprite s;
	  //int newSpriteAction;
	  
	  Iterator it = spriteList.iterator();
	  while(it.hasNext()){
		  s = (Sprite)it.next();
		  oldPosition = s.getPosition();			//�Ȧs��s�eSprite����m.
		  BitSet action = s.updateState();			//�x�sSprite��s�᪺�s���A.
		
		  //�YSprite���s���A�O"�s��"
		  if(action.get(Sprite.SA_ADDSPRITE)){
			  Sprite newSprite = s.addSprite(action);
			  if(newSprite != null){
				  spriteList.add(newSprite);
			  }
		  }
		  //�YSprite���s���A�O"��^���"
		  else if(action.get(Sprite.SA_RESTOREPOS)){
			  s.setPosition(oldPosition);
		  }
		  
		  //�YSprite���s���A�O"����". 
		  else if (action.get(Sprite.SA_KILL))
		  {
			  spriteList.remove(s);            //critical section
		      break;
		   }  
		  
		  //�ˬdSprite�����O�_�P�䥦Sprite�I��. 
	      if (checkSpriteCollision(s)){
	    	  s.setPosition(oldPosition);			//�o�͸I���h�NSprite�����h�^���m.
	    	  s.setBounce();						//�ϼu.
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
