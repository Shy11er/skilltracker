package net.brekker.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка, когда аккаунт с таким email уже зарегистрирован другим способом.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ProviderConflictException extends RuntimeException {
    public ProviderConflictException(String message) {
        super(message);
    }
}
