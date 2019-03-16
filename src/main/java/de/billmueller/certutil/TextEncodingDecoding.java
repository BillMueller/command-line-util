package de.billmueller.certutil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.crypto.*;
import javax.security.cert.CertificateException;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

public class TextEncodingDecoding {

	/**
	 * The main function that starts all the other functions needed for en- or
	 * decoding
	 *
	 * @param pFile    the directory file
	 * @param fileName the name of the .txt file to be en- or decoded
	 * @param certName the name of the certificate used to en- or decode
	 * @param mode     the mode (0 = encoding, 1 = decoding)
	 */
	public void main(String pFile, String fileName, String certName, String docDirect, int mode, Main main) {
		TextEncodingDecoding tc = new TextEncodingDecoding();
		try {
			if (mode == 0)
				tc.encode(docDirect + "/" + fileName + ".txt", tc.getPublicKey(pFile + "/" + certName + ".crt", main),
						getValidity(pFile + "/" + certName + ".crt", main), false, main);
			else
				tc.decode(docDirect + "/" + fileName + ".txt",
						tc.getPrivateKey(pFile + "/" + certName + "_private_key", main), false, main);
		} catch (IOException e) {
			main.printError("file couldn't be found");
		} catch (CertificateException e) {
			main.printError("certificate couldn't be found");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			main.printError("invalid key spec");
		}
	}

	/**
	 * The function that encodes the .txt file given as input
	 *
	 * @param file     the name of the .txt file to be encoded
	 * @param pubKey   the public key needed to encode a message
	 * @param validity if the certificate the public key was taken from is valid
	 * @throws IOException is thrown if the .txt file couldn't be found
	 */
	public void encode(String file, PublicKey pubKey, boolean validity, boolean certificate, Main main)
			throws IOException {
		if (!validity)
			main.printError(
					"The certificate isn't valid. Please contact the owner of the certificate to get a new one");
		else {
			try {
				if (certificate) {
					byte[] verified;
					try (FileInputStream fis = new FileInputStream(file)) {
						verified = decryptCert(pubKey, IOUtils.toByteArray(fis), main);
						String out = new String(verified, "UTF-8");
						write(file, out, main);
					}
				} else {
					String in = read(file, main);
					if (in != null) {
						byte[] signed = encrypt(pubKey, in, main);
						try (FileOutputStream fos = new FileOutputStream(file)) {
							fos.write(signed);
						}
					}
				}
			} catch (NoSuchPaddingException nP) {
				nP.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				main.printError("RSA algorithm isn't valid");
			} catch (InvalidKeyException e) {
				main.printError("the public/private key was invalid");
			} catch (IllegalBlockSizeException e) {
				main.printError("the text is too long for the encoding (max. 501 bytes)");
			} catch (BadPaddingException bP) {
				bP.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				main.printError("UTF-8 isn't supported");
			}

		}
	}

	/**
	 * The function that encodes the .txt file given as input
	 *
	 * @param file   the name of the .txt file to be decoded
	 * @param priKey the private key needed ti decode a message
	 * @throws IOException is thrown if the .txt file couldn't be found
	 */
	public void decode(String file, PrivateKey priKey, boolean certificate, Main main) throws IOException {
		String out = null;
		try {
			if (certificate) {
				String in = read(file, main);
				if (in != null) {
					byte[] signed = encryptCert(priKey, in, main);
					try (FileOutputStream fos = new FileOutputStream(file)) {
						fos.write(signed);
					}
				}
			} else {
				byte[] verified;
				try (FileInputStream fis = new FileInputStream(file)) {
					verified = decrypt(priKey, IOUtils.toByteArray(fis), main);
					out = new String(verified, "UTF-8");
				}
			}
		} catch (NoSuchPaddingException nP) {
			nP.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			main.printError("RSA algorithm isn't valid");
		} catch (InvalidKeyException e) {
			main.printError("the public/private key was invalid");
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException bP) {
		} catch (UnsupportedEncodingException e) {
			main.printError("UTF-8 isn't supported");
		}
		write(file, out, main);
	}

	/**
	 * The encryption function
	 *
	 * @param publicKey public key needed for encryption
	 * @param message   message as String
	 * @return the encrypted message
	 */
	private byte[] encrypt(PublicKey publicKey, String message, Main main) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		main.printInfo("encrypting Text");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);

		return cipher.doFinal(message.getBytes());
	}

	/**
	 * The decryption function
	 *
	 * @param privateKey public key needed for encryption
	 * @param encrypted  encrypted message as byte array
	 * @return the decrypted message as byte array
	 */
	private byte[] decrypt(PrivateKey privateKey, byte[] encrypted, Main main) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		main.printInfo("decrypting Text");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);

		return cipher.doFinal(encrypted);
	}

	/**
	 * The encryption function
	 *
	 * @param privateKey public key needed for encryption
	 * @param message    message as String
	 * @return the encrypted message
	 */
	private byte[] encryptCert(PrivateKey privateKey, String message, Main main) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		main.printInfo("encrypting certificate");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, privateKey);

		return cipher.doFinal(message.getBytes());
	}

	/**
	 * The decryption function
	 *
	 * @param publicKey public key needed for encryption
	 * @param encrypted encrypted message as byte array
	 * @return the decrypted message as byte array
	 */
	private byte[] decryptCert(PublicKey publicKey, byte[] encrypted, Main main) throws NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		main.printInfo("decrypting certificate");
		Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.DECRYPT_MODE, publicKey);

		return cipher.doFinal(encrypted);
	}

	/**
	 * Writes the String msg to the file f at the directory pF
	 *
	 * @param f   file name of the .txt file
	 * @param msg msg to be written
	 */
	private void write(String f, String msg, Main main) {
		try {
			if (f == null) {
				main.printError("you have to enter a file name with parameter --file <filename>");
			} else {
				main.printInfo("writing text to " + f);
				BufferedWriter bw = new BufferedWriter(new FileWriter(f));
				String[] l = msg.split("\n");
				int ls = l.length, c = 0;
				while (c < ls) {
					bw.write(l[c]);
					c++;
					if (c < ls)
						bw.newLine();
				}
				bw.close();
			}
		} catch (FileNotFoundException fE) {
			main.printError("could not find file " + f + " (wrong directory or file name");
			main.printInfo("dont use example.txt as file name but only example");
		} catch (IOException ioe) {
			main.printError("could not read file " + f + ".txt");
		} catch (NullPointerException e) {
			main.printInfo("wrong private key - file is empty");
		}
	}

	/**
	 * Reads the text of the file f at the directory pF and returns it as a String
	 * with \n for every new line
	 *
	 * @param f file name of the .txt file
	 * @return msg that has been read
	 */
	private String read(String f, Main main) {
		String out = "";
		try {
			int c = 0;
			if (f == null) {
				main.printError("you have to enter a file name with parameter --file <filename>");
			} else {
				main.printInfo("reading text from " + f);
				BufferedReader br = new BufferedReader(new FileReader(f));
				String s;
				out = br.readLine();
				while ((s = br.readLine()) != null) {
					out = out + "\n" + s;
					c++;
				}
				br.close();
			}
		} catch (FileNotFoundException fE) {
			main.printError("could not find file " + f + " (wrong directory or file name)");
			main.printInfo("dont use example.txt as file name but only example");
			out = null;
		} catch (IOException ioe) {
			main.printError("could not read file " + f + ".txt");
			out = null;
		}
		return out;
	}

	/**
	 * Gets a private key from a document at the file
	 *
	 * @param file file where the private key is saved (as byte[])
	 * @return the private key
	 */
	private PrivateKey getPrivateKey(String file, Main main)
			throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
		main.printInfo("getting private key");
		KeyFactory kf = KeyFactory.getInstance("RSA");
		return kf.generatePrivate(new PKCS8EncodedKeySpec(FileUtils.readFileToByteArray(new File(file))));
	}

	/**
	 * Gets a public key from the certificate at the file
	 *
	 * @param file file where the certificate is saved
	 * @return the public key
	 */
	private PublicKey getPublicKey(String file, Main main) throws CertificateException, IOException {
		main.printInfo("getting public key");
		InputStream inStream = new FileInputStream(file);
		javax.security.cert.X509Certificate cert = javax.security.cert.X509Certificate.getInstance(inStream);
		inStream.close();
		return cert.getPublicKey();
	}

	/**
	 * Checks the validity of the certificate at the file
	 *
	 * @param file file where the certificate is saved
	 * @return true if the certificate is valid <br>
	 *         false if the certificate isn't valid
	 */
	private boolean getValidity(String file, Main main) throws CertificateException, IOException {
		EditCertificate ec = new EditCertificate();
		main.printInfo("checking validity");
		InputStream inStream = new FileInputStream(file);
		javax.security.cert.X509Certificate cert = javax.security.cert.X509Certificate.getInstance(inStream);
		inStream.close();
		return ec.testDate(cert.getNotBefore(), cert.getNotAfter());
	}
}