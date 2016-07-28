package nzgo.toolkit.flowgram;

/*Scoring for Editing events
 *@author Neeraj Verma
 * */

public class scoreEdit {
	
	
	/*PARAMETERS FOR EDITING EVENTS
	 * @param mat   match score
	 * @param mis   mismatch score
	 * @param ins   insertion score
	 * @param del   deletion score
	 */
    static double mat=1.2;
	static double mis=-3.1;
	static double ins=-8.0;
	static double del=-8.0;
	
	
	/*SCORING OF EDITING EVENTS	
	 * @param e1         number of characters in t equal to b(e in paper)
	 * @param e2         number of characters in t not equal to b(ē in paper)
	 * @param length_t   length of substring t
	 * @param scoreEdit  score for editing events
	 */
	public static double sEdit(char ch,double l,String t){
		double scoreEdit = 0;
		int e1=0;
		int e2=0;
		int length_t=t.length();
		for(int i=0;i<length_t;i++){
			if(t.charAt(i)==ch){
				e1=e1+1;
			}
		}
		e2=length_t-e1;
		
		if(l==length_t){
			scoreEdit=e1*mat + e2*mis;
		//   System.out.println("\ne1="+e1+"/te2="+e2+"\tscoreEdit="+scoreEdit);
		}
		else if(l>=length_t){
			scoreEdit=e1*mat + e2*mis + (l-length_t)*ins;
			//System.out.println("\ne1="+e1+"/te2="+e2+"\tscoreEdit="+scoreEdit+"\t#match="+e1+"\t#mismatch="+e2+"\t#insertion="+(l-length_t));
		}
		else if(l<=length_t){
			scoreEdit=Math.min(e1, l)*mat + Math.max(l-e1, 0)*mis + (length_t-l)*del;
		//	System.out.println("\ne1="+e1+"/te2="+e2+"\tscoreEdit="+scoreEdit+"\t#match="+Math.min(e1, l)+"\t#mismatch="+Math.max(l-e1, 0)+"\t#deletion="+(length_t-l));
		}
		
		//System.out.println("\ne1="+e1+"\te2="+e2+"\tscoreEdit="+scoreEdit);//+"\t#match="+e1+"\t#mismatch="+e2+"\t#insertion="+(l-length_t)+"\t#deletion="+(length_t-l));
		return scoreEdit;
		}

}
