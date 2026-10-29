package dev.ams.regionalblog.gateway.routing;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegionalProxyController {

    private final RegionalProxyService proxyService;

    public RegionalProxyController(RegionalProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @RequestMapping({"/api/posts", "/api/posts/**", "/internal/routing-info"})
    public ResponseEntity<byte[]> proxyRequest(HttpServletRequest request) {
        return proxyService.forwardRequest(request);
    }
}
