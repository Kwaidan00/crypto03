package zad1;

import java.math.BigInteger;
import java.util.concurrent.Callable;
/**
 * 
 * @author Aleksander Spyra
 * Klasa DecCrtCallable jest wątkiem zwracającym wartość (Callable).
 * Służy do dekodowania RSA w wersji CRT (Chińskim Twierdzeniu o Resztach).
 * 
 *
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
	 * Funkcja call zwraca przy wywołaniu iloczyn
	 * M_i * N_i * y_i, gdzie
	 * M_i = C^(d mod (p_i - 1)) mod p_i
	 * N_i = N/p_i
	 * y_i = N_i^(-1) mod p_i
	 * 
	 * Zsumowanie tych wartości z wszystkich wątków i wzięcie modulo N daje
	 * poszukiwaną liczbę - zdekodowaną wartość.
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
