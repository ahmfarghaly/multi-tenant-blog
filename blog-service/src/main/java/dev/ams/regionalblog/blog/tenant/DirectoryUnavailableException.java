package dev.ams.regionalblog.blog.tenant;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class DirectoryUnavailableException extends RuntimeException {
    public DirectoryUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
