package nzgo.toolkit.flowgram;
/*
 *@author Neeraj Verma
 * */

public class Main {
	public static void main(String[] args){
		String inputString="TAAGGATGTCC T1.0 A2.1 C1.1 G2.2 T3.1 A0.1 C2.7 G0.2";
		System.out.println("INPUT       "+inputString);
		parseInput.parse(inputString);
		alignment.align(flowgram.flowcharacters, flowgram.intensities, parseInput.reference);
		}
}
