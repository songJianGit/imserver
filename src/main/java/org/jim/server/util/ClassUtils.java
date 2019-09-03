package org.jim.server.util;

import cn.hutool.core.util.ClassUtil;
import org.jim.common.http.UploadFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author WChao
 * 2017年7月26日 下午6:46:11
 */
public class ClassUtils {
    private static Logger log = LoggerFactory.getLogger(ClassUtils.class);

    /**
     * @author: WChao
     */
    public ClassUtils() {
    }

    public static boolean isSimpleTypeOrArray(Class<?> clazz) {
        return ClassUtil.isSimpleTypeOrArray(clazz) || clazz.isAssignableFrom(UploadFile.class);
    }


    /**
     * @param args
     * @author: WChao
     */
    public static void main(String[] args) {
        log.info("");
    }
}
