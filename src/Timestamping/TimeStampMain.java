package zad6;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class TimeStampMain {
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	TimeStampMain(String comm, String hashedFile, String ts, String timeStamp) {
		if ( comm.equals("timestamp") ) {
			timestamp(hashedFile);
		}
		else if ( comm.equals("verify")) {
			verify(hashedFile, ts, timeStamp);
		}
		else {
			System.out.println("Invalid command");
		}
	}
	
	private void generateKey() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
			SecureRandom random = new SecureRandom();
			keyGen.initialize(1024, random);
			KeyPair pair = keyGen.generateKeyPair();
			PrivateKey priv = pair.getPrivate();
			PublicKey pub = pair.getPublic();
			privateKey = priv;
			publicKey = pub;
			
			byte[] pubByte = pub.getEncoded();
			FileOutputStream pubByteOs = new FileOutputStream("public_key");
			pubByteOs.write(pubByte);
			pubByteOs.close();
			
		/*	byte[] privByte = priv.getEncoded();
			FileOutputStream privByteOs = new FileOutputStream("private_key");
			privByteOs.write(privByte);
			privByteOs.close();*/
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void readKey() {
		try {
			FileInputStream pubIs = new FileInputStream("public_key");
			byte[] encPub = new byte[pubIs.available()];
			pubIs.read(encPub);
			pubIs.close();
			X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(encPub);
			KeyFactory pubFactory = KeyFactory.getInstance("DSA", "SUN");
			publicKey = pubFactory.generatePublic(pubKeySpec);

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	private void timestamp(String hashedFile) {
		try {
			XMLDownloader download = new XMLDownloader("https://beacon.nist.gov/rest/record/last");
			download.downloadXML();
			String timeStampString = download.getTimeStampString();
			String seedValueString = download.getSeedValueString();
			
			String g = hashedFile + timeStampString + seedValueString;

			generateKey();

			//Mam już klucz prywatny, mogę podpisać g
			Signature dsa = Signature.getInstance("SHA1withDSA");
			dsa.initSign(privateKey);
			dsa.update(g.getBytes());
			byte[] realSig = dsa.sign();
			
			FileOutputStream sigOs = new FileOutputStream("ts");
			sigOs.write(realSig);
			sigOs.close();
			
			FileOutputStream stampOs = new FileOutputStream("timeStamp");
			stampOs.write(timeStampString.getBytes());
			stampOs.close();
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			System.out.println("Problem z URL");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Problem z połączeniem");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			System.out.println("parser problem");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			System.out.println("SAX problem");
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
	private void verify(String hashedFile, String ts, String timeStamp) {
		readKey();
		
		try {
			FileInputStream sigIs = new FileInputStream(ts);
			byte[] sigToVerify = new byte[sigIs.available()];
			sigIs.read(sigToVerify);
			sigIs.close();
			
			Signature dsa = Signature.getInstance("SHA1withDSA");
			dsa.initVerify(publicKey);
			
			FileInputStream timeStampIs = new FileInputStream(timeStamp);
			byte[] timeStampByte = new byte[timeStampIs.available()];
			timeStampIs.read(timeStampByte);
			timeStampIs.close();
			
			String timeStampString = new String(timeStampByte);
			String urlS = "https://beacon.nist.gov/rest/record/" + timeStampString;
			XMLDownloader download = new XMLDownloader(urlS);
			download.downloadXML();
			String seedValueString = download.getSeedValueString();
			
			
			String g = hashedFile + timeStampString + seedValueString;
			
			dsa.update(g.getBytes());
			
			boolean verified = dsa.verify(sigToVerify);
			
			if (verified) {
				System.out.println("Poprawnie zweryfikowano");
			}
			else {
				System.out.println("Nie zweryifkowano");
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		if (args.length < 3) {
			System.out.println("To small parameters; for help type -h or --help");
			System.exit(-1);
		}
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-h") || args[i].equals("--help")) {
				System.out.println("Help for timestamp program.");
				System.out.println("Usage: command parameters");
				System.out.println("Command:");
				System.out.println("timestamp");
				System.out.println("verify");
				System.out.println("Parameters:");
				System.out.println("-f fileName - timestamp or verify for file fileName");
				System.out.println("-h hashOfTheFile - hash SHA-256 in hex representation of the file to timestamp or verify");
				System.out.println("tsFileName - for verify: file with signed g");
				System.out.println("timeStampFileName - for verify: file with timeStamp");
				return;
			}
		}
		String hashedFile = "";
		if (args[1].equals("-f")) {
			//TODO: trzeba obliczyć hash pliku i przejść dalej
			//hashed_file = 
			try {
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				InputStream is = Files.newInputStream(Paths.get(args[2]));
				DigestInputStream dis = new DigestInputStream(is, md);
				byte[] byteArray = new byte[1024];
				while ( dis.read(byteArray) != -1 ) {
				}
				byte[] digest = md.digest();
				StringBuffer sb = new StringBuffer("");
				for (int i = 0; i < digest.length; i++) {
					sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
				}
				System.out.println("\nWygenerowałem hash: " + sb.toString());
				System.out.println("Długość: " + sb.toString().length());
				hashedFile = sb.toString();
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				System.out.println("Nie ma takiego algorytmu");
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println("Nie ma takiego pliku");
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			if ( args[2].length() != 64 ) {
				System.out.println("Invalid hash length");
				return;
			}
			hashedFile = args[1];
		}
		if (args.length == 5 ) new TimeStampMain(args[0], hashedFile, args[3], args[4]);
		else new TimeStampMain(args[0], hashedFile, null, null);
		

	}

}
