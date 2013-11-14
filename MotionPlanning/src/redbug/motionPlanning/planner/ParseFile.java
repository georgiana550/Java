package redbug.motionPlanning.planner;
import java.awt.geom.Point2D;
import java.io.*;

public class ParseFile {
	private static ParseFile parseFile = new ParseFile();
	MyObstacle[] obstacles;

	MyRobot[] robots;
	
	/* goalRobots旨在更新robots的goalConfig,當robots需要存取goalConfig時,
	 * 須先把goalRobots's initialConfig assign to robots's goalConfig,
	 * 否則robots仍保持更新前的goalConfig
	 */
	MyRobot[] goalRobots; 
	
	Point2D[] vertexs;

	Double[] initialConfig;

	Double[] goalConfig;

//	Point2D[] controlPoint;

	//File sourceFile[];
	InputStream[] sourceFile;

	public ParseFile(){}
	
	public static ParseFile getInstance(){
		return parseFile;
	}
	
	public void initialize(String[] files){	
		initialConfig = new Double[3];
		goalConfig = new Double[3];

		sourceFile = new InputStream[2];
		sourceFile[0] = getClass().getResourceAsStream(files[0]);
		sourceFile[1] = getClass().getResourceAsStream(files[1]);
 		
		for (int i = 0; i < sourceFile.length; i++) {
			try {

				BufferedReader in = new BufferedReader(new InputStreamReader(sourceFile[i]));
				
				if (files[i].contains("obstacle"))
					parse(in, "obstacle");
				else if (files[i].contains("robot"))
					parse(in, "robot");
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public MyObstacle[] getAllObstacles(){
		return obstacles;
	} 
	
	
	public void parse(BufferedReader in, String objectType) {
		String str;
		int num; 	// number of robot or obstacle
		int pNum; 	// number of polygon
		int vNum; 	// number of vertex
		int cNum; 	// number of controlPoint

		String[] tokens;

		try {
			if (objectType.equals("robot")) {
				//skip 一開始都所有註解
				do {
					str = in.readLine();
				} while (str.startsWith("#"));

				num = Integer.parseInt(str);
				robots = new MyRobot[num];
				goalRobots = new MyRobot[num];
				for (int i = 0; i < num; i++) {
					// skip下面兩行
					// # robot #0
					// # number of polygons
					do {
						str = in.readLine();
					} while (str.startsWith("#"));

					pNum = Integer.parseInt(str); // 取得有幾個polygon
					robots[i] = new MyRobot(pNum);
					for (int j = 0; j < pNum; j++) {
						// skip下面兩行
						// # polygon #0
						// # number of vertices
						do {
							str = in.readLine();
						} while (str.startsWith("#"));

						vNum = Integer.parseInt(str); // 取得有幾個vertexes
						vertexs = new Point2D[vNum];

						// skip # vertices
						str = in.readLine();

						for (int k = 0; k < vNum; k++) {
							str = in.readLine();
							tokens = str.split(" ");
							vertexs[k] = new Point2D.Double(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));
						}
						robots[i].addPolygon(vertexs);
					} //end of read in vertexes of one polygon
					// skip # initial configuration
					do {
						str = in.readLine();
					} while (str.startsWith("#"));

					tokens = str.split(" ");
					for (int k = 0; k < initialConfig.length; k++) {
						initialConfig[k] = Double.parseDouble(tokens[k]);
					}
					robots[i].setInitailConfig(initialConfig);
					
					// skip # goal configuration
					do {
						str = in.readLine();
					} while (str.startsWith("#"));

					tokens = str.split(" ");
					for (int k = 0; k < goalConfig.length; k++) {
						goalConfig[k] = Double.parseDouble(tokens[k]);
					}
					robots[i].setGoalConfig(goalConfig);

					// skip # number of control points
					do {
						str = in.readLine();
					} while (str.startsWith("#"));
					tokens = str.split(" ");
					cNum = Integer.parseInt(tokens[0]);
					
					Point2D[] controlPoint = new Point2D[cNum];

					for (int k = 0; k < cNum; k++) {
						// skip # control point #1
						do {
							str = in.readLine();
						} while (str.startsWith("#"));

						tokens = str.split(" ");
						controlPoint[k] = new Point2D.Double(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));
					}
					robots[i].setControlPoint(controlPoint);
					
					goalRobots[i] = (MyRobot)robots[i].clone();
					//把goalRobots的goalConfig assign to initialConfig以方便UI.class操作更新
					goalRobots[i].setInitailConfig(goalRobots[i].getGoalConfig());
					goalRobots[i].setIsGoalRobot();
				}
				
			} else if (objectType.equals("obstacle")) {
				//skip一開始所有註解
				do {
					str = in.readLine();
				} while (str.startsWith("#"));
				
				num = Integer.parseInt(str);
				obstacles = new MyObstacle[num];

				for (int i = 0; i < num; i++) {
					// skip下面兩行
					// # obstacle #0
					// # number of obstacles
					do {
						str = in.readLine();
					} while (str.startsWith("#"));

					pNum = Integer.parseInt(str); // 取得有幾個polygon
					obstacles[i] = new MyObstacle(pNum);

					for (int j = 0; j < pNum; j++) {
						// skip下面兩行
						// # polygon #0
						// # number of vertices
						do {
							str = in.readLine();
						} while (str.startsWith("#"));

						vNum = Integer.parseInt(str); // 取得有幾個vertexes
						vertexs = new Point2D[vNum];

						// skip # vertices
						str = in.readLine();

						for (int k = 0; k < vNum; k++) {
							str = in.readLine();
							tokens = str.split(" ");
							vertexs[k] = new Point2D.Double(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));
						}
						obstacles[i].addPolygon(vertexs);
					}
					// skip # initial configuration
					do {
						str = in.readLine();
					} while (str.startsWith("#"));

					tokens = str.split(" ");
					for (int k = 0; k < initialConfig.length; k++) {
						initialConfig[k] = Double.parseDouble(tokens[k]);
					}
					obstacles[i].setInitailConfig(initialConfig);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
