import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.*;

public class UI implements ActionListener{
	final int N =25;						   //題庫中的題數.
	
	JFrame f = null;                  	   //主視窗
	JPanel p = null;				   	   //按鍵列
	JTable table = null;				   //表格
	
	JButton b1,b2;
	JLabel l1;
	
	Container contentPane;
	
	ParseFile pFile;
	
	Search search;	
	
	int width;
	int height;
	int[][] board;								//儲存棋盤.
	
	public UI(ParseFile pf){				//接收經過parse的檔案
		pFile = pf;
		
		width      = pFile.width;
		height 	   = pFile.height;
		board      = pFile.board;
		
		
		
		/* 視窗介面初始化 */
		f = new JFrame("Number Link");

		/*繪製按鍵列*/
		p = new JPanel();
		p.setLayout(new GridLayout(3,1));		
		
		/*繪製表格*/
		Object [][] info = new Object[height][width];
		for(int j=0;j<width;j++){
			for(int i=0;i<height;i++){
				
				if(pFile.board[j][height-i-1]== 0){
					info[i][j]= new String("");
				}else{
					info[i][j]= new Integer(board[j][height-i-1]);
				}	
			}
		}
		
		String[] Names = new String[width];
		for(int i=0;i<Names.length;i++)
			Names[i]="";

		table = new JTable(info,Names);
		
		/* 改變所有欄寬 */
		for(int i=0;i<width;i++){
			TableColumn column = table.getColumnModel().getColumn(i);
			column.setPreferredWidth(23);
		}	
		//table.setPreferredScrollableViewportSize(new Dimension(580,550));
		table.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		table.setGridColor(Color.ORANGE);
		
		contentPane = f.getContentPane();
		b1 = new JButton("Search");
		b2 = new JButton("Questions");
		
		/*繪製訊息欄*/
		l1 = new JLabel();
		
		b1.addActionListener(this);
		b2.addActionListener(this);
	
		p.add(b1);
		p.add(b2);
		
		p.add(l1);
		
		contentPane.add(p, BorderLayout.EAST);					//按鍵列在東
		contentPane.add(table, BorderLayout.CENTER);			//畫布在中
		
		
		f.pack();				//視窗自動調整大小
		f.setVisible(true);		//顯示視窗
		f.setResizable( false ); //設定視窗不可改變大小
		
		f.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
	}
		
	public void actionPerformed(ActionEvent e){
		//Search
		if(e.getSource()== b1){
			long startTime = new GregorianCalendar().getTimeInMillis();  
			Search s = new Search(pFile); 								//搜尋.
			long endTime = new GregorianCalendar().getTimeInMillis();
			
			/* 重畫table */
			repaintTable();
			
			/* 計算Search的時間 */
			long elapseTime = endTime - startTime;
			int elapseMin = (int)(elapseTime / 60000);
			int elapseSec = (int)(elapseTime %60000)/1000;
							
			
			if(s.echoAnswer == true){
				l1.setText(" Search Success!! "+elapseMin+":"+elapseSec+" ");
			}	
			else{
				l1.setText(" Search Failed!! "+elapseMin+":"+elapseSec+" ");
			}	
		}	
			
		//出題
		else if(e.getSource()== b2){
			int num = 0;
			/* 隨機從題庫抓圖 */
			Random rand = new Random(System.currentTimeMillis());	
			num = Math.abs(rand.nextInt()%N)+1;
			pFile = new ParseFile("data/test"+num+".txt");
			System.out.println("第"+num+"題");
			/*重畫table*/	
			repaintTable();	
		}
		
	
	}
	
	
	public void repaintTable(){
		int w = pFile.width;
		int h = pFile.height;
		
		for(int j=0;j<w;j++){
			for(int i=0;i<h;i++){
				if(pFile.board[j][h-i-1]== 0){
					table.setValueAt(" ",i,j);
				}else{
					table.setValueAt(new Integer(pFile.board[j][h-i-1]),i,j);
				}
				
			}
		}
	}
	
	
	public static void main(String[] args) {	
		
		//ParseFile pf = new ParseFile(args[0]);
		ParseFile pf = new ParseFile("data/test.txt");
		new UI(pf);

	}

}
