package org.jim.common;

public interface Protocol {
    /**
     * 心跳字节
     */
    byte HEARTBEAT_BYTE = -128;

    /**
     * 握手字节
     */
    byte HANDSHAKE_BYTE = -127;

    /**
     * 协议版本号
     */
    byte VERSION = 0x01;

    String WEBSOCKET = "ws";

    String HTTP = "http";

    String TCP = "tcp";

    String COOKIE_NAME_FOR_SESSION = "jim-s";
    /**
     * 消息体最多为多少
     */
    int MAX_LENGTH_OF_BODY = (int) (1024 * 1024 * 2.1); //只支持多少M数据

    /**
     * 消息头最少为多少个字节
     */
    int LEAST_HEADER_LENGHT = 4;//1+1+2 + (2+4)

    /**
     * 加密标识位mask，1为加密，否则不加密
     */
    byte FIRST_BYTE_MASK_ENCRYPT = -128;

    /**
     * 压缩标识位mask，1为压缩，否则不压缩
     */
    byte FIRST_BYTE_MASK_COMPRESS = 0B01000000;

    /**
     * 是否有同步序列号标识位mask，如果有同步序列号，则消息头会带有同步序列号，否则不带
     */
    byte FIRST_BYTE_MASK_HAS_SYNSEQ = 0B00100000;

    /**
     * 是否是用4字节来表示消息体的长度
     */
    byte FIRST_BYTE_MASK_4_BYTE_LENGTH = 0B00010000;

    /**
     * 版本号mask
     */
    byte FIRST_BYTE_MASK_VERSION = 0B00001111;
}