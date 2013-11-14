import java.awt.Image;
import java.awt.Point;
import java.util.BitSet;
import java.util.Random;

import javax.swing.ImageIcon;

public class DirectionalSprite extends Sprite {
	//八個方位的速度分量
	protected static final int[][] velDirs = {
		{0,-1},       //北    velDirs[0]
		{1,-1},		  //東北velDirs[1]
		{1,0},		  //東    velDirs[2]	
		{1,1},		  //東南velDirs[3]
		{0,1},		  //南    velDirs[4]
		{-1,1},		  //西南velDirs[5]
		{-1,0},		  //西    velDirs[6]
		{-1,-1}		  //西北velDirs[7]
	};
	//ImageIcon image[][];     //儲存八個方位的圖
	Image directionalImg[];		//儲存八個方位的圖
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
		
		//根據方向變換速度
		velocity.x *= velDirs[dir][0]; //方向陣列的x分量
		velocity.y *= velDirs[dir][1]; //方向陣列的y分量
		
		setImage(directionalImg[dir]);     //更換sprite的圖
	}
	
	public int getDirection(){
		return direction;
	}
	
	public void setVelocity(Point vel){
		velocity = vel;
		//將二維速度分量,轉換成一維方向(0-7)
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
		//隨機改變方向
		if((rand.nextInt()%20 == 0)){
			velocity.x = velocity.y = 1;
			setDirection(direction+rand.nextInt()%2);   //只會向右或向左轉一單位
		}
		
		BitSet action = super.updateState();
		
		return action;
		
	}
	
}
