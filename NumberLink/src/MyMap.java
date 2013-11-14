import java.util.ArrayList;

abstract public class MyMap {
	int max;
	
	int[][] map;
	ParseFile pFile;
	
	int width;
	int height;
	GridNode initNode;
	GridNode goalNode;
	ArrayList<GridNode> initList;					//儲存所有數字的initial.
	ArrayList<GridNode> goalList;					//儲存所有數字的goal.
	
	public MyMap(ParseFile pf){
		pFile = pf;
		initList = pFile.initList;
		goalList = pFile.goalList;
		width = pf.width;
		height = pf.height;
		map = new int[width][height];
	}
	
	public int[][] getMap(){
		return map;
	}
	
	public int getMax(){
		return max;
	}
}
