package nzgo.toolkit.flowgram;

import java.util.Iterator;

/*splitting input string into reference and flowgram
 *@author Neeraj Verma
 * */

public class parseInput {
	static String reference;
	
	public static void parse(String INPUT){
		   		 
		        String[] parts = INPUT.trim().split(" ",2);
				reference=parts[0];
				System.out.println("reference   "+reference+"\n"+"Flowgram    "+parts[1]+"\n\n");
				String flwgrm = parts[1].trim();		       
		        String[] part=flwgrm.split("\\s+");	 	        
		    	for(int i=0;i<part.length;i++){
		            String[] s=part[i].split("(?=(\\d)([\\.]))");
		            char b=s[0].charAt(0);
		            double f = Double.parseDouble(s[1]);
		            flow.createFlow(b,f);
		            }
		    	
		    	Iterator<Character>  itr1=flowgram.flowcharacters.iterator();
				Iterator<Double>     itr2=flowgram.intensities.iterator();
				System.out.print("FlowChar\tIntensity \n");
				while(itr1.hasNext() && itr2.hasNext()){
					    System.out.print(itr1.next() + "\t\t" + itr2.next()+"\n");
				}
	}
}

		
