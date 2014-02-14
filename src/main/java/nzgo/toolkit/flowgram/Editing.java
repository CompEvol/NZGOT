package nzgo.toolkit.flowgram;

import java.util.ArrayList;
import java.util.Arrays;
/*calculating v(b,f,t) 
 * @author Neeraj Verma
 * */
@SuppressWarnings("unused")
public class Editing {
			
			//total scoring for aligning one flowgram and sequence
			public static double score(char b,double intensity,String t)
			{
				int length;
				//ArrayList<double> s=new ArrayList<double>();
				double[] s=new double[16];
				for(int i=0;i<=15;i++){
					length=i;
					s[i]=(scoreEdit.sEdit(b, length, t)+scoreSigma.sigma(intensity,length));
				}
				Arrays.sort(s);
				//System.out.println(" "+scoreEdit.sEdit(b, length, t)+scoreSigma.sigma(intensity,length));
				return s[15];
			}
	      
	  }

