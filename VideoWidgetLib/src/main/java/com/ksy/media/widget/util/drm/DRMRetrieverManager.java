package com.ksy.media.widget.util.drm;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class DRMRetrieverManager {

	private static final String DRM_CEK_STR = "Cek";
	private static final String DRM_VERSION_STR = "Version";
	private static final String DRM_RESPONSE_HANDLER = "handler";
	private static final String DRM_ERROR_CODE = "code";
	private static final String DRM_ERROR_MESSAGE = "response_content";
	private static final String DRM_ERROR_EXCEPTION = "exception";

	private static final int DRM_RESPONSE_SUCCESS = 0;
	private static final int DRM_RESPONSE_FAILED = 1;

	private static DRMRetrieverManager mInstance;
	private final ExecutorService mThreadPool;
	private final DRMInnerHandler mhandler;

	class DRMInnerHandler extends Handler {

		public void sendSuccessMessage(DRMKey key, DRMRetrieverResponseHandler handler) {

			Message msg = mhandler.obtainMessage();
			msg.what = DRM_RESPONSE_SUCCESS;
			Bundle datas = new Bundle();
			datas.putString(DRM_CEK_STR, key.getKey());
			datas.putString(DRM_VERSION_STR, key.getVersion());
			datas.putSerializable(DRM_RESPONSE_HANDLER, handler);
			msg.setData(datas);
			mhandler.sendMessage(msg);
		}

		public void sendFailedMessage(int code, String responseMessage, Exception e, DRMRetrieverResponseHandler handler) {

			Message msg = mhandler.obtainMessage();
			msg.what = DRM_RESPONSE_FAILED;
			Bundle datas = new Bundle();
			datas.putInt(DRM_ERROR_CODE, code);
			datas.putString(DRM_ERROR_MESSAGE, responseMessage);
			datas.putSerializable(DRM_ERROR_EXCEPTION, e);
			datas.putSerializable(DRM_RESPONSE_HANDLER, handler);
			msg.setData(datas);
			mhandler.sendMessage(msg);
		}

		@Override
		public void handleMessage(Message msg) {

			int what = msg.what;
			DRMRetrieverResponseHandler handler = (DRMRetrieverResponseHandler) msg.getData().getSerializable(DRM_RESPONSE_HANDLER);
			switch (what) {
			case DRM_RESPONSE_SUCCESS:
				String cek = msg.getData().getString(DRM_CEK_STR);
				String ver = msg.getData().getString(DRM_VERSION_STR);
				handler.onSuccess(ver, cek);
				break;
			case DRM_RESPONSE_FAILED:
				int code = msg.getData().getInt(DRM_ERROR_CODE);
				String message = msg.getData().getString(DRM_ERROR_MESSAGE);
				Exception e = (Exception) msg.getData().getSerializable(DRM_ERROR_EXCEPTION);
				handler.onFailure(code, message, e);
				break;
			}
		}
	}

	private DRMRetrieverManager() {

		mThreadPool = Executors.newCachedThreadPool();
		mhandler = new DRMInnerHandler();
	}

	public static DRMRetrieverManager getInstance() {

		synchronized (DRMRetrieverManager.class) {
			if (mInstance == null) {
				mInstance = new DRMRetrieverManager();
			}
			return mInstance;
		}
	}

	public void retrieveDRM(IDRMRetriverRequest request, DRMRetrieverResponseHandler handler) {

		request.setDRMInnerHandler(mhandler);
		request.setDRMResponseHandler(handler);
		mThreadPool.submit(request);
	}

	public static DRMKey parseDRMSecFromInputStream(InputStream inputStream) throws IOException {

		XmlPullParserFactory factory = null;
		String cek = "";
		String ver = "";
		try {
			factory = XmlPullParserFactory.newInstance();
			XmlPullParser parse = factory.newPullParser();
			parse.setInput(inputStream, "UTF-8");
			int eventType = parse.getEventType();
			while (XmlPullParser.END_DOCUMENT != eventType) {
				String nodeName = parse.getName();
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
				case XmlPullParser.END_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:
					if (DRM_CEK_STR.equalsIgnoreCase(nodeName)) {
						cek = parse.nextText();
					}
					if (DRM_VERSION_STR.equalsIgnoreCase(nodeName)) {
						ver = parse.nextText();
					}
					break;

				case XmlPullParser.END_TAG:
					break;
				case XmlPullParser.TEXT:
					break;
				default:
					break;
				}
				eventType = parse.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new DRMKey(cek, ver);

	}
}
