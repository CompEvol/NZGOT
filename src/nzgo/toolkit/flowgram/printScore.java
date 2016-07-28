package nzgo.toolkit.flowgram;

/*printing the path and score while tracing back from (m,n) to (0,0)
 *@author Neeraj Verma
 * */

public class printScore {
	
		public static void print(int m,int n, int moveForDel, double[][] matrix, int[][] tracking, double[][] L){
		
		System.out.println((m+1)+" "+(n+1));
		int i=m, j=n;
		while(i>0 && j>0){
				//System.out.println("("+i+", "+j+") "+tracking[i][j]);
				int pointer=tracking[i][j];
				if(pointer==moveForDel){
					j=j-1;
					continue;
				}
				else if(pointer<=0){
					i=i-1;
					j=j+pointer;
				}
				System.out.println("----> ("+(i)+", "+(j)+")  "+pointer+"  "+ matrix[i][j]);
			}
		}
	}

