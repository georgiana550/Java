package ui_panda_et_al;
import java.util.*;

public class SearchLogic {
	SearchLogic(ArrayList<String> in, ArrayList<String> s){
		//in 是邏輯運算, s 是 ("政大", "1", "4", "7")
		logicString = s;
		inputString = in;
		inPage = new ArrayList<Integer[]>();
		wordNum = new ArrayList<WordNum>();
		pageDate = new ArrayList<ArrayList<Integer>>();
		stack = new LinkedList<ArrayList<Integer>>();
	}
	
	public void run(){
		setData();
		InToPost intopost = new InToPost(inputString);
		intopost.Trans();
		ArrayList<String> result=intopost.getResult();
		
		for(int i=0; i<result.size(); i++){
			String sTmp = result.get(i);
			
			if(sTmp.equals("AND")){
				stack.add(and(stack.pollLast(),stack.pollLast()));
			}else if(sTmp.equals("OR")){
				stack.add(or(stack.pollLast(),stack.pollLast()));
			}else if(sTmp.equals("NOT")){
				stack.add(not(stack.pollLast(),stack.pollLast()));
			}else{
				stack.add(pageDate.get(getNum(sTmp)));
			}
		}
		
	}
	
	public ArrayList<Integer> getAns(){
		return stack.getFirst();
	}
	
	private void setData(){
		int dataNumCounter = 0;
		
		for(int i=0; i<logicString.size(); i++){
			String stringTmp = new String(logicString.get(i));
			
			String[] stringAr = stringTmp.split(" ");
			if(hasAdd(stringAr[0])){
				for(int j=1; j<stringAr.length;j++){
					pageDate.get(getNum(stringAr[0])).add(new Integer(Integer.parseInt(stringAr[j])));
				}
			}else{
				ArrayList<Integer> inTmp = new ArrayList<Integer>();
				WordNum wnTmp = new WordNum(stringAr[0], dataNumCounter);
				wordNum.add(wnTmp);
				
				for(int j=1; j<stringAr.length; j++){
					inTmp.add(new Integer(Integer.parseInt(stringAr[j])));
				}
				pageDate.add(inTmp);
				dataNumCounter++;
			}
		}
	}
	
	private boolean hasAdd(String s){
		for(int i=0; i<wordNum.size(); i++){
			if(s.equals(wordNum.get(i).getStr())){
				return true;
			}
		}
		return false;
	}
	
	private int getNum(String s){
		for(int i=0; i<wordNum.size(); i++){
			if(s.equals(wordNum.get(i).getStr())){
				return wordNum.get(i).getNum();
			}
		}
		
		return -1;
	}
	
	private ArrayList<Integer> and(ArrayList<Integer> i1, ArrayList<Integer> i2){
		ArrayList<Integer> r = new ArrayList<Integer>();
		
		for(int i=0; i<i1.size(); i++){
			Integer tmpi1 = new Integer(i1.get(i));
			
			for(int j=0; j<i2.size(); j++){	
				Integer tmpi2 = new Integer(i2.get(j));
				
				if(tmpi1.equals(tmpi2)){
					r.add(tmpi1);
				}
			}
		}
		
		return r;
	}
	
	private ArrayList<Integer> or(ArrayList<Integer> i1, ArrayList<Integer> i2){
		ArrayList<Integer> r = new ArrayList<Integer>();
		
		for(int i=0; i<i1.size(); i++){
			Integer tmpi1 = new Integer(i1.get(i));
			
			r.add(tmpi1);
		}
		
		for(int i=0; i<i2.size(); i++){
			Integer tmpi2 = new Integer(i2.get(i));
			
			for(int j=0; j<r.size(); j++){
				if(tmpi2.equals(r.get(j))){
					continue;
				}
			}
			
			r.add(tmpi2);
		}
		
		return r;
	}	
	
	private ArrayList<Integer> not(ArrayList<Integer> i2, ArrayList<Integer> i1){
		ArrayList<Integer> r = new ArrayList<Integer>();
		
		for(int i=0; i<i1.size(); i++){
			Integer tmpi1 = new Integer(i1.get(i));
			boolean lock = true;
			
			for(int j=0; j<i2.size(); j++){	
				Integer tmpi2 = new Integer(i2.get(j));
				
				if(tmpi1.equals(tmpi2)){
					//r.add(tmpi1);
					lock = false;
				}
			}
			
			if(lock){
				r.add(tmpi1);
			}
		}
		
		return r;
	}
	
	class WordNum{
		WordNum(String s, int i){
			str =new String(s);
			in = i;
		}
		
		public int getNum(){
			return in;
		}
		
		public String getStr(){
			return str;
		}
		
		private String str;
		private int in;
	}
	
	
	ArrayList<String> inputString;
	ArrayList<String> logicString;
	ArrayList<Integer[]> inPage; 
	ArrayList<WordNum> wordNum;
	ArrayList<ArrayList<Integer>> pageDate;
	LinkedList<ArrayList<Integer>> stack;
}
