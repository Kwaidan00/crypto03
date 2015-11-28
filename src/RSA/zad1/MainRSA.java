package zad1;

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
 * 
 * @author Aleksander Spyra
 *
 */
public class MainRSA {
	
	protected static void generate(int k, int d) throws GenerateKeysException {
		if (k < 2) {
			throw new GenerateKeysException();
		}
		try {
			/*
			 * Odnajdywane jest k liczb pierwszych długości d bitów każda
			 */
			ArrayList<BigInteger> primes = zad2.FindPrimes.findPrimes(k, d);
			/*
			 * Generowanie klucza publicznego, czyli pary
			 * (n,e)
			 */
			BigInteger n = new BigInteger("1");
			for (int i = 0; i < k; i++) {
				n = n.multiply(primes.get(i));
			}
			System.out.println("Dlugosc n:" + n.bitLength());
			PrintWriter fwPublicKey = new PrintWriter("public_key", "UTF-8");
			fwPublicKey.println(n.toString(16));
			fwPublicKey.flush();
			/*
			 * Szukanie e, gdzie 1 < e < fi takie, że gcd(e,fi) == 1
			 */
			BigInteger fi = new BigInteger("1");
			for (int i = 0; i < k; i++) {
				fi = fi.multiply( primes.get(i).subtract(BigInteger.ONE) );
			}
			//SecureRandom random = new SecureRandom();
			BigInteger e = new BigInteger("65537");
			/*do {
				e = new BigInteger(fi.bitLength()/2, random);
			}
			while ( !(e.gcd(fi)).equals(BigInteger.ONE) );*/
			System.out.println("Dlugosc e:" + e.bitLength());
			fwPublicKey.println(e.toString(16));
			fwPublicKey.flush();
			fwPublicKey.close();
			/*
			 * Wyznaczanie d, takiego że d*e = 1 mod fi (odwrotność e).
			 */
			BigInteger dKey = ExtendedEuclideanAlgorithm.inverse(e, fi);
			/*
			 * Zapisywanie klucza prywatnego. Ma postać pliku tekstowego z wartościami heksadecymalnymi
			 * postaci (n, d, p_1...p_k)
			 * (wartości p_i są potrzebne dla RSA w wersji CRT
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
	
	protected static void crypt(String op, String mode, String keyFilePath, String fileToReadPath) throws CryptException {
		if ( !(op.equals("enc") || op.equals("dec")) || !(mode.equals("norm") || mode.equals("crt")) ) {
			throw new CryptException();
		}
		try {
			/*
			 * Wczytywanie wartości z pliku z kluczem
			 * Wartością w pierwszej linii jest moduł n, w drugiej - eksponenta d lub e
			 */
			BufferedReader keyFile = new BufferedReader(new FileReader(keyFilePath));
			BigInteger nKey = new BigInteger(keyFile.readLine(), 16);
			BigInteger dOrE = new BigInteger(keyFile.readLine(), 16);
			/*
			 * Wczytanie p_i, potrzebne dla dekodowania w trybie CRT.
			 */
			ArrayList<BigInteger> pArrayList = new ArrayList<BigInteger>();
			String line = "";
			while ( (line = keyFile.readLine()) != null ) {
				//System.out.println("Wczytalem: " + line);
				pArrayList.add(new BigInteger(line, 16));
			}
			keyFile.close();		
			/*
			 * Dla szyfrowania plików wynik jest zapisywany do pliku enc, 
			 * zapisanego tekstowo (heksadecymalnie); w kolejnych liniach kolejne wartości M.
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
					//System.out.println("Wczytalem " + nRead + "bajtów o wartosci" + value.toString(16));
					BigInteger toSend = value.modPow(dOrE, nKey);
					//System.out.println("Chce wyslac " + toSend.bitLength() + " bitow o wartosci " + toSend.toString(16) );
					encOut.println(toSend.toString(16));
					encOut.flush();
				}
				encOut.close();
				fileToRead.close();
			}
			/*
			 * Dla deszyfrowania - wczytywany jest plik tekstowy z szyfrogramami
			 * wynik jest zapisywany do pliku binarnego.
			 */
			else if (op.equals("dec")) {
				BufferedReader fileToEnc = new BufferedReader(new FileReader(fileToReadPath));
				FileOutputStream fileResult = new FileOutputStream("dec");
				String stringBuffer = "";

				
				while((stringBuffer = fileToEnc.readLine()) != null) {
					BigInteger value = new BigInteger(stringBuffer, 16);
					//System.out.println("Wczytalem: " + value.toString(16));
					BigInteger toSend = new BigInteger("0");
					if (mode.equals("norm")) {
						toSend = value.modPow(dOrE, nKey);
						//System.out.println("Odkodowalem do wartosci " + toSend.toString(16));
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
						//System.out.println("Odkodowalem do wartosci " + toSend.toString(16));
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
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
