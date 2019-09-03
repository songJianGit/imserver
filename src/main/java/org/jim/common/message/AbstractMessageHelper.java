/**
 *
 */
package org.jim.common.message;

import org.jim.common.ImConfig;
import org.jim.common.ImConst;

/**
 * @author HP
 *
 */
public abstract class AbstractMessageHelper implements MessageHelper, ImConst {
    protected ImConfig imConfig;

    public ImConfig getImConfig() {
        return imConfig;
    }

    public void setImConfig(ImConfig imConfig) {
        this.imConfig = imConfig;
    }
}
