package zad2;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FindPrimes {
	
	private static boolean areDuplicates(ArrayList<BigInteger> list) {
		HashSet<BigInteger> set = new HashSet<BigInteger>(list);
		if (set.size() < list.size()) {
			return true;
		}
		return false;
	}
	
	public static void findPrimes2(int k, int d) throws FindPrimesException {
		if (k < 1 || d < 1) { 
			throw new FindPrimesException();
		}
		PrintWriter fileWriter;
		try {
			fileWriter = new PrintWriter("p_output", "UTF-8");
			PrimeThread[] threads = new PrimeThread[k];
			for (int i = 0; i < k; i++) {
				threads[i] = new PrimeThread(d, fileWriter);
			}
			for (int i = 0; i < k; i++) {
				threads[i].start();
			}
			for (int i = 0; i < k; i++) {
				threads[i].join();
			}
		} catch (FileNotFoundException | UnsupportedEncodingException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList<BigInteger> findPrimes(int k, int d) throws FindPrimesException {
		if (k < 1 || d < 1) { 
			throw new FindPrimesException();
		}
		ArrayList<BigInteger> primes = new ArrayList<BigInteger>();
		do {
			List<Future<BigInteger>> primesFut = new ArrayList<Future<BigInteger>>();
			ExecutorService executor = Executors.newFixedThreadPool(k);
			for (int i = 0; i < k; i++) {
				FindPrimeCallable callable = new FindPrimeCallable(d);
				Future<BigInteger> result = executor.submit(callable);
				primesFut.add(result);
			}
			for (Future<BigInteger> fut : primesFut) {
				try {
					primes.add(fut.get());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			executor.shutdown();
		}
		while ( areDuplicates(primes) );
		return primes;
	}

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Incorrent number of parameters; expected k and d");
			System.exit(-1);
		}
		try {
			int k = Integer.parseInt(args[0]); // nr of threads = nr of generated primes
			int d = Integer.parseInt(args[1]); // length of generated primes
			ArrayList<BigInteger> primes = findPrimes(k, d);
			for (int i = 0; i < k; i++) {
				System.out.println("Wygenerowano: " + primes.get(i));
			}
		}
		catch (Exception e) {
			
		}

		

	}

}
