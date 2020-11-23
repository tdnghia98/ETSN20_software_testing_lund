package reqsimile.simcalc;

import java.util.HashMap;
import java.util.Iterator;

/**
 * TokenFrequencies temporarily holds the token frequencies for one single requirement.
 * It is used in the preprocessing stage to rebuild the complete token 
 * frequency table (for a whole requirements set), which is stored in an external
 * database table.
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class TokenFrequencies {
	
	/** The toekn frequency table */
	private HashMap tokens = new HashMap();
	
	/**
	 * Adds a token or increments its use if it already exists.
	 * 
	 * @param key The unique token key (provided by {@link Tokens})
	 */
	public void increment(int key) {
		Integer reqKey = new Integer(key);
		if (tokens.containsKey(reqKey)) {
			// Increment usage
			tokens.put(reqKey, new Integer( ((Integer) tokens.get(reqKey)).intValue() + 1 ));
		} else {
			// Add token
			tokens.put(reqKey, new Integer(1));
		}
	}
	
	
	/**
	 * Returns the frequency for a token key
	 * 
	 * @param tokenKey A unique token key available in the token frequency table. 
	 * @return The token frequency
	 */
	public Object get(Object tokenKey) {
		return tokens.get(tokenKey);
	}
	
	/**
	 * Returns the weighted frequency 1+log2(freq) for a token key.
	 * 
	 * @param tokenKey The unique token key available in the token frequency table. 
	 * @return 1+log2(x), where x is the frequency for the specified token.
	 */
	public double getLogWeight(Object tokenKey) {
		return 1 + (Math.log(((Integer) tokens.get(tokenKey)).doubleValue())/Math.log(2));
	}
	 
	/**
	 * Returns the iterator for the token keys in the frequency table.
	 * 
	 * @return The iterator for the set of unqiue token keys in the frequency table.
	 */
	public Iterator iterator() {
		return tokens.keySet().iterator();
	}

	/**
	 * Empties the token frequency table.
	 */
	public void clear() {
		tokens.clear();
	}

}
