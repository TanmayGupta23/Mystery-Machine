import java.util.Arrays;
public class Transitive_Reduction_Algorithm {

	public static void main(String[] args) {
		// Testing
		long[][] test = new long[][]{{0, 1, 0, 0}, {0, 0, 1, 0}, {0, 0, 0, 1}, {0, 0, 0, 0}};
		System.out.println(Arrays.deepToString(transitiveClosure(test)));
		
		long[][] test2 = new long[][]{{0, 1, 1, 1}, {0, 0, 1, 1}, {0, 0, 0, 1}, {0, 0, 0, 0}};
		System.out.println(Arrays.deepToString(transitiveReduction(test2)));
	}
	
	// Transitive Reduction using Aho et al.'s algorithm
	public static long[][] transitiveReduction(long[][] A) {
		long[][] B = transitiveClosure(A);
		long[][] AB = multiply(A, B);
		long[][] ReducedA = new long[A.length][A[0].length];
		for(int r=0; r<A.length; r++) for(int c=0; c<A[r].length; c++) {
			if(A[r][c]==1 && AB[r][c]==0) ReducedA[r][c]=1;
		}
		return ReducedA;
	}
	

	// Implements Warshall's Algorithm
	public static long[][] transitiveClosure(long[][] adjMatrix) {
		long[][] transitivelyClosed = new long[adjMatrix.length][adjMatrix.length];
		for(int r=0; r<adjMatrix.length; r++) for(int c=0; c<adjMatrix[r].length; c++) transitivelyClosed[r][c] = adjMatrix[r][c];
		
		for(int k=0; k<transitivelyClosed.length; k++) { // For every linking node
			for(int i=0; i<transitivelyClosed.length; i++) {
				if(k == i) continue; 
				if(transitivelyClosed[i][k]==0) continue; //reduces run time drastically if outside 3rd nested loop 
				for(int j=0; j<transitivelyClosed.length; j++) { //For every pair i, j
					if((i == j) || (k == j)) continue;
					if(transitivelyClosed[k][j]==1) transitivelyClosed[i][j]=1;
				}
			}
		}
		
		return transitivelyClosed;
	}
	
	// Will be replaced with a more efficient matrix multiplication algorithm in the future
	public static long[][] multiply(long[][] A, long[][] B) {
		long[][] product = new long[A.length][B[0].length];
		for(int r=0; r<A.length; r++) {
			for(int c=0; c<B[0].length; c++) {
				for(int i=0; i<A[0].length; i++) {
					if(A[r][i]*B[i][c] == 1) {
						product[r][c] = 1;
						break;
					}
				}
			}
		}
		return product;
	}

}
