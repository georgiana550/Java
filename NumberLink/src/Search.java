import java.awt.Point;
import java.util.ArrayList;


public class Search {
	int min;										//�O��OPEN�̪��̤p������.
	boolean success;								//�O��Search�O�_���\.
	boolean echoAnswer;								//�^�Ƿj�M���G.	
	ArrayList<GridNode> path;						//�x�s�j�M���|.
	
	/*�x�s�Ӧ�ParseFile�����*/
	int width;
	int height;
	int[][] board;									//�x�s�ѽL.
	int[][] contourMap;								//�����u��,�U���߷U��.
	GridNode initNode;								//�O��initial.
	GridNode goalNode;								//�O��goal.
	ArrayList<GridNode> initList;					//�x�s�Ҧ��Ʀr��initial
	ArrayList<GridNode> goalList;					//�x�s�Ҧ��Ʀr��goal

	ParseFile pFile;
	PotentialField pField;							//�ȦspotentialField����T.
	ContourMap cMap;								//�ȦsContourMap����T.
	
	
	
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


	/*backTracking�Nreturn�̲ת�success*/
	public Point backTracking(int level){
		
		
		/*  result.x == 1 success     result.x == 0 false  *
		 *  result.y �x�s failed node��value��.
		 *                                                 */
		Point result = new Point(1,0);      			//�x�s�l�`�I��search���G.   
		GridNode currInitNode;							//�Ȧs���^�X��initial node,�H��search���Ѯ��٭�.
		GridNode currGoalNode;							//�Ȧs���^�X��goal node,�H��search���Ѯ��٭�.
		
		int[] failedNodeList = new int[initList.size()];		//�x�s�o�@�h���w�եιL��node.
		int count = 0;											//�p�Ƴo�h�w�եιL�X��node.
		boolean alreadyTry = false;								//�x�s�o�Ӥl�`�I�O�_�w�b�o�h�եιL.
		
		int x	   = 0,
			y	   = 0,
			value  = 0;
		
		int[][] oldCMap = new int[width][height];		//�Ȧs���^�X�|���ܧ�contourMap.
	    int[][] oldBoard = new int[width][height];		//�Ȧs���^�X�|���ܧ�board.
		
	    
		for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				oldCMap[i][j] = contourMap[i][j];
				oldBoard[i][j] = board[i][j];
			}
		}

		/* �O���Ĥ@�h�w���չL�Ĥ@�Ӹ`�I */
		if(initList.size() !=0){
			if(level ==0)
				failedNodeList[count++] = initList.get(0).getValue();
		}
		
		
		for(int i=0;i<initList.size();i++){
	
			/*�l�`�Isearch���Ѯ�,restore�쥻����path,������Ӥl�`�I,���լO�_search���\*/
			if(result.x == 0){
				
				/*result.y�x�s���Ѥl�`�I��value��, ���B����������node,�H�i�@�Bsearch*/		
				if(initList.get(i).getValue() != result.y && alreadyTry == false){
					//�|�����չL��node�Q����List���̫�.
					GridNode tempNode = initList.remove(i);
					initList.add(tempNode);
					tempNode = goalList.remove(i);
					goalList.add(tempNode);
			/*		
					System.out.println("result.y:"+result.y);
					System.out.println("�Q��������node:"+initList.get(i).getValue());
					continue;
			*/		
				}
				failedNodeList[count++]=result.y;
				alreadyTry = false;
			}
			
						
			initNode = initList.get(i);
			goalNode = goalList.get(i); 
			contourMap[initNode.getPosition().x][initNode.getPosition().y] = 0;		
			contourMap[goalNode.getPosition().x][goalNode.getPosition().y] = 0;		//�Ӧ^�X��node���]����ê��,�_�h�䤣��.	
			success=false;

			 
			/* ����contourMap�j�M */
			if(aStar(cMap)){		//���^�XSearch���\.
			//if(aStar(pField)){
				for(int j=0;j<path.size();j++){
					x = path.get(j).getPosition().x;
					y = path.get(j).getPosition().y;
					value = path.get(j).getValue();

					board[x][y] = value;								//�N�ѽL��s,��Wsearch���\�����|.
					contourMap[x][y] = cMap.getMax();					//���L�����������U�@������ê��.
				}
//System.out.println("b");
//pFile.printBoard();
				currInitNode = initList.remove(i);			//���^�X��initial node�u�X,�ѤU��node�A�~��search.
				currGoalNode = goalList.remove(i);			//���^�X��goal    node�u�X,�ѤU��node�A�~��search.

				result = backTracking(level+1);					//�V�U�@�htrace, �O���l�`�I��search���G. <--backtracking��DFS�������.
			}
			else{ 							//���^�XSearch����.
//System.out.println("�ĤG��Search");			
				if(aStar(pField)){			//���potential Field.
				//if(aStar(cMap)){
//System.out.println("b");
//pFile.printBoard();
					result = backTracking(level+1);
					return result;
				}else{ 	
					result.setLocation(0,initNode.getValue());	//�i�D���`�I�ڤw����,�çi�D�o�ڬO��(value��).
//System.out.println("���Ѫ��l�`�I:"+initNode.getValue());
					return result;
				}	
			}
			
			
			/*  �p�G�l�`�Isearch����,�����^�Ө즹,�}�lBackTracking*/
			if(result.x == 0){
				//search����,���^�X�Q�u�������n�[�^��.
				initList.add(0,currInitNode);	
				goalList.add(0,currGoalNode);
				
				//���^�X�Q��L��contourMap��board���nrestore.
				for(int p=0;p<width;p++){
					for(int q=0;q<height;q++){
						contourMap[p][q] = oldCMap[p][q];
						board[p][q] = oldBoard[p][q];
					}
				}
				
				/*�ˬd�Ӥl�`�I�O�_�w�b���h�Q�եιL,�Q�եιL��,�h���A�ե�*/
				for(int c=0;c<count;c++){
					if(result.y == failedNodeList[c]){
						alreadyTry = true;
					}
				}
//System.out.println("�}�lbacktracking:�^��W��contourMap");
//pFile.printBoard();
			}
		}
		return result;
	}
	
	
	
	/* A*�t��k�ϥ� */
	public boolean aStar(MyMap mp){
		ArrayList<GridNode>[] openList  = new ArrayList[min = mp.getMax()+1];	//OPEN List.
		boolean[][] visted 			    = new boolean[width][height];				//�O�����X�L����l.
		GridNode currentNode;	//�Ȧs�ثe�̨Ϊ��I.
		
		
		int x 	  = 0,
		    y 	  = 0,
		    value = 0;	
		path = new ArrayList();
		
		initNode.setParent(null);
		
		visted[initNode.getPosition().x][initNode.getPosition().y] = true;
		
		/* contourMap�̪���ê������visted,�i�H��potentialField�Ѧ� */
		for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				if(contourMap[i][j]==cMap.getMax()){
					visted[i][j] = true;						
				}
			}
		}
		
		
		insertOPEN(initNode,openList,visted,mp);		//�Ninitial node ���JOpen

		while(!success && !openIsEmpty(openList)){

			while(openList[min]== null || openList[min].isEmpty()){		
				min++;
			}
	
			currentNode = (GridNode)openList[min].remove(0);		//���ثe�̨Ϊ��I(�a�ճ̧C���I)
			x = currentNode.getPosition().x;
			y = currentNode.getPosition().y;
			value = currentNode.getValue();
			
			//���վF�I�O�_�w���X�θI��
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
			//�qgoal�}�l�V�e��parentNode,�Ǧ��������@��path.
			while(goalNode != null){
				path.add(0,goalNode);
				goalNode = goalNode.getParent();
			}
//printVisted(visted);
			return true;
		}
		else{
		//	System.out.println("Search����!!");			
			return false;
		}
	}
	
	
	public boolean isSuccess(){
		return success;
	}
	
	public ArrayList<GridNode> getPath(){
		return path;
	}
	
	//���լO�_�����X&&���I��,�Y�O�K���JOPEN(�I�sinsertOpen())
	private void testNeighbor(int x, int y, int value, ArrayList<GridNode>[] open, boolean[][] visted, GridNode cNode, MyMap mp){
		GridNode tempNode;
		
		
		//���լO�_���X�L
		if (visted[x][y] == true){
			return;
		}	
		else{
			tempNode = new GridNode(value,x,y);
			tempNode.setParent(cNode);								//x' point toward to x
			insertOPEN(tempNode,open,visted, mp);						//�����X�L���I�N�[�JOPEN

			/* �F�~�N�Onode */
			if(x == goalNode.getPosition().x && y ==goalNode.getPosition().y){
				success = true;
				goalNode = tempNode;
			}
		}	
	}
	
	
	//�P�_OPEN�O�_����array
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
	
	
	//�Nnode���JOPEN,�]���w���X,�ç�smin��index
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
		//cost�qcontourMap���o.
		if (open[cost] == null){
			open[cost] = new ArrayList();
		}
		open[cost].add(0,node);		//�i�}��node���bList���Ĥ@��element
		
		if (cost <= this.min)
			this.min = cost;
		
		visted[x][y] = true;			//�O�������X�L
	
		
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

