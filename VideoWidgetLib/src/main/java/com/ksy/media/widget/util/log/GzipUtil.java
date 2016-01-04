package com.ksy.media.widget.util.log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {

	public static String zip(String source) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			PrintWriter pw = new PrintWriter(new GZIPOutputStream(outputStream));
			pw.write(source);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String result = outputStream.toString();
		try {
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static String unzip(InputStream inputStream) {
		String result = null;
		try {
			GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int i;
			while ((i = gzipInputStream.read()) != -1) {
				baos.write(i);
			}
			result = baos.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public static ByteArrayOutputStream compress(String str) throws IOException {
//		byte[] blockcopy = ByteBuffer.allocate(4)
//				.order(java.nio.ByteOrder.LITTLE_ENDIAN).putInt(str.length())
//				.array();
		ByteArrayOutputStream os = new ByteArrayOutputStream(str.length());
		GZIPOutputStream gos = new GZIPOutputStream(os);
		gos.write(str.getBytes());
		gos.close();
		os.close();
//		byte[] compressed = new byte[os.toByteArray().length];
//		System.arraycopy(blockcopy, 0, compressed, 0, 4);
//		System.arraycopy(os.toByteArray(), 0, compressed, 0,
//				os.toByteArray().length);
		return os;
	}

	public static String decompress(String zipText) throws IOException {
		byte[] compressed = Base64.decode(zipText);
		if (compressed.length > 4) {
			GZIPInputStream gzipInputStream = new GZIPInputStream(
					new ByteArrayInputStream(compressed, 4,
							compressed.length - 4));

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (int value = 0; value != -1;) {
				value = gzipInputStream.read();
				if (value != -1) {
					baos.write(value);
				}
			}
			gzipInputStream.close();
			baos.close();
			String sReturn = new String(baos.toByteArray(), "UTF-8");
			return sReturn;
		} else {
			return "";
		}
	}

}
