package com.ksy.media.widget.util.log;

	public final class Base64 {
	    private static final byte[] DECODE_MAP;
	    private static final char[] ENCODE_MAP = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_"
	            .toCharArray();
	 
	    static {
	        DECODE_MAP = new byte[128];
	        for (byte b = 0; b < 64; ++b) {
	            DECODE_MAP[ENCODE_MAP[b]] = b;
	        }
	        DECODE_MAP['+'] = 62;
	        DECODE_MAP['/'] = 63;
	    }
	 
	    public static byte[] decode(final String base64String) {
	        final byte[] buffer = new byte[decodeLength(base64String)];
	        decode(base64String, buffer, 0);
	        return buffer;
	    };
	 
	    public static int decode(final String base64String, final byte[] buffer,
	            final int offset) {
	        int p = 0;
	        final byte[] base64Chars = base64String.getBytes();
	        for (int i = 0; i < base64Chars.length; ++i) {
	            if (i % 4 == 0) {
	                buffer[offset + i - p] = (byte) (DECODE_MAP[base64Chars[i]] << 2);
	            } else if (i % 4 == 1 && offset + i - p - 1 < buffer.length) {
	                buffer[offset + i - p - 1] |= DECODE_MAP[base64Chars[i]] >> 4;
	                if (offset + i - p < buffer.length) {
	                    buffer[offset + i - p] = (byte) (DECODE_MAP[base64Chars[i]] << 4);
	                }
	            } else if (i % 4 == 2 && offset + i - p - 1 < buffer.length) {
	                buffer[offset + i - p - 1] |= DECODE_MAP[base64Chars[i]] >>> 2;
	                if (offset + i - p < buffer.length) {
	                    buffer[offset + i - p] = (byte) (DECODE_MAP[base64Chars[i]] << 6);
	                }
	            } else if (i % 4 == 3 && offset + i - p - 1 < buffer.length) {
	                buffer[offset + i - p - 1] |= DECODE_MAP[base64Chars[i]];
	                p++;
	            }
	        }
	        return decodeLength(base64String);
	    }
	 
	    public static int decodeLength(final String base64String) {
	        final int originalLength = base64String.length();
	        if (originalLength == 0) {
	            return 0;
	        } else if (base64String.charAt(originalLength - 2) == '=') {
	            return (originalLength - 2) * 3 / 4;
	        } else if (base64String.charAt(originalLength - 1) == '=') {
	            return (originalLength - 1) * 3 / 4;
	        } else {
	            return originalLength * 3 / 4;
	        }
	    }
	 
	    public static String encode(final byte[] bytes) {
	        return encode(bytes, 0, bytes.length);
	    }
	 

	    public static String encode(final byte[] bytes, final int offset,
	            final int length) {
	        final StringBuilder buffer = new StringBuilder(length * 3);
	        for (int i = offset; i < offset + length; i += 3) {
	            // p's are the segments for each byte. For every triple there are 6
	            // segments
	            int p0 = bytes[i] & 0xFC;
	            p0 >>= 2;
	 
	            int p1 = bytes[i] & 0x03;
	            p1 <<= 4;
	 
	            int p2;
	            int p3;
	            if (i + 1 < offset + length) {
	                p2 = bytes[i + 1] & 0xF0;
	                p2 >>= 4;
	                p3 = bytes[i + 1] & 0x0F;
	                p3 <<= 2;
	            } else {
	                p2 = 0;
	                p3 = 0;
	            }
	            int p4;
	            int p5;
	            if (i + 2 < offset + length) {
	                p4 = bytes[i + 2] & 0xC0;
	                p4 >>= 6;
	                p5 = bytes[i + 2] & 0x3F;
	            } else {
	                p4 = 0;
	                p5 = 0;
	            }
	 
	            if (i + 2 < offset + length) {
	                buffer.append(ENCODE_MAP[p0]);
	                buffer.append(ENCODE_MAP[p1 | p2]);
	                buffer.append(ENCODE_MAP[p3 | p4]);
	                buffer.append(ENCODE_MAP[p5]);
	            } else if (i + 1 < offset + length) {
	                buffer.append(ENCODE_MAP[p0]);
	                buffer.append(ENCODE_MAP[p1 | p2]);
	                buffer.append(ENCODE_MAP[p3]);
	                buffer.append('=');
	            } else {
	                buffer.append(ENCODE_MAP[p0]);
	                buffer.append(ENCODE_MAP[p1 | p2]);
	                buffer.append('=');
	                buffer.append('=');
	            }
	        }
	        return buffer.toString();
	    }

	    private Base64() {
	 
	    }
}