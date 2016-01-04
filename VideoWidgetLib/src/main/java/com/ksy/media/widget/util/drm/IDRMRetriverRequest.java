package com.ksy.media.widget.util.drm;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.Calendar;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;

import android.util.Log;

import com.ksy.media.widget.util.Constants;
import com.ksy.media.widget.util.drm.AuthUtils;
import com.ksy.media.widget.util.drm.DRMKey;
import com.ksy.media.widget.util.drm.DRMRetrieverManager;
import com.ksy.media.widget.util.drm.DRMRetrieverManager.DRMInnerHandler;
import com.ksy.media.widget.util.drm.DRMRetrieverResponseHandler;

public abstract class IDRMRetriverRequest implements Runnable, Serializable {

	private static final long serialVersionUID = 1L;
	public static final String REQUEST_METHOD_TAG = "https";
	public static final String SIGNATURE_KEY_TAG = "signature";
	public static final String ACCESS_KEY_ID_KEY_TAG = "accesskey";
	public static final String EXPIRE_KEY_TAG = "expire";
	public static final String NONCE_KEY_TAG = "nonce";
	public static final String CEK_URL_KEY_TAG = "resource";
	public static final String CEK_VERSION_KEY_TAG = "version";

	public static final String SERVICE_VALUE = "service";

	public static final String KSC_DRM_HOST_PORT = "115.231.96.89:80";
	public static final String KSC_DRM_REQUEST_METHOD = "GetCek";
	public static final String ENCODING = "UTF-8";

	public Calendar calendar = Calendar.getInstance();

	private static final AllowAllHostnameVerifier HOSTNAME_VERIFIER = new AllowAllHostnameVerifier();
	private static X509TrustManager xtm = new X509TrustManager() {

		@Override
		public void checkClientTrusted(X509Certificate[] chain, String authType) {

		}

		@Override
		public void checkServerTrusted(X509Certificate[] chain, String authType) {

		}

		@Override
		public X509Certificate[] getAcceptedIssuers() {

			return null;
		}
	};
	private static X509TrustManager[] xtmArray = new X509TrustManager[] { xtm };

	private final String mCekVersion;
	private final String mCekUrl;

	private DRMInnerHandler mHandler;
	private DRMRetrieverResponseHandler mResponseHandler;

	public enum DRMMethod {
		GetCek("GetCek");

		private DRMMethod(String method) {

		}
	}

	public class DRMFullURL {

		public static final int URL_TYPE_TO_APP_SERVER = 0;
		public static final int URL_TYPE_TO_KSY_SERVER = 1;
		int mUrlType;
		final String mKSCDRMHostPort;
		final String mCustomerName;
		final DRMMethod mDRMMethod;
		String mSignature;
		final String mAccessKey;
		final String mExpire;
		final String mNonce;
		final String mCekUrl;
		final String mCekVersion;

		public DRMFullURL(String kSCDRMHostPort, String customerName, DRMMethod drmMethod, String signature, String accessKey, String expire, String noce, String cekUrl, String version) {

			this.mKSCDRMHostPort = kSCDRMHostPort;
			this.mCustomerName = customerName;
			this.mDRMMethod = drmMethod;
			this.mSignature = signature;
			this.mAccessKey = accessKey;
			this.mExpire = expire;
			this.mNonce = noce;
			this.mCekUrl = cekUrl;
			this.mCekVersion = version;
			this.mUrlType = URL_TYPE_TO_KSY_SERVER;
		}

		public DRMFullURL(String accessKey, String secretKey, String cekUrl, String version) {

			this.mKSCDRMHostPort = KSC_DRM_HOST_PORT;
			this.mCustomerName = SERVICE_VALUE;
			this.mDRMMethod = DRMMethod.GetCek;
			this.mAccessKey = accessKey;
			this.mCekUrl = cekUrl;
			this.mCekVersion = version;
			this.mExpire = String.valueOf(System.currentTimeMillis() / 1000 + 3600);
			this.mNonce = this.mExpire;

			try {
				this.mSignature = AuthUtils.calAuthorizationForDRM(secretKey, this.mExpire, this.mNonce);
			} catch (SignatureException e) {
				this.mSignature = "";
			}

			this.mUrlType = URL_TYPE_TO_KSY_SERVER;

		}

		public DRMFullURL(String cekUrl, String cekVersion) {

			this(null, null, null, null, null, null, null, cekUrl, cekVersion);
			this.mUrlType = URL_TYPE_TO_APP_SERVER;
		}

		public String generatFullUrl() throws UnsupportedEncodingException {

			if (this.mUrlType == URL_TYPE_TO_APP_SERVER)
				return null;
			StringBuffer stringBuffer = new StringBuffer(REQUEST_METHOD_TAG);
			stringBuffer.append("://").append(mKSCDRMHostPort).append("/");
			stringBuffer.append(mCustomerName).append("/").append(mDRMMethod).append("?");
			stringBuffer.append(SIGNATURE_KEY_TAG).append("=").append(URLEncoder.encode(mSignature, ENCODING)).append("&");
			stringBuffer.append(ACCESS_KEY_ID_KEY_TAG).append("=").append(URLEncoder.encode(mAccessKey, ENCODING)).append("&");
			stringBuffer.append(EXPIRE_KEY_TAG).append("=").append(mExpire).append("&");
			stringBuffer.append(NONCE_KEY_TAG).append("=").append(mNonce).append("&");
			stringBuffer.append(CEK_URL_KEY_TAG).append("=").append(mCekUrl).append("&");
			stringBuffer.append(CEK_VERSION_KEY_TAG).append("=").append(mCekVersion);
			Log.d(Constants.LOG_TAG, "retrieve drm full url :" + stringBuffer.toString());
			return stringBuffer.toString();
		}

		public boolean validate() {

			if (this.mUrlType == URL_TYPE_TO_APP_SERVER) {
				if (this.mCekUrl == null || "".equals(this.mCekUrl))
					return false;
				if (this.mCekVersion == null || "".equals(this.mCekVersion))
					return false;
				return true;
			}
			if (this.mKSCDRMHostPort == null || "".equals(this.mKSCDRMHostPort))
				return false;
			if (this.mCustomerName == null || "".equals(this.mCustomerName))
				return false;
			if (this.mDRMMethod == null || "".equals(this.mDRMMethod))
				return false;
			if (this.mSignature == null || "".equals(this.mSignature))
				return false;
			if (this.mAccessKey == null || "".equals(this.mAccessKey))
				return false;
			if (this.mExpire == null || "".equals(this.mExpire))
				return false;
			if (this.mNonce == null || "".equals(this.mNonce))
				return false;
			if (this.mCekUrl == null || "".equals(this.mCekUrl))
				return false;
			if (this.mCekVersion == null || "".equals(this.mCekVersion))
				return false;
			return true;
		}
	}

	public void setDRMInnerHandler(DRMInnerHandler handler) {

		this.mHandler = handler;
	}

	public void setDRMResponseHandler(DRMRetrieverResponseHandler handler) {

		this.mResponseHandler = handler;
	}

	public DRMRetrieverResponseHandler getResponseHandler() {

		return this.mResponseHandler;
	}

	public IDRMRetriverRequest(String cekVersion, String cekUrl) {

		this.mCekVersion = cekVersion;
		this.mCekUrl = cekUrl;
	}

	public abstract DRMFullURL retriveDRMFullUrl(String cekVersion, String cekUrl) throws Exception;

	public abstract DRMKey retriveDRMKeyFromAppServer(String cekVersion, String cekUrl);

	private void retriveDRMKeyFromKSYServer(DRMFullURL fullURL) {

		URL url = null;
		try {
			url = new URL(fullURL.generatFullUrl());
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			if (conn instanceof HttpsURLConnection) {
				SSLContext context = SSLContext.getInstance("TLS");
				context.init(new KeyManager[0], xtmArray, new SecureRandom());
				SSLSocketFactory socketFactory = context.getSocketFactory();
				conn.setSSLSocketFactory(socketFactory);
				conn.setHostnameVerifier(HOSTNAME_VERIFIER);
			}
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			int responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				DRMKey key = DRMRetrieverManager.parseDRMSecFromInputStream(conn.getInputStream());
				mHandler.sendSuccessMessage(key, mResponseHandler);
			} else {
				mHandler.sendFailedMessage(responseCode, "retrive drm key from ksy server occur an openning conn exception", new Exception(), mResponseHandler);
			}
		} catch (Exception e) {
			mHandler.sendFailedMessage(0, "retrive drm key from ksy server occur an openning conn exception", e, mResponseHandler);
		}

	}

	@Override
	public void run() {

		DRMFullURL fullURL = null;

		try {
			fullURL = retriveDRMFullUrl(this.mCekVersion, this.mCekUrl);
		} catch (Exception e) {
			if (mHandler != null) {
				mHandler.sendFailedMessage(0, "retrive drm full url from app server occur an error", e, mResponseHandler);
			}
			return;
		}

		if (fullURL == null) {
			if (mHandler != null) {
				mHandler.sendFailedMessage(0, "retrive drm full is null", new Exception(), mResponseHandler);
			}
			return;
		}

		Log.i(Constants.LOG_TAG, "full url:" + fullURL.toString());

		if (fullURL.mUrlType == DRMFullURL.URL_TYPE_TO_KSY_SERVER && !fullURL.validate()) {
			if (mHandler != null) {
				mHandler.sendFailedMessage(0, "retrive drm full is not validate", new Exception(), mResponseHandler);
			}
			return;
		}

		if (fullURL.mUrlType == DRMFullURL.URL_TYPE_TO_APP_SERVER && !fullURL.validate()) {
			if (mHandler != null) {
				mHandler.sendFailedMessage(0, "retrive drm full from app server, but result is not validate", new Exception(), mResponseHandler);
			}
			return;
		}

		if (fullURL.mUrlType == DRMFullURL.URL_TYPE_TO_APP_SERVER) {
			try {
				DRMKey key = retriveDRMKeyFromAppServer(fullURL.mCekVersion, fullURL.mCekUrl);
				if (mHandler != null) {
					mHandler.sendSuccessMessage(key, mResponseHandler);
				}
			} catch (Exception e) {
				if (mHandler != null) {
					mHandler.sendFailedMessage(0, "retrive drm key from app server occur an exception", e, mResponseHandler);
				}
			}
		} else if (fullURL.mUrlType == DRMFullURL.URL_TYPE_TO_KSY_SERVER) {
			retriveDRMKeyFromKSYServer(fullURL);
		}

	}

}
