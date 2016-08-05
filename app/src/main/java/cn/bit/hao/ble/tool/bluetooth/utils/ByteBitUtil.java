/**
 * Copyright (c) www.bugull.com
 */
package cn.bit.hao.ble.tool.bluetooth.utils;

/**
 * @author wuhao on 2016/8/5
 */
public class ByteBitUtil {
	private static final String TAG = ByteBitUtil.class.getSimpleName();

	private static final char[] HEX_CHARACTERS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'A', 'B', 'C', 'D', 'E', 'F'};

	public static String byteArrayToHexString(byte[] src) {
		if (src == null) {
			return "null";
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < src.length; ++i) {
			sb.append(i == 0 ? "0x" : " 0x").append(HEX_CHARACTERS[(src[i] & 0xF0) >>> 4])
					.append(HEX_CHARACTERS[src[i] & 0x0F]);
		}
		return sb.toString();
	}

	public static String byteToHexString(byte b) {
		return new StringBuilder().append("0x").append(HEX_CHARACTERS[(b & 0xF0) >>> 4])
				.append(HEX_CHARACTERS[b & 0x0F]).toString();
	}

}
