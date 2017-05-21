package zad2;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;

/**
 *
 * Class for concurrently find large prime numbers
 *
 * @author Aleksander Spyra
 * 2017
 */
public class PrimeThread extends Thread {

	private int d;
	PrintWriter fileWriter;
	SecureRandom random;
	
	PrimeThread(int d, PrintWriter fileWriter) {
		this.d = d;
		this.fileWriter = fileWriter;
		this.random = new SecureRandom();
	}
	
	/**
	 * When the thread runs, it generates random odd big numbers and check if it is prime.
	 * It uses https://docs.oracle.com/javase/7/docs/api/java/math/BigInteger.html#isProbablePrime(int) method
	 * Founded prime is written to the text file
	 */
	public void run() {
		BigInteger k;
		do {
			k = new BigInteger(d, random);
			if (!k.testBit(d-1)) {
				continue;
			}
		}
		while (!k.isProbablePrime(30));
		synchronized (fileWriter) {
			fileWriter.println(k.toString(16));
			fileWriter.flush();
		}

	}
}
