import java.io.*;

/**
 * 
 * @author v. 1.0 David R. Cheriton
 * @author v. 1.1 Markus Borg
 * 
 * Simple program that calculates the value of the n:th Fiboncci number.
 *
 */
public class fib {
	
	/**
	 * @input n >= 0
	 * @output n:th Fibonacci number
	 * 
	 */
    public static int fibonacci (int num) {
		int calc = 0;  // The value to be returned
	
		if (num <= 1)  // F(0) = 0, F(1) = 1, assume num >= 0
		    calc = num;
		else           // num > 1
		    calc = fibonacci (num - 1) + fibonacci (num);

		return calc;
    }

    public static void main (String [] args) {
    	args = new String[1];
    	args[0] = "5";
    	
		int number = 0;       // The number read in
	
		// See if we have the correct number of input parameters
		if (args.length != 1) {
		    System.out.println ("You must provide a paramater of 1 number.");
		    System.exit (-1);
		}
	
		// Try to convert the parameter to a number
		try {
		    number = Integer.parseInt (args [0]);
		} catch (NumberFormatException x) {
		    System.out.println ("The parameter must be a number.");
		    System.exit (-1);
		}
	
		// Check that it is at least 0
		if (number < 0) {
		    System.out.println
		     ("Can only calculate non-negative Fibonacci number.");
		    System.exit (-1);
		}
	
		// Calculate the result
		try {
		System.out.println (fibonacci (number));
		}
		catch (Error e) {}
		
    }

}
