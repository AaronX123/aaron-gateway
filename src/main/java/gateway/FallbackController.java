package gateway;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author CJF
 * @version V1.0.0
 * @date 2019/8/12
 */
@RestController
public class FallbackController {
    /**
     * 单个服务短路
     */
    @RequestMapping("/fallback")
    public Map fallback(){
        Map<String,String> errorMap = new HashMap<>();
        errorMap.put("code","200000");
        errorMap.put("msg","熔断了，请稍后再试");
        return errorMap;
    }
}

