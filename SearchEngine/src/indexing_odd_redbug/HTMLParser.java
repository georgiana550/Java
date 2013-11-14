package indexing_odd_redbug;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.swing.text.html.HTMLEditorKit;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import java.io.*;

public class HTMLParser extends HTMLEditorKit {
	static String rawDocument;
	int fileIndex;

	HTMLParser(){};
	HTMLParser(String doc){
		rawDocument = doc;
	};
	
	void setDocument(String s){
		rawDocument = s;
	}
	
	public void setIndex(int index) {
		fileIndex = index;
	}
	
	String getDocument(){
		return rawDocument;
	}
	
	/***********************************************************************************************
	 * 1. parse the html file.
	 * 2. generate a xml file which fit the template of the CKIP(中文斷詞系統) from parsed html file.
	 ***********************************************************************************************/
	void parsing(){
		HTMLEditorKit.Parser htmlParser = new HTMLParser().getParser();
        InputStreamReader in = null;
        PrintWriter out = null;
        HTMLHandler htmlHandler = null;

        try {
            in = new InputStreamReader(new FileInputStream(rawDocument));
            htmlHandler = new HTMLHandler();
            
            /************************************************************************
             * parse the html file.
             ************************************************************************/
            htmlParser.parse(in, htmlHandler, true);
            
            /************************************************************************
             * using DOM to make a parsed file that fit to the template of the CKIP.
             ************************************************************************/
            try {
				DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
				builderFactory.setNamespaceAware(true);
				DocumentBuilder parser = builderFactory.newDocumentBuilder();
				
				try {																		
					Document d = parser.parse("./CKIP_template.xml");
					NodeList father = d.getElementsByTagName("wordsegmentation");
					Node currentNode = father.item(0);
					
					Element insertElement = d.createElement("text");
					Node textNode = d.createTextNode(htmlHandler.getTextOnly());
					
					insertElement.appendChild(textNode);
					currentNode.appendChild(insertElement);
			
					out = new PrintWriter(
							  new BufferedWriter(
								 new FileWriter(Constant.PARSEDFILE+"/parsed_file"+fileIndex+".xml")),true);
					
					out.println("<?xml version=\"1.0\" ?>");
					out.print("<" + currentNode.getNodeName());
					
					NamedNodeMap attributes = currentNode.getAttributes();
					
					for(int j = 0; j < attributes.getLength(); j++) {
						out.print(" " + attributes.item(j).getNodeName() + "=\"" + attributes.item(j).getNodeValue() + "\"");
					}
					
					out.println(">");
					
					NodeList nodelist = currentNode.getChildNodes();

					if(nodelist.getLength() > 0) {
						if(nodelist.item(0).getNodeType() != Node.TEXT_NODE) {
							out.println();
						}
						
						for(int k = 1; k < nodelist.getLength() - 1; k+=2) {						
							Node tempNode = nodelist.item(k);
							out.print("<" + tempNode.getNodeName());
							NamedNodeMap tempAttributes = tempNode.getAttributes();
							
							for(int j = 0; j < tempAttributes.getLength(); j++) {
								out.print(" " + tempAttributes.item(j).getNodeName() + "=\"" + tempAttributes.item(j).getNodeValue() + "\"");
							}
							out.println(" />");
						}
					}
					
					
					/**************************************************
					 * Text Preprocessing
					 **************************************************/
					String str = new String(htmlHandler.getTextOnly());

					str = str.replace(">","");
					str = str.replace("&","");
					str = str.replace("?","");
					str = str.replace("-","");
					str = str.replace(":","");
					str = str.replace(".","");
					out.println("<text>" + str.replace("/","") + "</text>");
					
					out.println("</" + currentNode.getNodeName() + ">");
					
					out.close();
				} catch (SAXException e) {
					System.err.println(e);
				} catch (IOException e) {
					System.err.println(e);         
				}
			
            } catch (ParserConfigurationException e) {
            	System.err.println("You need to install a JAXP aware parser.");
            }
            
            in.close();
        } catch (IOException e) {
            System.out.println(e);
        }
     
    }
		
		
}
	