import java.util.ArrayList;


public class ContourMap extends MyMap {
		
	public ContourMap(ParseFile pf){
		super(pf);
		
		/* 製造等高線圖,愈外圍愈低 */
		for(int h=0;h<=height/2;h++){
			for(int j=height-1;j>=0;j--){
				for(int i=0;i<width;i++){
					if((i>=h && j>=h) && (i<=width-h-1 && j<=height-h-1))
						map[i][j] =h;
				}
			}	
		}
		max = (height<width)? height/2+1 : width/2+1;     //contourMap的至高點.
				
		for(int i=0;i<initList.size();i++){
			initNode = initList.get(i);	
			goalNode = goalList.get(i);

			//initialNode及goalNode都先預設為障礙物.
			map[initNode.getPosition().x][initNode.getPosition().y] = max;
			map[goalNode.getPosition().x][goalNode.getPosition().y] = max;
		}
		
		
		//printCMap();
	}
		
	public void printCMap(){
		System.out.println("==============================================");
		System.out.println();	
		//印出contour map
		for(int j=height-1;j>=0;j--){
			for(int i=0;i<width;i++){
				if(map[i][j]>=10)
					System.out.print(" "+map[i][j]+" ");
				else 
					System.out.print("  "+map[i][j]+" ");
			}
			System.out.println();
		}	
	}

/*	
	public static void main(String[] args) {	
		new ContourMap(new ParseFile(args[0]));
	}
*/		
}
