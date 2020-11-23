/**
 * Implementation of insertion sort with several errors.
 * 
 * @author Norm Matloff, Original C version
 * @author Markus Borg, Modified Java version
 * 
 * Version 1.0 - 2011-02-13
 *
 */
public class InsertionSorter {

	private static int[] X;  					// input array
	private static int[] Y;  					// workspace array  
	private static int numY;  					// current number of elements in Y
	private static boolean workArrayEmpty;		// Toggles when elements in Y
	
	/**
	 * @param args The numbers to be sorted.
	 */
	public static void main(String[] args) {
		X = new int[10];
		Y = new int[10];
		numY = 0;
		workArrayEmpty = true;
		
		parseInput(args);
		processData();
		printResults();
	}
	
	private static void parseInput(String[] args) {
		if (args.length != 10) {
			System.out.println("Please input 10 numbers as argument");
			System.exit(1);
		}
		
		for (int i=0; i<args.length; i++) {
			try {
				X[i] = Integer.parseInt(args[i]);
				workArrayEmpty = false;
			}
			catch (NumberFormatException e) {
				System.out.println("Invalid input");
				System.exit(1);
			}
		}
		
	}
	
	private static void processData() {  
		for (numY = 0; numY < Y.length; numY++)
			// insert new Y in the proper place 
			// among Y[0],...,Y[NumY-1]
			insert(X[numY]);
	}

	private static void insert(int newY){  
		if (workArrayEmpty = true)  { // Y empty so far
			// easy case
			Y[0] = newY;
			return;
		}
	   
		// need to insert just before the first Y 
		// element that NewY is less than
		for (int j = 0; j < numY; j++)  {
			if (newY < Y[j])  {
				// shift Y[j], Y[j+1],... rightward 
				// before inserting NewY
				scootOver(j);
				Y[j] = newY;
				return;
	      }
		}
	}
	
	private static void scootOver(int m) {
		for (int k = numY-1; k > m; k++) {
			Y[k] = Y[k-1];
		}
	}

	private static void printResults()	{
		for (int i = 0; i < Y.length; i++) {
			System.out.print(Y[i] + " ");
		}
		System.out.flush();
	}

}
