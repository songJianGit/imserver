package org.jim.common.exception;

/**
 * @ClassName ImException
 * @Description Im异常类
 * @Author WChao
 * @Date 2019/6/13 3:28
 * @Version 1.0
 **/
public class ImException extends Exception {

    /**
     * @param []
     * @return
     * @Author WChao
     * @Description //TODO
     **/
    public ImException() {
    }

    /**
     * @param [message]
     * @return
     * @Author WChao
     * @Description //TODO
     **/
    public ImException(String message) {
        super(message);

    }

    /**
     * @param [message, cause]
     * @return
     * @Author WChao
     * @Description //TODO
     **/
    public ImException(String message, Throwable cause) {
        super(message, cause);

    }

    /**
     * @param [message, cause, enableSuppression, writableStackTrace]
     * @return
     * @Author WChao
     * @Description //TODO
     **/
    public ImException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);

    }

    /**
     * @param [cause]
     * @return
     * @Author WChao
     * @Description //TODO
     **/
    public ImException(Throwable cause) {
        super(cause);

    }
}
