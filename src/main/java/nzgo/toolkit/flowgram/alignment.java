package nzgo.toolkit.flowgram;

import java.util.*;
/*Alignment of Flowgram list to DNA string
 *@author Neeraj Verma
 * */
public class alignment {
	
public static void align(ArrayList<Character> flowcharacters,ArrayList<Double> intensity,String sequence){
		
		//assert flowchars.size()==intensity.size();
		 int m=flowcharacters.size();
		 int n=sequence.length();
		 sequence=sequence.toUpperCase();
		 int i,j,k,moveForDel=1;
		 double temp=0,maxk=0;
		 
		 double[][] matrix=new double[m+1][n+1];
		 int [][] tracking=new int[m+1][n+1];
		 double[][] L =new double[m+1][n+1];
			
		for(i=0;i<m+1;i++){
			for(j=0;j<n+1;j++){
				matrix[i][j]=0;
				tracking[i][j]=moveForDel;
				L[i][j]=-1;
			}
		}
		
		//Arrays.fill(matrix, 0);
		//Arrays.fill(tracking, moveForDel);
		//Arrays.fill(L, -1.00);
		//matrix[0][0]=0;
		//because S(0,j)=j*(delete penalty) in GCB paper
		
		//System.out.println("\n#flows m="+m+"\t#sequence length n="+n);
		
		for(j=0;j<n+1;j++){
			matrix[0][j]=(scoreEdit.del)*j;
			//System.out.println("matrix[0]["+j+"]="+matrix[0][j]+"\n");
		}
	
		//s(i,0)=E(k=1-i) of v(b,f,e)
		for(i=1;i<m+1;i++){
			temp=Editing.score(flowcharacters.get(i-1),intensity.get(i-1)," ");
			matrix[i][0]=matrix[i-1][0]+temp;
			L[i][0]=temp;
			tracking[i][0]=0;
			
			//System.out.println("\n"+flowcharacters.get(i-1)+"   "+intensity.get(i-1)+"  "+"matrix["+i+"][0]="+matrix[i][0]+"  "+"L["+i+"][0]"+L[i][0]+"  "+"tracking["+i+"][0]="+tracking[i][0]);
			
		}
				
		//s(i,j)= max  {  s(i,j-1)+delete penalty
		//             {  max(s(i-1,k)+v(b(i),f(i),s(k+1....j)))[from k=0 to j]
		double scoreDel,tempscore,len;
		
		//System.out.println("\nchar\tintensity\tsequence\tscore\t\ttrack\tLength");
		
		for(i=1;i<m+1;i++){
			System.out.println("***********************************************************************************************************");
			for(j=1;j<n+1;j++){
				
				scoreDel=matrix[i][j-1]+scoreEdit.del;                  //s(i,j)=s(i,j-1)+delete
									
				ArrayList<Double> temp1=new ArrayList<Double>();
				ArrayList<Double> temp2=new ArrayList<Double>();
				
				for (k=0;k<j;k++){
					tempscore=Editing.score(flowcharacters.get(i-1),intensity.get(i-1),sequence.substring(k,j-1));
					temp1.add(matrix[i-1][k] + tempscore);
					temp2.add(tempscore);
					
					System.out.println("S("+(i-1)+")"+"("+k+") + v(b,f,t))="+(matrix[i-1][k] + tempscore)+"\tmatrix("+(i-1)+")("+k+")="+matrix[i-1][k]+"\tv(b,f,t)="+tempscore +"\t\tk=" +k+"\n");
					}
				

				maxk=Collections.max(temp1);
				int index=temp1.indexOf(maxk);
				len=temp2.get(index);
				
				System.out.println("max("+scoreDel+","+maxk+")\n");
				
				double max=Math.max(scoreDel, maxk);
				matrix[i][j]=max;
				tracking[i][j]=(max==scoreDel?moveForDel:index-j);
				L[i][j]=(max==scoreDel?-1:len);	
				System.out.println(flowcharacters.get(i-1)+"\t"+intensity.get(i-1)+"\t\t"+sequence.substring(Math.min(index, j),Math.max(index, j))+"\t\t"+matrix[i][j]+"\t\t"+tracking[i][j]+"\t"+L[i][j]+"\t\t\ti="+i+",j="+j+"\n***********************************************************************************************************\n");
		     }
		}
			printScore.print(m,n,moveForDel, matrix, tracking, L);
    }
}
