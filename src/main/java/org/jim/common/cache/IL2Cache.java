package org.jim.common.cache;

import java.io.Serializable;

/**
 * @author WChao
 * @date 2018年3月13日 下午7:47:28
 */
public interface IL2Cache {

    void putL2Async(String key, Serializable value);
}
