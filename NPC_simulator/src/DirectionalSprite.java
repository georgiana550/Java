import java.awt.Image;
import java.awt.Point;
import java.util.BitSet;
import java.util.Random;

import javax.swing.ImageIcon;

public class DirectionalSprite extends Sprite {
	//�K�Ӥ�쪺�t�פ��q
	protected static final int[][] velDirs = {
		{0,-1},       //�_    velDirs[0]
		{1,-1},		  //�F�_velDirs[1]
		{1,0},		  //�F    velDirs[2]	
		{1,1},		  //�F�nvelDirs[3]
		{0,1},		  //�n    velDirs[4]
		{-1,1},		  //��nvelDirs[5]
		{-1,0},		  //��    velDirs[6]
		{-1,-1}		  //��_velDirs[7]
	};
	//ImageIcon image[][];     //�x�s�K�Ӥ�쪺��
	Image directionalImg[];		//�x�s�K�Ӥ�쪺��
	int direction;
	static Random rand = new Random(System.currentTimeMillis());
	
	
	
	public DirectionalSprite(Image[] img, Point pos, Point vel, int ba,
			int b_Width, int b_Height, int direct) 
	{
		super(img[direct], pos, vel, ba, b_Width, b_Height);
		directionalImg = img;
		this.direction = direct;
	}
/*
	public DirectionalSprite(ImageIcon[][] img, int f, int fi, int fd, Point pos,
			Point vel, int ba, int b_Width, int b_Height, int direct) 
	{
		super(img[direct], f, fi, fd, pos, vel, ba, b_Width, b_Height);
		image = img;
		setDirection(direct);
	}
*/
	public void setImage(Image img){
		super.image = img;
	}
	
	public void setDirection(int dir){
		if(dir < 0)
			dir = 7;
		else if(dir > 7)
			dir = 0;
		direction = dir;
		
		//�ھڤ�V�ܴ��t��
		velocity.x *= velDirs[dir][0]; //��V�}�C��x���q
		velocity.y *= velDirs[dir][1]; //��V�}�C��y���q
		
		setImage(directionalImg[dir]);     //��sprite����
	}
	
	public int getDirection(){
		return direction;
	}
	
	public void setVelocity(Point vel){
		velocity = vel;
		//�N�G���t�פ��q,�ഫ���@����V(0-7)
		if(vel.x == 0 && vel.y ==0)
			return;
		if(vel.x == 0)
			direction = (vel.y + 1) * 2;
		else if(vel.x ==1)
			direction = vel.y + 2;
		else if(vel.x == -1)
			direction = -vel.y + 6;
	}
	
	public BitSet updateState(){
		//�H�����ܤ�V
		if((rand.nextInt()%20 == 0)){
			velocity.x = velocity.y = 1;
			setDirection(direction+rand.nextInt()%2);   //�u�|�V�k�ΦV����@���
		}
		
		BitSet action = super.updateState();
		
		return action;
		
	}
	
}
