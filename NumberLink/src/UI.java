import java.awt.*;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.*;

public class UI implements ActionListener{
	final int N =25;						   //�D�w�����D��.
	
	JFrame f = null;                  	   //�D����
	JPanel p = null;				   	   //����C
	JTable table = null;				   //���
	
	JButton b1,b2;
	JLabel l1;
	
	Container contentPane;
	
	ParseFile pFile;
	
	Search search;	
	
	int width;
	int height;
	int[][] board;								//�x�s�ѽL.
	
	public UI(ParseFile pf){				//�����g�Lparse���ɮ�
		pFile = pf;
		
		width      = pFile.width;
		height 	   = pFile.height;
		board      = pFile.board;
		
		
		
		/* ����������l�� */
		f = new JFrame("Number Link");

		/*ø�s����C*/
		p = new JPanel();
		p.setLayout(new GridLayout(3,1));		
		
		/*ø�s���*/
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
		
		/* ���ܩҦ���e */
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
		
		/*ø�s�T����*/
		l1 = new JLabel();
		
		b1.addActionListener(this);
		b2.addActionListener(this);
	
		p.add(b1);
		p.add(b2);
		
		p.add(l1);
		
		contentPane.add(p, BorderLayout.EAST);					//����C�b�F
		contentPane.add(table, BorderLayout.CENTER);			//�e���b��
		
		
		f.pack();				//�����۰ʽվ�j�p
		f.setVisible(true);		//��ܵ���
		f.setResizable( false ); //�]�w�������i���ܤj�p
		
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
			Search s = new Search(pFile); 								//�j�M.
			long endTime = new GregorianCalendar().getTimeInMillis();
			
			/* ���etable */
			repaintTable();
			
			/* �p��Search���ɶ� */
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
			
		//�X�D
		else if(e.getSource()== b2){
			int num = 0;
			/* �H���q�D�w��� */
			Random rand = new Random(System.currentTimeMillis());	
			num = Math.abs(rand.nextInt()%N)+1;
			pFile = new ParseFile("data/test"+num+".txt");
			System.out.println("��"+num+"�D");
			/*���etable*/	
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
