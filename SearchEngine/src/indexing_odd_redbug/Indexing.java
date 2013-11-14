package indexing_odd_redbug;

import javax.xml.parsers.*;

import org.w3c.dom.*;
import org.xml.sax.*;

import java.util.*;
import java.io.*;

/**********************************
 * To build inverted index file
 **********************************/
public class Indexing {

	/*****************************
	 * keyword -> document Id
	 *****************************/
	static Map<String, ArrayList<Integer>> map = new HashMap<String, ArrayList<Integer>>();
	
	static int fileIndex = 1;
	static String toSplit;
	static int orderIndex = 0;
	
	static public void setIndex(int index) {
		fileIndex = index;
	}
	
	static public void setLocation(String s) {
		toSplit = s;
	}
	
	
	public static void splitAndIndexing() {
			
        try {
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			DocumentBuilder parser = builderFactory.newDocumentBuilder();
			
			try {
				/**********************************************************************
				 * I don't know why, but Big5toUTF8 transform was not needed any more.
				 **********************************************************************/
				//Big5toUTF8 testfile = new Big5toUTF8();    
				//testfile.writeFile(testfile.readFile(Constant.SEGMENTEDFILE+"/segment_file"+fileIndex+".xml"));

				Document d = parser.parse(Constant.SEGMENTEDFILE+"/segment_file"+fileIndex+".xml");

				NodeList sentence = d.getElementsByTagName("sentence");
				
				for (int i = 0; i < sentence.getLength(); i++) {
					Node atom = sentence.item(i);
					String text = atom.getFirstChild().getNodeValue();
					
					String[] tokens = text.split("¡@");
					
					for(String token: tokens) {
						
						if(map.containsKey(token)){
							ArrayList<Integer> tempList = map.get(token);
	
							if (fileIndex != orderIndex){
						    	if(!(tempList.contains(Integer.valueOf(fileIndex)))){
						    		tempList.add(Integer.valueOf(fileIndex));
						    		map.put(token, tempList);
						    	}		
							}
						} else {
							ArrayList<Integer> newList = new ArrayList<Integer>();
							newList.add(Integer.valueOf(fileIndex));
							map.put(token, newList);
						}
					}
					
					orderIndex = fileIndex;
					
				    PrintWriter out1 =
				    	new PrintWriter(
				    		new BufferedWriter(
				    			new FileWriter("./InvertedIndex.txt")),true);
									    
					Collection collection = map.values();
					Set set = map.keySet();
					Iterator iterator = collection.iterator();
					Iterator iterator2 = set.iterator();
					while(iterator.hasNext()) {
						out1.print(iterator2.next() + ": ");
						out1.println(iterator.next());
					}
					out1.close();
					
				}

			} catch (SAXException e) {
				System.err.println(e);
			} catch (IOException e) {
				System.err.println(e);         
			}
		
        } catch (ParserConfigurationException e) {
        	System.err.println("You need to install a JAXP aware parser.");
        }        
	}

}
