package reqsimile.simcalc;

import java.util.*;

/**
 * Cosine contains helper methods to calculate the Cosine angle between
 * two requirements in the word space.
 * <p>
 * In the current implementation, the Cosine is calculated in a two-step process:
 * <ul>
 * <li>Preprocessing where weights and square sums calculated
 * <li>SQL statement in which the final calculation is made
 * </ul>
 * This class contains only helper functions. For further information on
 * how the measure is calculated see {@link reqsimile.data.PreProcessRs} and {@link reqsimile.simcalc.FetchSimilar}.
 *  
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class Cosine {

	/**
	 * Calculates the square sum for the words in a collection
	 * Words are weighted as 1+log2(word frequency)
	 * 
	 * @param c
	 * @return The square sum for the words in the collection.
	 */
	public static double sqSumLog(Collection c) {
		double sqSum = 0;
		HashMap wordFreq = new HashMap();
		String token;
		for(Iterator it = c.iterator(); it.hasNext(); ) {
			token = (String) it.next();
			if(wordFreq.containsKey(token)) {
				wordFreq.put(token, new Integer(((Integer) wordFreq.get(token)).intValue()+1) );
			} else {
				wordFreq.put(token, new Integer(1));
			}
		}
		// Calculate squared sum
		for(Iterator it = wordFreq.values().iterator(); it.hasNext();  ) {
			sqSum += Math.pow( 1 + (Math.log( ((Integer) it.next()).doubleValue() )/Math.log(2)) , 2);
		}
		return sqSum;
	}
	
}
