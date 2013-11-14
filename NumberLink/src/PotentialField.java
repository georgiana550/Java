import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


public class PotentialField extends MyMap{
	final int UNVISTED = 65;
	//final int OBSTACLE = 70;
	
	Map<Integer,int[][]> pFieldBox;					//�x�s�U�Ʀr������potentialField.
	int tempMap[][];				
	LinkedList La,Lb,Lt;							//��NF1�t��k�x�s�i����list.

	
	public PotentialField(ParseFile pf){
		super(pf);
		max = width+height-1;				//potentialField���ܰ��I.
		pFieldBox = new HashMap<Integer, int[][]>();
		La = new LinkedList();
		Lb = new LinkedList();
		
		initBitMap();
		
			
		//���C�@�ռƦr�إ�Potential Field.
		for(int i=0;i<initList.size();i++){
			initNode = initList.get(i);	
			goalNode = goalList.get(i);
			
			//�ثe��goalNode �]�������X�L,�_�h�|�䤣��.
			map[goalNode.getPosition().x][goalNode.getPosition().y] = UNVISTED;
			NF1(initNode.getPosition().x, initNode.getPosition().y);
			
			//�ƻs�@�����pFieldBox��.
			tempMap = new int[width][height];
			for(int p=0;p<width;p++){
				for(int q=0;q<height;q++){
					tempMap[p][q] = map[p][q];
				}
			}
			
			pFieldBox.put(initNode.getValue(),tempMap);
			initBitMap();
		}
		/*
		for(int i=0;i<initList.size();i++){
			System.out.println("value:"+initList.get(i).getValue());
			map = pFieldBox.get(initList.get(i).getValue());
			printPF();
		}
		*/
	}	 	
	
	
	
	void testNeighbor(int x, int y, int potential){
		if(map[x][y] == UNVISTED){					//���լO�_�����X
			map[x][y] = potential;			
			Lb.add(new Point(x,y));     			//�[�J��i+1�Ӫi
		}
	}
		
	public void NF1(int initX, int initY){
		int potential = 0;		//��l�_�l����, �Y0
		Point q;
		q = new Point(initX,initY);		
		La.add(q); 							//����goal��J��0�i
		int x,y;
			
		while(!La.isEmpty()){        		//����i�e����
			Iterator it = La.iterator();
			while(it.hasNext()){			//���ժi�e�Ҧ����I
				q = (Point)it.next();
				x = q.x;
				y = q.y;
					
				if(x > 0){           					//���լO�_�W�L�����
					testNeighbor(x-1,y,potential+1);	//���ե��F�~�O�_���X
				}
				if(x < width-1){							//���լO�_�W�L�k���
					testNeighbor(x+1,y,potential+1);	//���եk�F�~�O�_���X
				}
				if(y > 0){								//���լO�_�W�L�U���
					testNeighbor(x,y-1,potential+1);	//���դU�F�~�O�_���X
				}
				if(y < height-1){							//���լO�_�W�L�k���
					testNeighbor(x,y+1,potential+1);	//���եk�F�~�O�_���X
				}
				
			}
			potential++;
			Lt = La;
			La = Lb;
			Lt.clear();
			Lb = Lt; 
		}
		//printPF();
	}	
	
		
	/* �L�XPotential Field */
	public void printPF(){
		System.out.println("===========================");
		for(int j=height-1;j>=0;j--){
			for(int i=0;i<width;i++){
				if(map[i][j] == max){
					System.out.print(" * ");
				}
				else if (initNode.getPosition().x == i && initNode.getPosition().y == j)
				    System.out.print(" I ");
				else if (map[i][j] == UNVISTED){ 
					System.out.print(" R ");
				}
				else{
					if(map[i][j] < 10)
						System.out.print(" "+map[i][j]+" ");
					
					else 
						System.out.print(map[i][j]+" ");
				}	
			}
			System.out.println();
		}
	}
		
	
	/* �NBitMap���]�������X�λ�ê�� */
	public void initBitMap(){
		for(int k=0;k<width;k++){
			for(int l=0;l<height;l++){
			//	if(map[k][l] == 0)
					map[k][l] = UNVISTED ;  //�w�]�������X
			}
		}
		
		for(int i=0;i<initList.size();i++){
			initNode = initList.get(i);	
			goalNode = goalList.get(i);

			//initialNode��goalNode�����w�]����ê��.
			map[initNode.getPosition().x][initNode.getPosition().y] = max;
			map[goalNode.getPosition().x][goalNode.getPosition().y] = max;
		}
		
	}
	
	public int[][] getMap(int value){
		return pFieldBox.get(value);
	}
	
/*
	public static void main(String[] args) {	
		new PotentialField(new ParseFile(args[0]));
	}
*/	
		
}


