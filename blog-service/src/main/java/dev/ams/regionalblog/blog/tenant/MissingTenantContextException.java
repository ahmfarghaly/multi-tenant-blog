package dev.ams.regionalblog.blog.tenant;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class MissingTenantContextException extends RuntimeException {
    public MissingTenantContextException(String message) {
        super(message);
    }
}
