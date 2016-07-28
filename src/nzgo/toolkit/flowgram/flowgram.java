package nzgo.toolkit.flowgram;

import java.util.ArrayList;

/*creating a list of flow characters and their associated intensities
 *@author Neeraj Verma
 * */

public class flowgram {
	static ArrayList<Character> flowcharacters=new ArrayList<Character>();
	static ArrayList<Double>    intensities=new ArrayList<Double>();

	public static void createFlowgram(char b,double f){
			flowcharacters.add(b);
			intensities.add(f);
	}
	
	public static boolean checkLength(){
	    if(flowcharacters.size()==intensities.size())
	    	return true;
	    else 
	    	return false;
	    }
}