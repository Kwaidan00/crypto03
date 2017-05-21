package zad1;

import static org.junit.Assert.*;

import java.math.BigInteger;

import org.junit.Test;

public class ExtendedEuclideanAlgorithmTest {

	@Test
	public void test1() {
		try {
			BigInteger t = ExtendedEuclideanAlgorithm.inverse(new BigInteger("2"), new BigInteger("17"));
			assertEquals(new BigInteger("9"), t);
			System.out.println(t.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test2() {
		try {
			BigInteger t = ExtendedEuclideanAlgorithm.inverse(new BigInteger("19"), new BigInteger("139"));
			System.out.println(t.toString());
			assertEquals(new BigInteger("22"), t);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test3() {
		try {
			BigInteger t = ExtendedEuclideanAlgorithm.inverse(new BigInteger("3"), new BigInteger("1094767"));
			System.out.println(t.toString());
			assertEquals(new BigInteger("729845"), t);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void test4() {
		try {
			BigInteger c = new BigInteger("513").modPow(new BigInteger("3"), new BigInteger("1094767"));
			System.out.println("c: " + c.toString());
			
			BigInteger m1 = c.modPow(new BigInteger("4445"), new BigInteger("7991"));
			BigInteger m2 = c.modPow(new BigInteger("3605"), new BigInteger("8357"));
			BigInteger m3 = c.modPow(new BigInteger("4965"), new BigInteger("17947"));
			
			System.out.println("m1: " + m1.toString());
			System.out.println("m2: " + m2.toString());
			System.out.println("m3: " + m3.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
