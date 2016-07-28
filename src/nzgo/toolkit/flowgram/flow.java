package nzgo.toolkit.flowgram;

/*creating single flow
 *@author Neeraj Verma
 * */

public class flow {
	 static char character;
	 static double intensity;
	public static void createFlow(char b,double f){
		flow.character=b;
		flow.intensity=f;
		flowgram.createFlowgram(character,intensity);
	}
}
	


