package findPrimes;

import org.junit.Ignore;

//import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Tests of generating large primes
 *
 * @author Aleksander Spyra
 * 2017
 */
public class FindPrimesTest {

	@Test
	public void testFindPrimes() {
		int[] dArray = {256, 512, 1024, 2048, 3072};
		for (int k = 1; k <= 8; k++) {
			for (int i = 0; i < dArray.length; i++) {
				int d = dArray[i];
				long start = System.currentTimeMillis();
				try {
					FindPrimes.findPrimes(k, d);
				} catch (FindPrimesException e) {
					e.printStackTrace();
				}
				System.out.println("Generating " + k + " prime(s) " + d + " bit length takes " + (System.currentTimeMillis() - start));
			}
		}
	}
	
	@Ignore
	@Test
	public void testFindPrimes7680() {
		for (int k = 1; k <= 8; k++) {
			int d = 7680;
			long start = System.currentTimeMillis();
			try {
				FindPrimes.findPrimes(k, d);
			} catch (FindPrimesException e) {
				e.printStackTrace();
			}
			System.out.println("Generating " + k + " prime(s) " + d + " bit length takes " + (System.currentTimeMillis() - start));
		}
		
	}

}
