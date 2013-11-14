package indexing_odd_redbug;

import java.net.*;
import java.io.*;
import java.util.*;
public class Segmentation {
	int fileIndex;
	
	/**************************************************
	 * �Y�Ǳ����ɮצ��j�q��r��, �o��buffer�n�[�j.
	 * �_�h�ǰe�α�����xml�|������, �ɭP�᭱�X�{exception.
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
		      //�إߺ�����X�y
		      
		      OutputStream netOut=theSocket.getOutputStream();

		      OutputStream doc=new DataOutputStream(new BufferedOutputStream(netOut));

		      

		      //�إߤ��Ū���w�İ�
		      byte[] buf=new byte[MAX_Send_File_Buffer_Bytes];

		      int num=fos.read(buf);

		      while(num!=(-1)){//�O�_Ū�����

		             doc.write(buf,0,num);//���󤺮e�g������w�İ�

		             doc.flush();//��w�İϪ��ƾڼg���Ȥ��

		             num=fos.read(buf);//�~��q���Ū���ƾ�
		      }

		      File file=new File(Constant.SEGMENTEDFILE+"/segment_file"+ fileIndex +".xml");
		      
		      file.createNewFile();

		      RandomAccessFile raf = new RandomAccessFile(file,"rw");
		      
		      //�Ыغ��������y�����A�Ⱦ����ƾ�

		      InputStream netIn=theSocket.getInputStream();

		      InputStream in=new DataInputStream(new BufferedInputStream(netIn));
		      

		      //�Ыؽw�İϱ��������ƾ�
		      byte[] buff=new byte[MAX_Receive__File_Buffer_Bytes];
		      
		      int numm=in.read(buff); 
		    
		      raf.write(buff,0,numm);//�N�ƾڼg�����
	          raf.skipBytes(numm);//�M�����������n��Byte,�p0
		      
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
	