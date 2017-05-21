package rsa;

import java.math.BigInteger;

/**
 * 
 * @author Aleksander Spyra
 *
 * 2017
 */
public class ExtendedEuclideanAlgorithm {
	
	/**
	 * 
	 * This method calculate the inverse of a modulo n
	 *
	 */
	public static BigInteger inverse(BigInteger a, BigInteger n) throws Exception {
		BigInteger t = new BigInteger("0");
		BigInteger r = n;
		BigInteger newt = new BigInteger("1");
		BigInteger newr = a;
		while (newr.compareTo(BigInteger.ZERO) != 0) {
			BigInteger quotient = r.divide(newr);
			BigInteger temp = newt;
			newt = t.subtract(quotient.multiply(newt));
			t = temp;
			temp = newr;
			newr = r.subtract(quotient.multiply(newr));
			r = temp;
		}
		if ( r.compareTo(BigInteger.ONE) > 0) {
			throw new Exception();
		}
		if ( t.compareTo(BigInteger.ZERO) < 0) {
			t = t.add(n);
		}
		return t;
	}

}
