import java.io.*;
import java.util.ArrayList;

public class ParseFile {
	int width;
	int height;
	int[][] board;								//儲存棋盤.
	InputStream sourceFile;
	ArrayList<GridNode> initList;				//儲存所有數字的initial
	ArrayList<GridNode> goalList;					//儲存所有數字的goal
	
	
	public ParseFile(String filename){
		width = height = 0;
		sourceFile = getClass().getResourceAsStream(filename);
		initList = new ArrayList();
		goalList = new ArrayList();
		
		parse();
	}
	
	public void parse(){
		String str;
		String[] tokens;
		int x	   = 0,
		    y      = 0,
		    value  = 0,
		    count  = 0;
		
		
		try {
				BufferedReader in = new BufferedReader(new InputStreamReader(sourceFile));
				
				//read first line.
				str = in.readLine();
				width = Integer.parseInt(str);
				
				//read second line.
				str = in.readLine();
				height = Integer.parseInt(str);
				board = new int[width][height];		
			
				while((str = in.readLine())!=null){
					tokens = str.split(",");
					value = Integer.parseInt(tokens[0]);
					x = Integer.parseInt(tokens[1])-1;
					y = Integer.parseInt(tokens[2])-1;
					if(x>width-1 || y >height-1){
						System.out.println("檔案格式錯誤!!");
						System.exit(0);
					}
					
					board[x][y] = value;
					if(count++ % 2 == 0){		//奇數筆存initial.
						initList.add(new GridNode(value,x,y));
					}else{						//偶數筆存goal.
						goalList.add(new GridNode(value,x,y));
					}
				}	
				in.close();
		}catch (Exception e) {
				System.out.println("檔案格式錯誤!!");
				e.printStackTrace();
		}					
		
//		printBoard();
	}
	
	public void printBoard(){
		System.out.println("==============================================");
		System.out.println();
		
		//印出board
		for(int j=height-1;j>=0;j--){
			for(int i=0;i<width;i++){
				if(board[i][j] == 0)
					System.out.print("   ");
				else if(board[i][j] >= 10)
				    System.out.print(board[i][j]+" ");
				else
					System.out.print(" "+board[i][j]+" ");
			}
			System.out.println();	
		}
	}
	
	
	
}
