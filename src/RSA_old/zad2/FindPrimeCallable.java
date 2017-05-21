package zad2;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.Callable;

/**
 *
 * Class for concurrently find large prime numbers
 * It uses Callable interface, so it returns founded numbers to the caller
 * 
 * @author Aleksander Spyra
 * 2017
 */
public class FindPrimeCallable implements Callable<BigInteger> {
	
	private int d;
	SecureRandom random;
	
	FindPrimeCallable(int d) {
		this.d = d;
		this.random = new SecureRandom();
	}

	/**
	 * When the thread runs, it generates random odd big numbers and check if it is prime.
	 * It uses https://docs.oracle.com/javase/7/docs/api/java/math/BigInteger.html#isProbablePrime(int) method
	 * It returns founded prime.
	 */
	@Override
	public BigInteger call() throws Exception {
		BigInteger k;
		do {
			k = new BigInteger(d, random);
			if (!k.testBit(d-1)) {
				continue;
			}
		}
		while (!k.isProbablePrime(30));
		return k;
	}

}
