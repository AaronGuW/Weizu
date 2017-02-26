package com.demo.aaronapplication.alipay;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class SignUtils {
	private static final String APP_ID = "2016092401961431";
	private static final String publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvXvYM5fZnUMtX6MY+LnLC/OUN7vmo1x902k46X0N2wb0UbSaq72pPokgkO31Sg+lLt/bzV0ECmIUuP6zHLdKpExEiojjK/mfEMS3OrvJU8/kknH2zOeC7uqB4R2Z8/EsUbH7R5s6uvqqNFMcocYS" +
			"SkPHgRWc6/IydDefOMIfrqszN9DkHNdHVVownodjeLLQKahG4KTaqYwxt13fMtOoSQqKVU21CY/y2ndTIp+UU752kIrrCJXwuaNOw8nEyYHrFLbcqMztcQtRKqh3E4DsA6GjWd7jECYR0roykom8CrcLmvZ7NBGu9LWiCw+CIIbqNvK2V7kgpYtng87z+00QUwIDAQAB";

	private static final String ALGORITHM = "RSA";

	private static final String SIGN_ALGORITHMS = "SHA1WithRSA";

	private static final String SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA";

	private static final String DEFAULT_CHARSET = "UTF-8";

	private static String getAlgorithms(boolean rsa2) {
		return rsa2 ? SIGN_SHA256RSA_ALGORITHMS : SIGN_ALGORITHMS;
	}
	
	public static String sign(String content, String privateKey, boolean rsa2) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature
					.getInstance(getAlgorithms(rsa2));

			signature.initSign(priKey);
			signature.update(content.getBytes(DEFAULT_CHARSET));

			byte[] signed = signature.sign();

			return Base64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

}
