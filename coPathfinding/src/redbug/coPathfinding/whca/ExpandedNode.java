package redbug.coPathfinding.whca;

import java.awt.Point;

public class ExpandedNode{
	int nodeId;
	Point position;
	ExpandedNode parentNode;
	float g;				// historyCost
	float h;				// heuristic
	int t;				// expanded time

	public ExpandedNode(int nodeId, Point position, float g, float h, int t){
		this.nodeId = nodeId;
		this.position = position;
		this.parentNode = null;
		this.g = g;
		this.h = h;
		this.t = t;
	}
	
	public int getNodeId() {
		return nodeId;
	}

	public Point getPosition() {
		return position;
	}

	public ExpandedNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(ExpandedNode parentNode) {
		this.parentNode = parentNode;
	}

	public float getCost() {
		return g;
	}

	public void setCost(float cost) {
		this.g = cost;
	}
	
	public float getHeuristic() {
		return h;
	}

	public void setHeuristic(float heuristic) {
		this.h = heuristic;
	}
	
	public int getT() {
		return t;
	}

	public void setT(int t) {
		this.t = t;
	}
	
}