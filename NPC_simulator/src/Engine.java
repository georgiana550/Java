/* 初始視窗介面 */

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
//import java.awt.Color;
import java.awt.Container;
//import java.awt.Dimension;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
import java.awt.GridLayout;
//import java.awt.Point;
//import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.event.MouseAdapter;
//import java.awt.event.MouseEvent;
//import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
//import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;

public class Engine implements ActionListener{
	final int canvasWidth = 512,
	  		  canvasHeight = 512;
	
	JFrame f = null;                   //主視窗
	JPanel controlBar,canvas;
	JRadioButton[] BA_radio, V_radio;
	ButtonGroup BA_group,V_group;
	
	Image[] sprite = new Image[8];
	Image bg;
	Background background;
	
	Thread newThread;
	SpriteADM spriteADM;
	
	static int customize_BA;				//暫存使用者BA選擇.							
	static int customize_V;					//暫存使用者Velocity選擇.
	
	public Engine(){
		
		initResources();
		
		//field
		spriteADM = new SpriteADM();
		
		//視窗初始化
		f = new JFrame("Simulator");
		canvas = new SIMCanvas(this);
		controlBar = new JPanel();
		
		controlBar.setLayout(new GridLayout(6,1));
		canvas.setBorder(BorderFactory.createEtchedBorder(SoftBevelBorder.RAISED));
		BA_radio = new JRadioButton[4];
		V_radio = new JRadioButton[5];
		
		BA_radio[0] = new JRadioButton("STOP",true);
		BA_radio[1] = new JRadioButton("WRAP");
		BA_radio[2] = new JRadioButton("BOUNCE");
		BA_radio[3] = new JRadioButton("DIE");
		
		V_radio[0] = new JRadioButton("0",true);
		V_radio[1] = new JRadioButton("1");
		V_radio[2] = new JRadioButton("2");
		V_radio[3] = new JRadioButton("3");
		V_radio[4] = new JRadioButton("4");
		
		BA_group = new ButtonGroup();
		V_group = new ButtonGroup();
		for(int i=0;i<BA_radio.length;i++){
			BA_group.add(BA_radio[i]);
			BA_radio[i].addActionListener(this);
			controlBar.add(BA_radio[i]);
		}
		
		for(int i=0;i<V_radio.length;i++){
			V_group.add(V_radio[i]);
			V_radio[i].addActionListener(this);
			controlBar.add(V_radio[i]);
		}
		
		Container contentPane = f.getContentPane();
		
		contentPane.add(controlBar, BorderLayout.EAST);			//按鍵列在東
		contentPane.add(canvas, BorderLayout.CENTER);			//畫布在中
			
		f.pack();							//視窗自動調整大小
		f.setVisible(true);					//顯示視窗
		f.setResizable( false ); 			//設定視窗不可改變大小
		
		f.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
		
	}	
	
	
	public void drawBackround(Graphics2D g){
		background.draw(g);
	} 
	
	public Image[] getSprite(){
		return sprite;
	}
	
	//從Jar讀取圖檔
	public void initResources(){
		//bg = new ImageIcon(ClassLoader.getSystemResource("res/Map004.png"));
		bg = Toolkit.getDefaultToolkit().getImage(
				this.getClass().getClassLoader().getResource("res/Map004.png"));
		background = new ImageBackground(canvasWidth,canvasHeight,bg);
		for(int i = 0; i < 8; i++)
			sprite[i] = Toolkit.getDefaultToolkit().getImage(
					this.getClass().getClassLoader().getResource("res/"+i+".gif"));
	}
	
	
	//處理使用者預設的Sprite狀態
	public void actionPerformed(ActionEvent e){
		for(int i=0; i<BA_radio.length; i++){
			if(e.getSource() == BA_radio[i]){
				customize_BA = i;
			}
		}
		
		for(int i=0; i<V_radio.length; i++){
			if(e.getSource() == V_radio[i]){
				customize_V = i+1;
			}
		}
	}
		
	public SpriteADM getSpriteADM(){
		return spriteADM;
	}
	
	public void addSprite(Sprite s){
		spriteADM.addSprite(s);
	}
	
	public void update(){
		canvas.repaint();
		try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		spriteADM.updateSprites();
	}
}

