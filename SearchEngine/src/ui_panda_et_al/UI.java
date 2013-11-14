package ui_panda_et_al;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class UI{
	private JTextField search_t;
	private JButton search_b, exit_b, about_b;
	private JFrame frame;
	private JFrame resultframe;
	private JTextArea area;
	private JScrollPane scrollpane;
	private ArrayList<String> result;
	public UI(){
		search_t = new JTextField("");
		search_t.setColumns(51);
		search_t.addActionListener(new ButtonListener());
		search_b = new JButton("Search");
		search_b.addActionListener(new ButtonListener());
		exit_b = new JButton("Exit");
		exit_b.addActionListener(new ButtonListener());
		about_b = new JButton("About");
		about_b.addActionListener(new ButtonListener());
		JPanel panel = new JPanel();
		panel.setLayout( new BoxLayout( panel , BoxLayout.X_AXIS ) );
		panel.add(search_t);
		panel.add(search_b);
		panel.add(about_b);
		panel.add(exit_b);
		frame = new JFrame("My Search Engine");
		frame.getContentPane().add(BorderLayout.CENTER, panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		frame.pack();
		center();
		frame.setVisible(true);
	}
	
	public void center() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		int x = (screenSize.width - frameSize.width) / 2;
		int y = (screenSize.height - frameSize.height) / 2;
		frame.setLocation(x, y);
	}
	
	public static void main(String [] args){
		UI ui = new UI();
	}
	
	public class ButtonListener implements ActionListener{
		public void actionPerformed(ActionEvent ae){
			if(ae.getSource()==search_b||ae.getSource()==search_t){
				try {
					resultframe = new JFrame("Search Result:");
					area = new JTextArea();
					scrollpane = new JScrollPane(area);
					area.setRows(30);
					area.setColumns(61);
					area.setEditable(false);
					resultframe.getContentPane().add(BorderLayout.CENTER, scrollpane);
					resultframe.setResizable(false);
					BufferedReader reader = new BufferedReader(new FileReader(new File("./InvertedIndex.txt")));
					String line = null;
					StringTokenizer st = new StringTokenizer(search_t.getText()," \n\r\t");
					String [] tokens = new String[st.countTokens()];
					ArrayList<String> inorder = new ArrayList<String>();
					ArrayList<String> string = new ArrayList<String>();
					int c = 0;
					while(st.hasMoreTokens()){
						String temp = st.nextToken();
						inorder.add(temp);
						tokens[c++] = temp;
					}
					InToPost itp = new InToPost(inorder);
					itp.Trans();
					//itp.show();
					
					ArrayList<String> postorder = itp.getResult();
					result = new ArrayList<String>();
					while((line = reader.readLine())!=null){
						for(int i = 0; i < tokens.length; i++){
							if(!(tokens[i].equals("OR")||tokens[i].equals("NOT")||tokens[i].equals("AND"))&&line.contains(tokens[i])){
								//area.append("關鍵字: "+tokens[i]+"\n");
								int begin = line.indexOf("[", 0);
								int end = line.indexOf("]",begin);
								String number = line.substring(begin+1, end);
								//System.out.println("number:"+number);
								
								StringTokenizer st2 = new StringTokenizer(number,", \n\r\t");
								String tempint = new String(tokens[i]+" ");
								int k = 0;
								while(st2.hasMoreTokens()){
									String stt = st2.nextToken();
									int num = Integer.parseInt(stt);
									tempint = tempint + stt + " ";
									/*
									BufferedReader reader2 = new BufferedReader(new FileReader(new File("./Doc_list.txt")));
									String line2 = null;
									while((line2 = reader2.readLine())!=null){
										int num2 = Integer.parseInt(line2.substring(0, line2.indexOf(";")));
										if(num == num2){
											String url = line2.substring(line2.indexOf("\\", line2.indexOf(".")+13)+1);
											//System.out.println(line2);
											//System.out.println(url);
											url.replace("\\", "/");
											//System.out.println(url);
											area.append("http://"+url+"\n");
										}
									}
									reader2.close();
									*/
								}
								string.add(tempint);
							}
						}
					}
					
					// inorder and integer;
					//Logic logic = new Logic();
					//logic.setInformation(inorder, integer);
					//logic.count();
					SearchLogic logic = new SearchLogic(inorder, string);
					//System.out.println("Size:"+inorder.size()+" "+string.size());
					
					//area.append("搜尋\""+search_t.getText()+"\"的結果: \n");
					logic.run();
					ArrayList<Integer> ii = logic.getAns();
					/*for(int j = 0; j < ii.size(); j++){
						System.out.print(ii.get(j)+" ");
					}
					System.out.println();*/
					BufferedReader reader2 = new BufferedReader(new FileReader(new File("./Doc_list.txt")));
					String line2 = null;
					area.append("搜尋 \""+search_t.getText()+"\" 的結果: \n");
					
					int counter = 0;
					while((line2 = reader2.readLine())!=null){
						int num2 = Integer.parseInt(line2.substring(0, line2.indexOf(";")));
						for(int j = 0; j < ii.size(); j++){
							if(num2 == ii.get(j)){
								String url = line2.substring(line2.indexOf("\\", line2.indexOf(".")+13)+1);
								url.replace("\\", "/");
								//url.replaceAll("\\", "/");
								area.append("http://"+url+"\n");
								counter++;
							}
						}
					}
					area.append("共 "+counter+" 筆資料\n");
					if(ii.size()==0){
						area.append("Sorry, not found.");
					}
					reader.close();
					resultframe.pack();
					resultframe.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			else if(ae.getSource()==exit_b){
				System.exit(0);
			}
			else if(ae.getSource()==about_b){
				JOptionPane.showMessageDialog(null, "\"Inverted indexing\" made by Odd and Redbug\n\"UI and Query logic\" made by Pandia et al.", "About", JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}
}