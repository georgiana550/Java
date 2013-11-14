package indexing_odd_redbug;
import java.io.*;

public class Big5toUTF8 {
  
  String filename;
  
  /**
   * transform the file from BIG5 to UTF8
   */
  public void writeFile(byte[] str){
    try{  
        FileOutputStream fout = new FileOutputStream(filename);        
        DataOutputStream dataout = new DataOutputStream(fout);        
        dataout.write(str);
        fout.close();      
      }catch (Exception e){
        System.out.println("cant write file check writeFile method"+e);
      }
  }
  /**
   * read file
   */  
  public byte[] readFile(String filename){
    this.filename=filename;
    try{
      FileInputStream fin=new FileInputStream(filename);    
      byte[] data = new byte[fin.available()];      
      fin.read(data);                              
      String str=new String(data);
      byte[] data1=str.getBytes("UTF-8");      
      fin.close();
        System.out.println("read file to String");
        return data1;
      }catch (Exception e){
        System.out.println("cant read file¡G"+e);
      }    
      return null;
    }

}
