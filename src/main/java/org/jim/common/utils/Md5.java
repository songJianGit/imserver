package org.jim.common.utils;

import cn.hutool.crypto.SecureUtil;

import java.io.UnsupportedEncodingException;
import java.security.SignatureException;

/**
 * @author wchao
 */
public class Md5 {

    /**
     * @param content
     * @param charset
     * @return
     * @throws SignatureException
     * @throws UnsupportedEncodingException
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错误,指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }

    /**
     * 签名字符串
     *
     * @param text          需要签名的字符串
     * @param key           密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static String sign(String text, String key, String input_charset) {
        text = text + key;
        byte[] contentBytes = getContentBytes(text, input_charset);
        return SecureUtil.md5().digestHex(contentBytes);
    }

    /**
     * 签名字符串
     *
     * @param text          需要签名的字符串
     * @param sign          签名结果
     * @param key           密钥
     * @param input_charset 编码格式
     * @return 签名结果
     */
    public static boolean verify(String text, String sign, String key, String input_charset) {
        text = text + key;
        String mysign = SecureUtil.md5().digestHex(getContentBytes(text, input_charset));
        return mysign.equals(sign);
    }


    public static void main(String[] args) {
//    	String input = "1234567中文fgfdg";
//    	System.out.println(com.talent.utils.Md5.getMD5(input));
//    	System.out.println(sign(input, "", "utf-8"));
        System.out.println(Integer.valueOf("1001001", 2).toString());


    }

    public static String getMD5(String input) {
        return sign(input, "", "utf-8");
    }

}
