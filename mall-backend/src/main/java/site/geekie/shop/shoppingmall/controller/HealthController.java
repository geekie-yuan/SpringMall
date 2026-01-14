package site.geekie.shop.shoppingmall.controller;

import site.geekie.shop.shoppingmall.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 提供应用健康状态检查接口
 *
 * 主要用途：
 *   - 监控系统运行状态
 *   - 提供心跳检测接口
 *   - 用于负载均衡器和容器编排工具的健康检查
 *
 */
@Tag(name = "Health Check", description = "Health check API")
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    /**
     * 健康检查接口
     * 返回应用程序的运行状态和基本信息
     *
     * 响应数据包含：
     *   - status: 应用状态（UP表示正常运行）
     *   - timestamp: 当前服务器时间
     *   - application: 应用程序名称
     *   - version: 应用程序版本
 *
     * @return 包含健康状态信息的统一响应对象
     */
    @Operation(summary = "Health check")
    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "UP");
        data.put("timestamp", LocalDateTime.now());
        data.put("application", "Shopping Mall Backend");
        data.put("version", "1.0.0");
        return Result.success(data);
    }
}
