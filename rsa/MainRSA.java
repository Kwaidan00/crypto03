package rsa;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Implementation of RSA cryptosystem
 * 
 * @author Aleksander Spyra
 * 2017
 */
public class MainRSA {
	
	/**
	 * This method generate RSA public and private keys.
	 *
	 * Public key (n,e) is saved in public_key file
	 * 
	 */
	protected static void generate(int k, int d) throws GenerateKeysException {
		if (k < 2) {
			throw new GenerateKeysException();
		}
		try {
			/*
			 * Find k prime numbers of length d bits
			 */
			ArrayList<BigInteger> primes = findPrimes.FindPrimes.findPrimes(k, d);
			/*
			 * Generate public key. It is the tuple
			 * (n,e)
			 */
			BigInteger n = new BigInteger("1");
			for (int i = 0; i < k; i++) {
				n = n.multiply(primes.get(i));
			}
			System.out.println("Length of the n:" + n.bitLength());
			PrintWriter fwPublicKey = new PrintWriter("public_key", "UTF-8");
			fwPublicKey.println(n.toString(16));
			fwPublicKey.flush();
			/*
			 * Calculate fi, the Euler's totient function for n
			 * Save 1 < e < fi, where GCD(e,fi) == 1
			 */
			BigInteger fi = new BigInteger("1");
			for (int i = 0; i < k; i++) {
				fi = fi.multiply( primes.get(i).subtract(BigInteger.ONE) );
			}
			BigInteger e = new BigInteger("65537");
			System.out.println("Length of the e:" + e.bitLength());
			fwPublicKey.println(e.toString(16));
			fwPublicKey.flush();
			/*
			 * End of generating public key
			 */
			fwPublicKey.close();
			/*
			 * Calculate d, where d*e = 1 mod fi (inverse of e mod fi)
			 */
			BigInteger dKey = ExtendedEuclideanAlgorithm.inverse(e, fi);
			/*
			 * Save private key to the file. It is text file with hex values
			 * (n, d, p_1...p_k)
			 * (p_i is needed for RSA-CRT
			 */
			PrintWriter fwPrivateKey = new PrintWriter("private_key", "UTF-8");
			fwPrivateKey.println(n.toString(16));
			fwPrivateKey.println(dKey.toString(16));
			fwPrivateKey.flush();
			for (int i = 0; i < k; i++) {
				fwPrivateKey.println(primes.get(i).toString(16));
				fwPrivateKey.flush();
			}
			fwPrivateKey.close();
		}
		catch (Exception e) {
			System.out.println("Error");
		}
	}
	
	/*
	 * Method for RSA encrypt and decrypt file
	 *
	 * Decription is implement in normal mode or in CRT mode (Chinese remainder theorem is used, 
	 * calculations are concurrent)
	 */
	protected static void crypt(String op, String mode, String keyFilePath, String fileToReadPath) throws CryptException {
		if ( !(op.equals("enc") || op.equals("dec")) || !(mode.equals("norm") || mode.equals("crt")) ) {
			throw new CryptException();
		}
		try {
			/*
			 * Read the key file
			 * In the first line is n, in the second line - d (if private_key is read) or e (if public_key is read).
			 */
			BufferedReader keyFile = new BufferedReader(new FileReader(keyFilePath));
			BigInteger nKey = new BigInteger(keyFile.readLine(), 16);
			BigInteger dOrE = new BigInteger(keyFile.readLine(), 16);
			/*
			 * Read p_i values, which is needed to CRT decoding
			 */
			ArrayList<BigInteger> pArrayList = new ArrayList<BigInteger>();
			String line = "";
			while ( (line = keyFile.readLine()) != null ) {
				pArrayList.add(new BigInteger(line, 16));
			}
			keyFile.close();		
			/*
			 * If the file is encoded, the result is saved in "enc" text file
			 * with hex values; next lines is next M values
			 */
			if (op.equals("enc")) {
				FileInputStream fileToRead = new FileInputStream(fileToReadPath);
				PrintWriter encOut = new PrintWriter("enc", "UTF-8");
				byte[] buffer = new byte[nKey.bitLength()/8 - 1];
				int nRead = 0;
				while ((nRead = fileToRead.read(buffer)) != -1) {
					byte[] buffer2 = new byte[nRead];
					for (int i = 0; i < nRead; i++) {
						buffer2[i] = buffer[i];
					}
					BigInteger value = new BigInteger(buffer2);
					BigInteger toSend = value.modPow(dOrE, nKey);
					encOut.println(toSend.toString(16));
					encOut.flush();
				}
				encOut.close();
				fileToRead.close();
			}
			/*
			 * If the file is decoded, text file with ciphertexts is readed
			 * the result is saved to the binary file
			 */
			else if (op.equals("dec")) {
				BufferedReader fileToEnc = new BufferedReader(new FileReader(fileToReadPath));
				FileOutputStream fileResult = new FileOutputStream("dec");
				String stringBuffer = "";
				while((stringBuffer = fileToEnc.readLine()) != null) {
					BigInteger value = new BigInteger(stringBuffer, 16);
					BigInteger toSend = new BigInteger("0");
					if (mode.equals("norm")) {
						toSend = value.modPow(dOrE, nKey);
					}
					else if (mode.equals("crt")) {
						List<Future<BigInteger>> partialResults = new ArrayList<Future<BigInteger>>();
						int r = pArrayList.size();
						ExecutorService executor = Executors.newFixedThreadPool(r);
						for (int i = 0; i < r; i++) {
							DecCrtCallable thread = new DecCrtCallable(pArrayList.get(i), dOrE, nKey, value);
							Future<BigInteger> result = executor.submit(thread);
							partialResults.add(result);
						}
						for (Future<BigInteger> fut : partialResults) {
							try {
								toSend = toSend.add(fut.get());
							} catch (InterruptedException | ExecutionException e) {
								e.printStackTrace();
							}
						}
						toSend = toSend.mod(nKey);
						executor.shutdown();
					} 
					fileResult.write(toSend.toByteArray());
					fileResult.flush();
		        }
				fileToEnc.close();
				fileResult.close();
			}

		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 * @param args
	 * gen k d - generating public and private keys; modulus N = Sum(p_i), 1 <= i <= k, d is the length of p_i (in bytes).
	 * enc norm/crt public_key_file file_to_encode -> file named "enc"
	 * dec norm/crt private_key_file file_to_decode -> file named "dec"
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("To small parameters; for help type -h or --help");
			System.exit(-1);
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-h") || args[i].equals("--help")) {
				System.out.println("Help for RSA encryption/decryption program.");
				System.out.println("Parameters:");
				System.out.println("gen k d - generating public and private keys to public_key and private_key");
				System.out.println("enc norm/crt public_key_file file_to_encode - encoding file to file named enc");
				System.out.println("dec norm/crt private_key_file file_to_decode - decoding file to file named dec");
			}
		}
		if (args[0].equals("gen")) {
			int k = Integer.parseInt(args[1]);
			int d = Integer.parseInt(args[2]);
			try {
				generate(k, d);
			} catch (GenerateKeysException e) {
				System.out.println("I can not generate RSA keys from less than 2 prime numbers.");
				e.printStackTrace();
			}
		}
		else if (args[0].equals("enc") || args[0].equals("dec")) {
			if (args.length < 4) {
				System.out.println("To small parameters");
				System.exit(-1);
			}
			try {
				crypt(args[0], args[1], args[2], args[3]);
			} catch (CryptException e) {
				System.out.println("Incorrect operation or mode");
				e.printStackTrace();
			}
		}
		else {
			System.out.println("Incorrect operation");
		}
	}
	
}
