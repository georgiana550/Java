import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;


public class PotentialField extends MyMap{
	final int UNVISTED = 65;
	//final int OBSTACLE = 70;
	
	Map<Integer,int[][]> pFieldBox;					//儲存各數字對應的potentialField.
	int tempMap[][];				
	LinkedList La,Lb,Lt;							//供NF1演算法儲存波長的list.

	
	public PotentialField(ParseFile pf){
		super(pf);
		max = width+height-1;				//potentialField的至高點.
		pFieldBox = new HashMap<Integer, int[][]>();
		La = new LinkedList();
		Lb = new LinkedList();
		
		initBitMap();
		
			
		//為每一組數字建立Potential Field.
		for(int i=0;i<initList.size();i++){
			initNode = initList.get(i);	
			goalNode = goalList.get(i);
			
			//目前的goalNode 設為未拜訪過,否則會找不到.
			map[goalNode.getPosition().x][goalNode.getPosition().y] = UNVISTED;
			NF1(initNode.getPosition().x, initNode.getPosition().y);
			
			//複製一份丟到pFieldBox裡.
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
		if(map[x][y] == UNVISTED){					//測試是否未拜訪
			map[x][y] = potential;			
			Lb.add(new Point(x,y));     			//加入第i+1個波
		}
	}
		
	public void NF1(int initX, int initY){
		int potential = 0;		//初始起始高度, 即0
		Point q;
		q = new Point(initX,initY);		
		La.add(q); 							//先把goal放入第0波
		int x,y;
			
		while(!La.isEmpty()){        		//直到波前死光
			Iterator it = La.iterator();
			while(it.hasNext()){			//測試波前所有的點
				q = (Point)it.next();
				x = q.x;
				y = q.y;
					
				if(x > 0){           					//測試是否超過左邊界
					testNeighbor(x-1,y,potential+1);	//測試左鄰居是否拜訪
				}
				if(x < width-1){							//測試是否超過右邊界
					testNeighbor(x+1,y,potential+1);	//測試右鄰居是否拜訪
				}
				if(y > 0){								//測試是否超過下邊界
					testNeighbor(x,y-1,potential+1);	//測試下鄰居是否拜訪
				}
				if(y < height-1){							//測試是否超過右邊界
					testNeighbor(x,y+1,potential+1);	//測試右鄰居是否拜訪
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
	
		
	/* 印出Potential Field */
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
		
	
	/* 將BitMap都設為未拜訪及障礙物 */
	public void initBitMap(){
		for(int k=0;k<width;k++){
			for(int l=0;l<height;l++){
			//	if(map[k][l] == 0)
					map[k][l] = UNVISTED ;  //預設為未拜訪
			}
		}
		
		for(int i=0;i<initList.size();i++){
			initNode = initList.get(i);	
			goalNode = goalList.get(i);

			//initialNode及goalNode都先預設為障礙物.
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


