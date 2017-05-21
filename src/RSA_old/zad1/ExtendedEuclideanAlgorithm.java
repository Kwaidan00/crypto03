package zad1;

import java.math.BigInteger;

public class ExtendedEuclideanAlgorithm {
	
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

	/*public static BigInteger calc(BigInteger a, BigInteger b) {
		if (a.compareTo(b) < 0) {
			BigInteger temp = b;
			b = a;
			a = temp;
		}
		BigInteger x2 = new BigInteger("1");
		BigInteger x1 = new BigInteger("0");
		BigInteger y2 = new BigInteger("0");
		BigInteger y1 = new BigInteger("1");
		while (b.compareTo(BigInteger.ZERO) > 0 ) {
			
		}
		
	}*/
}
