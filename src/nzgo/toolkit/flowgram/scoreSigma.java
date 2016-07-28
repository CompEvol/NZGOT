package nzgo.toolkit.flowgram;

/*scoring for aligning flow intensity to substring lengths
 *@author Neeraj Verma
 * */

public class scoreSigma {
	
    //param is scoring parameters(values of S(l,f) at each l) for flow intensities from 0-7 such that we can extrapolate scores at other points
    //[Figure 4 in the paper]
	//order of score values in param={ l-1.0, l-0.75, l ,l+0.75, l+1.0 }
	
	static double[][] param={{0.660, 0.00, 0.00 , 9.993, -17.903},
				    		{1.058, 11.819, 26.378, 7.919, -14.780},
				    		{2.341, 12.581, 21.812, 5.620, -9.875},
				    		{3.566, 12.702, 18.746, 6.972, -9.428},
				    		{4.691, 11.219, 13.639, 11.642, -12.514},
				    		{5.802, 8.824, 8.931, 8.279, -5.984},
				    		{6.889, 10.255, 7.924, 9.208, -5.713},
				    		{7.557, 9.025, 4.739, 8.010, -2.880}
				    		};
    		
		/*scoring for flow intensities against substring length	
		*/
	
	 public static double sigma(double intensity,int length){
			int n=param.length;
			int k=length<n ? length : n-1;
			double[] m=param[k];
			double diff=intensity-length;
			double scoreAtl=m[0];
			double score = 0;
			
			//score between interval [l-0.5 , l+0.5]
			if(-0.5<diff && diff>0.5){
				return scoreAtl;
			}
			
			//calculating score by linear resp. constant interpolation 
			if(diff<=-0.5)
				score=m[1]+diff*m[2];
			else
				score=m[3]+diff*m[4];
			
					
			if(score<scoreAtl){
				return score;
			}
			else {
				return scoreAtl;
			}
		}
		
}
