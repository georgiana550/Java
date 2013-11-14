package indexing_odd_redbug;

import java.net.*;
import java.io.*;
import java.util.*;
public class Segmentation {
	int fileIndex;
	
	/**************************************************
	 * 若傳接的檔案有大量文字時, 這個buffer要加大.
	 * 否則傳送或接收的xml會不完整, 導致後面出現exception.
	 **************************************************/
	final int MAX_Send_File_Buffer_Bytes = 100000;
	final int MAX_Receive__File_Buffer_Bytes = 100000;
	
	
	public void setIndex(int index) {
		fileIndex = index;
	}
	
	void SegmentWord()
	{
		  Socket theSocket;	
		  String hostname;
		  hostname="140.109.19.104";
		  try{
			  theSocket= new Socket(hostname,1501);
			  
			  File send_file=new File(Constant.PARSEDFILE+"/parsed_file"+fileIndex+".xml");
			 
		      FileInputStream fos=new FileInputStream(send_file);
		      //建立網路輸出流
		      
		      OutputStream netOut=theSocket.getOutputStream();

		      OutputStream doc=new DataOutputStream(new BufferedOutputStream(netOut));

		      

		      //建立文件讀取緩衝區
		      byte[] buf=new byte[MAX_Send_File_Buffer_Bytes];

		      int num=fos.read(buf);

		      while(num!=(-1)){//是否讀完文件

		             doc.write(buf,0,num);//把文件內容寫到網路緩衝區

		             doc.flush();//把緩衝區的數據寫往客戶端

		             num=fos.read(buf);//繼續從文件中讀取數據
		      }

		      File file=new File(Constant.SEGMENTEDFILE+"/segment_file"+ fileIndex +".xml");
		      
		      file.createNewFile();

		      RandomAccessFile raf = new RandomAccessFile(file,"rw");
		      
		      //創建網路接受流接受服務器文件數據

		      InputStream netIn=theSocket.getInputStream();

		      InputStream in=new DataInputStream(new BufferedInputStream(netIn));
		      

		      //創建緩衝區接收網路數據
		      byte[] buff=new byte[MAX_Receive__File_Buffer_Bytes];
		      
		      int numm=in.read(buff); 
		    
		      raf.write(buff,0,numm);//將數據寫往文件
	          raf.skipBytes(numm);//清除文件尾不必要的Byte,如0
		      
		      long len = raf.length();

		      fos.close();
		      doc.close();
		      in.close();
		      
		      raf.close();

		      theSocket.close();
		  }
		  catch (UnknownHostException e)  {
			  System.out.println("NET Error!!!");
			  System.err.println(e);
		  }
		  catch (IOException e){
				  System.err.println(e);
		  }
	}
}	
	