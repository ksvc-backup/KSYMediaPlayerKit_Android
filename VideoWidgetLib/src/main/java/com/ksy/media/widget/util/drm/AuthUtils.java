package com.ksy.media.widget.util.drm;

import java.security.SignatureException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import android.util.Base64;

public class AuthUtils {

	public static String calAuthorizationForDRM(String secretKey, String expire, String nonce) throws SignatureException {

		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("GET").append("\n");
		sBuffer.append(expire).append("\n");
		sBuffer.append(IDRMRetriverRequest.NONCE_KEY_TAG).append("=").append(nonce);

		String serverSignature = calculateRFC2104HMAC(sBuffer.toString(), secretKey);
		return serverSignature;

	}

	private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

	public static String calculateRFC2104HMAC(String data, String key)
			throws SignatureException {

		String result;
		try {
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(),
					HMAC_SHA1_ALGORITHM);
			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);
			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());
			// base64-encode the hmac
			// result = new String(Base64.encodeBase64(rawHmac), "GBK");
			result = Base64.encodeToString(rawHmac, Base64.NO_WRAP);
		} catch (Exception e) {
			throw new SignatureException("Failed to generate HMAC : " + e);
		}
		return result;
	}

}
