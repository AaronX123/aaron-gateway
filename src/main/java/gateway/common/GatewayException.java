package gateway.common;

import aaron.common.data.exception.NestedExamException;
import aaron.common.data.exception.StarterError;

/**
 * @author xiaoyouming
 * @version 1.0
 * @since 2020-04-14
 */
public class GatewayException extends NestedExamException {

    public GatewayException(String errorMessage, String errorCode) {
        super(errorMessage, errorCode);
    }

    public GatewayException(StarterError error){
        super(error.getMsg(), error.getCode());
    }

    public GatewayException(GatewayError error){
        super(error.getMsg(), error.getCode());
    }
}
