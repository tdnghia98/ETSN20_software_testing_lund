package reqsimile;

import java.awt.Color;
import java.util.*;

/**
 * Contains generic functions that are used throughout the application.
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public final class Common {

	/** 
	 * Creates a comma separated field list from the
	 * elements in a collection. Each field identifier is
	 * enclosed in brackets. The result may be used in SQL
	 * statements.
	 * 
	 * @param c The collection containing field strings
	 */
	public static final String getSqlFields(Collection c) {
		String sqlFields = "";
		Object field;
		
		for(Iterator it=c.iterator(); it.hasNext(); ) {
			sqlFields += "[" + ((String) it.next()) + "]";
			if (it.hasNext())
				sqlFields += ", ";
		}		
		return sqlFields; 
	}
	
	/**
	 * Converts a string returned from Collection.toString() back into a collection
	 * 
	 * @param collString The string containing the collection
	 * @return A collection 
	 */
	public static final Collection toCollection(String collString) {
		// Truncate [ and ]
		collString = collString.substring(1, collString.length()-1);
		// Split on ', ' (comma+space) and return as a list
		return new ArrayList(Arrays.asList(collString.split(", ")));
	}
	
	/**
	 * Converts a native Java array to an ArrayList
	 * 
	 * @param array The native Java array 
	 * @return An ArrayList
	 */
    public static ArrayList toArrayList(int[] array) {
    	ArrayList aL = new ArrayList(); 
    	for(int i = 0; i < array.length;i++) {
    	    aL.add(new Integer(array[i]));
    	}	
    	return aL;
    }
		
	/**
	 * Returns the time difference between a given time and the
	 * current system time. The result is returned as a string in natural language.
	 * Precision is on seconds only, as the accuracy is not very high.
	 * 
	 * @param startTime The given time to which the difference to current time shall be given.
	 * @return The difference between current time and startTime in natural language.
	 */
	public static final String timeDiff(long startTime) {
	    long diff = System.currentTimeMillis()-startTime;
    
	    long millis = diff%1000;
	    long secs = (diff/1000)%60;
	    long mins = (diff/(1000*60))%60;
	    long hs = (diff/(1000*3600))%24;
	    long days = diff/(1000*3600*24);

	    if (mins > 0)
	    	return "approximately " + mins+" minutes "+secs+" seconds"; // "+millis+"ms";
	    if (secs > 0)
	    	return "approximately " + secs+" seconds"; // "+millis+"ms";
	    else
	    	return "less than a second"; //millis+"ms";
	}
	
	/**
	 * Returns the other available source requirements set id
	 * given a requirements set id
	 * 
	 * @param reqSet A requirements set id
	 * @return The requirements set corresponding id to the given
	 */
	public static int otherReqSet(int reqSet) {
		return (reqSet==APPCONSTANTS.A ? APPCONSTANTS.B : APPCONSTANTS.A);
	}
	
	/**
	 * Blends the base color clr1 with the blend color clr2 using the multiply technique
	 * 
	 * @param clr1 Base color
	 * @param clr2 Blend color
	 * @return The blended color
	 */
	public static Color multiplyBlend(Color clr1, Color clr2) {
		return new Color( 
				Math.min(clr1.getRed(), Math.round(clr1.getRed()*clr2.getRed()/255)),
				Math.min(clr1.getGreen(), Math.round(clr1.getGreen()*clr2.getGreen()/255)),
				Math.min(clr1.getBlue(), Math.round(clr1.getBlue()*clr2.getBlue()/255)),
				200
		);
	}

	/**
	 * Pause the application
	 * 
	 * @param millis Milliseconds to pause
	 */
	public static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			// Do not care.
		}
	}
	
}
