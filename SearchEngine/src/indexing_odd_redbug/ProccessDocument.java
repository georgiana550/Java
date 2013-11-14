package indexing_odd_redbug;


import java.io.*;
import java.util.*;

public class ProccessDocument {
	ArrayList<File> fileList;
	HashMap<Integer,String> DocList;
	
	
	public ProccessDocument(){
		 fileList = new ArrayList<File>();
		 DocList = new HashMap<Integer,String>();
	}
	
	public void traverse(String str){
		File file = new File(str);
		File[] files = file.listFiles();
		for(int i = 0; i < files.length; i++) { // ���C�X�ؿ�     		
			if(files[i].isDirectory()) { //�O�_���ؿ�        
				if(!files[i].getName().equals(".svn")){
					System.out.println("[" + files[i].getPath() + "]"); // ���o���|�W
					traverse(files[i].getPath());
				}	
			}
			else {
				// �ɮץ��s�JfileList�A�ݷ|�A�C�X 
				fileList.add(files[i]);
			}

		} 
		
	}
	
	public static void main(String[] args) {
		File file = new File(Constant.CRAWLEDDATA);
		
		int Doc_counter = 0;
		ProccessDocument ds = new ProccessDocument();
		ds.traverse(file.getPath());

		// to generate the document list.
		try{
		    HTMLParser parse = new HTMLParser();
		   
		    Segmentation seg = new Segmentation();
		    
		    PrintWriter out1 =
		    	new PrintWriter(
		    		new BufferedWriter(
		    			new FileWriter("./Doc_list.txt")),true);
		    
		    
		    
			for(File f: ds.fileList){
				ds.DocList.put(Doc_counter,f.toString());
				out1.println(Doc_counter++ + ";" + f);
			}
			
			out1.close();

			Doc_counter =0;
						
			for(File f: ds.fileList) {
				parse.setDocument(f.toString());
				parse.setIndex(Doc_counter);
				parse.parsing();
				
				seg.setIndex(Doc_counter);
				
				seg.SegmentWord();
				
				Indexing.setIndex(Doc_counter++);
				Indexing.splitAndIndexing();
			}
		}
		
		catch (IOException e) {
			System.err.println(e); 	
		}
		
	}

}
