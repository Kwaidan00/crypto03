package zad1;

import org.junit.Test;

public class MainRSATest {

	@Test
	public void testEncodeToCompareTime() {
		try {
			long start1 = System.currentTimeMillis();
			MainRSA.generate(15, 1024);
			System.out.println("Generating key takes " + (System.currentTimeMillis() - start1));
			long start2 = System.currentTimeMillis();
			MainRSA.crypt("enc", "crt", "public_key", "bible.txt");
			System.out.println("Encoding takes " + (System.currentTimeMillis() - start2));
			long start3 = System.currentTimeMillis();
			MainRSA.crypt("dec", "crt", "private_key", "enc");
			System.out.println("Decoding takes " + (System.currentTimeMillis() - start3));
		} catch (GenerateKeysException | CryptException e) {
			e.printStackTrace();
		}
		
	}

}
