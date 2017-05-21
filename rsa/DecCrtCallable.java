package rsa;

import java.math.BigInteger;
import java.util.concurrent.Callable;
/**
 * 
 * @author Aleksander Spyra
 * The DecCrtCallable is the class which returns value (Callable).
 * It is needed to RSA decode in CRT version (Chinese remainder theorem).
 * 
 * 2017
 */
public class DecCrtCallable implements Callable<BigInteger> {
	private BigInteger pi;
	private BigInteger d;
	private BigInteger n;
	private BigInteger c;
	
	DecCrtCallable(BigInteger pi, BigInteger d, BigInteger n, BigInteger c) {
		this.pi = pi;
		this.d = d;
		this.n = n;
		this.c = c;
	}

	/**
	 * Function call returns the product 
	 * M_i * N_i * y_i, where
	 * 
	 * M_i = C^(d mod (p_i - 1)) mod p_i
	 * N_i = N/p_i
	 * y_i = N_i^(-1) mod p_i
	 * 
	 * When we will add returns from all threads and we will take it modulo N
	 * we will have searched number (decoded value for RSA).
	 *
	 * @return M_i * N_i * y_i
	 * 
	 * http://www.di-mgt.com.au/crt_rsa.html
	 */
	@Override
	public BigInteger call() throws Exception {
		BigInteger di = d.mod(pi.subtract(BigInteger.ONE));
		
		BigInteger mi = c.modPow(di, pi);
		BigInteger ni = n.divide(pi);
		BigInteger yi = ni.modInverse(pi);
		return (mi.multiply(ni)).multiply(yi);
	}

}
