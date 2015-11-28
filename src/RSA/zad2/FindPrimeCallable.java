package zad2;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.concurrent.Callable;

public class FindPrimeCallable implements Callable<BigInteger> {
	
	private int d;
	SecureRandom random;
	
	FindPrimeCallable(int d) {
		this.d = d;
		this.random = new SecureRandom();
	}

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
