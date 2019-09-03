package org.jim.common.ws;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.RandomUtil;
import org.tio.core.intf.TioUuid;

/**
 * @author WChao
 * 2017年6月5日 上午10:44:26
 */
public class WsTioUuid implements TioUuid {
    private Snowflake snowflake;

    public WsTioUuid() {
        snowflake = new Snowflake(RandomUtil.randomInt(1, 30), RandomUtil.randomInt(1, 30));
    }

    public WsTioUuid(long workerId, long datacenterId) {
        snowflake = new Snowflake(workerId, datacenterId);
    }

    /**
     * @return
     * @author wchao
     */
    @Override
    public String uuid() {
        return snowflake.nextId() + "";
    }
}
