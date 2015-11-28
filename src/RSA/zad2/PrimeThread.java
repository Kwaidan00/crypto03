package zad2;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.SecureRandom;

public class PrimeThread extends Thread {

	private int d;
	PrintWriter fileWriter;
	SecureRandom random;
	
	PrimeThread(int d, PrintWriter fileWriter) {
		this.d = d;
		this.fileWriter = fileWriter;
		this.random = new SecureRandom();
	}
	
	public void run() {
		BigInteger k;
		do {
			/*byte kBytes[] = new byte[d];
			random.nextBytes(kBytes);
			k = new BigInteger(kBytes);
			k = k.abs();*/
			k = new BigInteger(d, random);
			if (!k.testBit(d-1)) {
				continue;
			}
		}
		while (!k.isProbablePrime(30));
	/*	synchronized (System.out) {
			System.out.println(k.toString());
			System.out.flush();
		}*/
		synchronized (fileWriter) {
			fileWriter.println(k.toString(16));
			//fileWriter.println("Watek");
			fileWriter.flush();
		}

	}
}
