import java.awt.Point;
import java.util.ArrayList;


public class Search {
	int min;										//記錄OPEN裡的最小成本值.
	boolean success;								//記錄Search是否成功.
	boolean echoAnswer;								//回傳搜尋結果.	
	ArrayList<GridNode> path;						//儲存搜尋路徑.
	
	/*儲存來自ParseFile的資料*/
	int width;
	int height;
	int[][] board;									//儲存棋盤.
	int[][] contourMap;								//等高線圖,愈中心愈高.
	GridNode initNode;								//記錄initial.
	GridNode goalNode;								//記錄goal.
	ArrayList<GridNode> initList;					//儲存所有數字的initial
	ArrayList<GridNode> goalList;					//儲存所有數字的goal

	ParseFile pFile;
	PotentialField pField;							//暫存potentialField的資訊.
	ContourMap cMap;								//暫存ContourMap的資訊.
	
	
	
public Search(ParseFile pf){
		pFile  = pf;
		pField = new PotentialField(pf);
		cMap   = new ContourMap(pf);
		
		width      = pf.width;
		height 	   = pf.height;
		board      = pf.board;
		contourMap = cMap.getMap();
		initList   = pf.initList;
		goalList   = pf.goalList;
		
			
		if(backTracking(0).x==1){
			echoAnswer = true;
			System.out.println("success!!");
		}
		else{
			echoAnswer = false;
			System.out.println("failed!!");
		}
		
		for(int i=0;i<initList.size();i++){
			System.out.print(initList.get(i).getValue()+" ");
		}
		System.out.println();	
	}


	/*backTracking將return最終的success*/
	public Point backTracking(int level){
		
		
		/*  result.x == 1 success     result.x == 0 false  *
		 *  result.y 儲存 failed node的value值.
		 *                                                 */
		Point result = new Point(1,0);      			//儲存子節點的search結果.   
		GridNode currInitNode;							//暫存本回合的initial node,以備search失敗時還原.
		GridNode currGoalNode;							//暫存本回合的goal node,以備search失敗時還原.
		
		int[] failedNodeList = new int[initList.size()];		//儲存這一層當中已試用過的node.
		int count = 0;											//計數這層已試用過幾個node.
		boolean alreadyTry = false;								//儲存這個子節點是否已在這層試用過.
		
		int x	   = 0,
			y	   = 0,
			value  = 0;
		
		int[][] oldCMap = new int[width][height];		//暫存本回合尚未變更的contourMap.
	    int[][] oldBoard = new int[width][height];		//暫存本回合尚未變更的board.
		
	    
		for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				oldCMap[i][j] = contourMap[i][j];
				oldBoard[i][j] = board[i][j];
			}
		}

		/* 記錄第一層已測試過第一個節點 */
		if(initList.size() !=0){
			if(level ==0)
				failedNodeList[count++] = initList.get(0).getValue();
		}
		
		
		for(int i=0;i<initList.size();i++){
	
			/*子節點search失敗時,restore原本走的path,改先走該子節點,測試是否search成功*/
			if(result.x == 0){
				
				/*result.y儲存失敗子節點的value值, 此處為直接找到該node,以進一步search*/		
				if(initList.get(i).getValue() != result.y && alreadyTry == false){
					//尚未嘗試過的node被移到List的最後.
					GridNode tempNode = initList.remove(i);
					initList.add(tempNode);
					tempNode = goalList.remove(i);
					goalList.add(tempNode);
			/*		
					System.out.println("result.y:"+result.y);
					System.out.println("被忽略掉的node:"+initList.get(i).getValue());
					continue;
			*/		
				}
				failedNodeList[count++]=result.y;
				alreadyTry = false;
			}
			
						
			initNode = initList.get(i);
			goalNode = goalList.get(i); 
			contourMap[initNode.getPosition().x][initNode.getPosition().y] = 0;		
			contourMap[goalNode.getPosition().x][goalNode.getPosition().y] = 0;		//該回合的node不設為障礙物,否則找不到.	
			success=false;

			 
			/* 先用contourMap搜尋 */
			if(aStar(cMap)){		//本回合Search成功.
			//if(aStar(pField)){
				for(int j=0;j<path.size();j++){
					x = path.get(j).getPosition().x;
					y = path.get(j).getPosition().y;
					value = path.get(j).getValue();

					board[x][y] = value;								//將棋盤更新,填上search成功的路徑.
					contourMap[x][y] = cMap.getMax();					//走過的路都成為下一次的障礙物.
				}
//System.out.println("b");
//pFile.printBoard();
				currInitNode = initList.remove(i);			//本回合的initial node彈出,剩下的node再繼續search.
				currGoalNode = goalList.remove(i);			//本回合的goal    node彈出,剩下的node再繼續search.

				result = backTracking(level+1);					//向下一層trace, 記錄子節點的search結果. <--backtracking或DFS的分支於此.
			}
			else{ 							//本回合Search失敗.
//System.out.println("第二次Search");			
				if(aStar(pField)){			//改試potential Field.
				//if(aStar(cMap)){
//System.out.println("b");
//pFile.printBoard();
					result = backTracking(level+1);
					return result;
				}else{ 	
					result.setLocation(0,initNode.getValue());	//告訴母節點我已失敗,並告訴她我是誰(value值).
//System.out.println("失敗的子節點:"+initNode.getValue());
					return result;
				}	
			}
			
			
			/*  如果子節點search失敗,首先回來到此,開始BackTracking*/
			if(result.x == 0){
				//search失敗,本回合被彈掉的都要加回來.
				initList.add(0,currInitNode);	
				goalList.add(0,currGoalNode);
				
				//本回合被改過的contourMap及board都要restore.
				for(int p=0;p<width;p++){
					for(int q=0;q<height;q++){
						contourMap[p][q] = oldCMap[p][q];
						board[p][q] = oldBoard[p][q];
					}
				}
				
				/*檢查該子節點是否已在此層被試用過,被試用過者,則不再試用*/
				for(int c=0;c<count;c++){
					if(result.y == failedNodeList[c]){
						alreadyTry = true;
					}
				}
//System.out.println("開始backtracking:回到上個contourMap");
//pFile.printBoard();
			}
		}
		return result;
	}
	
	
	
	/* A*演算法使用 */
	public boolean aStar(MyMap mp){
		ArrayList<GridNode>[] openList  = new ArrayList[min = mp.getMax()+1];	//OPEN List.
		boolean[][] visted 			    = new boolean[width][height];				//記錄拜訪過的格子.
		GridNode currentNode;	//暫存目前最佳的點.
		
		
		int x 	  = 0,
		    y 	  = 0,
		    value = 0;	
		path = new ArrayList();
		
		initNode.setParent(null);
		
		visted[initNode.getPosition().x][initNode.getPosition().y] = true;
		
		/* contourMap裡的障礙物視為visted,可以供potentialField參考 */
		for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				if(contourMap[i][j]==cMap.getMax()){
					visted[i][j] = true;						
				}
			}
		}
		
		
		insertOPEN(initNode,openList,visted,mp);		//將initial node 插入Open

		while(!success && !openIsEmpty(openList)){

			while(openList[min]== null || openList[min].isEmpty()){		
				min++;
			}
	
			currentNode = (GridNode)openList[min].remove(0);		//取目前最佳的點(地勢最低的點)
			x = currentNode.getPosition().x;
			y = currentNode.getPosition().y;
			value = currentNode.getValue();
			
			//測試鄰點是否已拜訪或碰撞
			if(y<height-1){
			   testNeighbor(x,y+1,value,openList,visted,currentNode,mp);
			}
			if(y>0){
			   testNeighbor(x,y-1,value,openList,visted,currentNode,mp);
			}
			if(x<width-1){
			   testNeighbor(x+1,y,value,openList,visted,currentNode,mp);
			}
			if(x>0){
			   testNeighbor(x-1,y,value,openList,visted,currentNode,mp);
			}			
		}

		if(success){
			//從goal開始向前找parentNode,藉此收集成一條path.
			while(goalNode != null){
				path.add(0,goalNode);
				goalNode = goalNode.getParent();
			}
//printVisted(visted);
			return true;
		}
		else{
		//	System.out.println("Search失敗!!");			
			return false;
		}
	}
	
	
	public boolean isSuccess(){
		return success;
	}
	
	public ArrayList<GridNode> getPath(){
		return path;
	}
	
	//測試是否未拜訪&&不碰撞,若是便插入OPEN(呼叫insertOpen())
	private void testNeighbor(int x, int y, int value, ArrayList<GridNode>[] open, boolean[][] visted, GridNode cNode, MyMap mp){
		GridNode tempNode;
		
		
		//測試是否拜訪過
		if (visted[x][y] == true){
			return;
		}	
		else{
			tempNode = new GridNode(value,x,y);
			tempNode.setParent(cNode);								//x' point toward to x
			insertOPEN(tempNode,open,visted, mp);						//未拜訪過的點就加入OPEN

			/* 鄰居就是node */
			if(x == goalNode.getPosition().x && y ==goalNode.getPosition().y){
				success = true;
				goalNode = tempNode;
			}
		}	
	}
	
	
	//判斷OPEN是否為空array
	private boolean openIsEmpty(ArrayList<GridNode>[] open){
		for(ArrayList a:open){
			if(a != null){
				if(!a.isEmpty()){
					return false;				//return Open is not Empty
				}
			}
		}
		return true;						   //return Open is Empty
	}
	
	
	//將node插入OPEN,設為已拜訪,並更新min的index
	private void insertOPEN(GridNode node,ArrayList<GridNode>[] open, boolean[][] visted, MyMap mp){
		int[][] map;
		int x = node.getPosition().x;
		int y = node.getPosition().y;
		int cost;
		if(mp instanceof PotentialField){
			map = ((PotentialField)mp).getMap(node.getValue()); 
		}
		else{
			map = mp.getMap();
		}
		cost = map[x][y];
		//cost = contourMap[x][y];
		//cost從contourMap取得.
		if (open[cost] == null){
			open[cost] = new ArrayList();
		}
		open[cost].add(0,node);		//展開的node插在List的第一個element
		
		if (cost <= this.min)
			this.min = cost;
		
		visted[x][y] = true;			//記錄為拜訪過
	
		
	}
	
	public void printVisted(boolean[][] visted){
		System.out.println("=======================");
		System.out.println("visted");
				
		for(int j=height-1;j>=0;j--){
			for(int i=0;i<width;i++){
				if(visted[i][j]==true)
					System.out.print("*");
				else
					System.out.print("0");
			}
			System.out.println();
		}	
	}
		
}

