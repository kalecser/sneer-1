package sneer.crypto.impl;

import static java.lang.System.arraycopy;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.util.Locale.US;
import static sneer.commons.Codec.fromHex;
import static sneer.commons.exceptions.Exceptions.check;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.util.Arrays;

import sneer.PrivateKey;
import sneer.PublicKey;
import sneer.commons.SystemReport;
import sneer.crypto.Keys;

public class KeysImpl implements Keys {

	private static SecureRandom random = new SecureRandom();
	

	private static Provider bouncyCastle() {
		try {
			return (Provider) Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	
	public static void autotest() {
		new KeysImpl();
	}
	
	
	{
		if (Security.getProvider("BC") == null) {
			check(!isAndroid());
			Security.addProvider(bouncyCastle());
		}

		autotestInstance();
	}

	
	/** We want this to run in Android production environments to make sure the crypto providers are OK.*/
	private void autotestInstance() {
		PrivateKey prik = createPrivateKey();
		check(createPrivateKey(prik.toBytes()).equals(prik));
		check(createPrivateKey(prik.toHex()).equals(prik));
			
		PublicKey puk = prik.publicKey();
		check(createPublicKey(puk.toBytes()).equals(puk));
		check(createPublicKey(puk.toHex()).equals(puk));
		
		if (isAndroid())
			System.out.println("Crypto Keys Autotest SUCCESS: " + KeysImpl.class.getName());
	}
	
	
	public PrivateKey createPrivateKey() {
		return createPrivateKey(randomSeed());
	}

	
	public PrivateKey createPrivateKey(byte[] seed) {
		check(seed.length == 32);
	
		KeyPair pair = generateKeyPair(seed);
		
		java.security.PrivateKey prik = pair.getPrivate();
		java.security.PublicKey  puk  = pair.getPublic();
		return new PrivateKeyImpl(seed, prik, puk, encode(puk));
	}

	
	private byte[] encode(java.security.PublicKey puk) {
		ECPublicKeySpec spec;
		try {
			spec = KeyFactory.getInstance("EC").getKeySpec(puk, ECPublicKeySpec.class);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}

		byte[] x = to32bytes(spec.getW().getAffineX().toByteArray());
		byte[] y = to32bytes(spec.getW().getAffineY().toByteArray());
		return concat(x, y);
	}


	private byte[] to32bytes(byte[] integer) {
		if (integer.length == 32) return integer;
		if (integer.length == 33) {
			check(integer[0] == 0);
			return Arrays.copyOfRange(integer, 1, integer.length); //Omit first byte (always zero because of BigInteger encoding).
		}
		
		return paddedTo32Bytes(integer);
	}


	private byte[] paddedTo32Bytes(byte[] integer) {
		byte[] ret = new byte[32];
		System.arraycopy(integer, 0, ret, 32 - integer.length, integer.length);
		return ret;
	}


	public static byte[] concat(byte[] a, byte[] b) {
		byte[] ret = Arrays.copyOf(a, a.length + b.length);
		arraycopy(b, 0, ret, a.length, b.length);
		return ret;
	}


	private KeyPair generateKeyPair(byte[] seedBytes) {
		try {
			return keyGenerator(spec(), seedBytes).generateKeyPair();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	
	private ECParameterSpec spec() {
		return new ECParameterSpec(
			new EllipticCurve(
				new ECFieldFp(new BigInteger("115792089237316195423570985008687907853269984665640564039457584007908834671663")),
				new BigInteger("0"),
				new BigInteger("7")),
			new ECPoint(
				new BigInteger("55066263022277343669578718895168534326250603453777594175500187360389116729240"),
				new BigInteger("32670510020758816978083085130507043184471273380659243275938904335757337482424")),
			new BigInteger("115792089237316195423570985008687907852837564279074904382605163141518161494337"),
			1);
	}

	

	private KeyPairGenerator keyGenerator(AlgorithmParameterSpec spec, byte[] seed) throws Exception {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
		keyGen.initialize(spec, new RandomWrapper(seed));
		return keyGen;
	}
	
	public PrivateKey createPrivateKey(String hexSeed) {
		return createPrivateKey(fromHex(hexSeed));
	}
	
	
	public PublicKey createPublicKey(byte[] bytes) {
		byte[] tmp = new byte[33]; //BigInteger wastes one bit for signed representation.
		arraycopy(bytes,  0, tmp, 1, 32);
		BigInteger x = new BigInteger(tmp);
		arraycopy(bytes, 32, tmp, 1, 32);
		BigInteger y = new BigInteger(tmp);
		
		check(x.compareTo(BigInteger.ZERO) >= 0);
		check(y.compareTo(BigInteger.ZERO) >= 0);
		
		ECPoint w = new ECPoint(x, y);

		java.security.PublicKey puk;
		try {
			puk = KeyFactory.getInstance("EC").generatePublic(new ECPublicKeySpec(w, spec()));
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
		return new PublicKeyImpl(puk, bytes);
	}

	
	@Override	
	public PublicKey createPublicKey(String hex) {
		return createPublicKey(fromHex(hex));
	}

	private static byte[] randomSeed() {
		random.setSeed(currentTimeMillis());
		random.setSeed(nanoTime());
		random.setSeed(new Object().hashCode());
		random.setSeed(urandomBytes());
		
		byte[] ret = new byte[32];
		random.nextBytes(ret);
		return ret;
	}

	
	private static byte[] urandomBytes() {
		byte[] ret = new byte[32];
		try {
			DataInputStream in = new DataInputStream(new FileInputStream("/dev/urandom"));
			in.readFully(ret);
			in.close();
		} catch (Exception e) {
			check(!isAndroid());
			SystemReport.updateReport("security/random-seed/warning", "Warning: Unable to read from /dev/urandom. OK if you are not on Linux.");
		}
		return ret;
	}


	private static boolean isAndroid() {
		return System.getProperty("java.vendor").toLowerCase(US).contains("android");
	}

}
