package ui_panda_et_al;
import java.util.ArrayList;

public class InToPost{
	private ArrayList<String> input;
	private ArrayList<String> stack;
	private ArrayList<String> temp;
	private ArrayList<String> post;
	
	private int getOrder(String cElement){
        if(cElement.equals("OR"))
            return 2;
        else if( cElement.equals("AND"))
            return 3;
        else if( cElement.equals("NOT"))
            return 1;
        else
            return 0;
    }
	
	
	
	public InToPost(ArrayList<String> in){
		input = in;
		post = new ArrayList<String>();
		temp = new ArrayList<String>();
		stack = new ArrayList<String>();
	}
	
	void Trans(){
        for(int i=0;i<input.size();i++){
            //saver << getOrder(In[i]) << i <<endl;
            if(getOrder(input.get(i))==0){  //use strcmp!!!
                post.add(input.get(i));
            } else{
                if(!stack.isEmpty()){
                    if( getOrder(stack.get(stack.size()-1)) > getOrder(input.get(i))){
                        while(!stack.isEmpty()){
                            post.add(stack.get(stack.size()-1));
                            stack.remove(stack.size()-1);
                        }
                    } else if( getOrder(stack.get(stack.size()-1)) == getOrder(input.get(i))){
                        while(!stack.isEmpty()){
                            if(getOrder(stack.get(stack.size()-1)) < getOrder(input.get(i)))
                                break;
                            post.add(stack.get(stack.size()-1));
                            stack.remove(stack.size()-1);
                        }
                    }
                }

                stack.add(input.get(i));
                /*
                if( getOrder(input.get(i)) == 4){
                    Stack.pop_back();
                    while( getOrder(Stack.back()) != 3 ){
                        Post.push_back(Stack.back());
                        //saver << Stack.back() << endl;
                        Stack.pop_back();
                    }
                    //Stack.pop_back();
                }
                */
            }
        }

        while(!stack.isEmpty()){
            post.add(stack.get(stack.size()-1));
            stack.remove(stack.size()-1);
        }
    }
	
	public ArrayList<String> getResult(){
		return post;
	}
	
	public void show(){
		for(int i = 0; i < post.size(); i++){
			System.out.print(post.get(i)+ " ");
		}
		System.out.println();
	}
	
}