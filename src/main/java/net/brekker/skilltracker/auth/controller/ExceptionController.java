    package net.brekker.skilltracker.auth.controller;

    import com.fasterxml.jackson.databind.exc.InvalidFormatException;
    import jakarta.persistence.EntityNotFoundException;
    import jakarta.validation.ConstraintViolationException;
    import net.brekker.skilltracker.auth.dto.ExceptionResponseDto;
    import net.brekker.skilltracker.auth.dto.ValidationErrorResponseDto;
    import net.brekker.skilltracker.auth.dto.Violation;
    import net.brekker.skilltracker.common.exceptions.RateLimitException;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.converter.HttpMessageNotReadableException;
    import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
    import org.springframework.web.bind.MethodArgumentNotValidException;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.ResponseStatus;
    import org.springframework.web.bind.annotation.RestControllerAdvice;
    import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

    import javax.naming.AuthenticationException;
    import java.nio.file.AccessDeniedException;
    import java.util.stream.Collectors;

    @RestControllerAdvice
    public class ExceptionController {
        private static final Logger logger = LoggerFactory.getLogger(ExceptionController.class);

        /* 400 */
        @ExceptionHandler(ConstraintViolationException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ValidationErrorResponseDto handleConstraintViolationException(
                ConstraintViolationException exception) {
            return new ValidationErrorResponseDto(
                    exception.getConstraintViolations().stream()
                            .map(
                                    violation ->
                                            Violation.builder()
                                                    .fieldName(violation.getPropertyPath().toString())
                                                    .message(violation.getMessage())
                                                    .build())
                            .collect(Collectors.toList()));
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ValidationErrorResponseDto handleMethodArgumentNotValidException(
                MethodArgumentNotValidException exception) {
            return new ValidationErrorResponseDto(
                    exception.getBindingResult().getFieldErrors().stream()
                            .map(
                                    error ->
                                            Violation.builder()
                                                    .fieldName(error.getField())
                                                    .message(error.getDefaultMessage())
                                                    .build())
                            .collect(Collectors.toList()));
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ExceptionResponseDto handleInvalidDateFormat(HttpMessageNotReadableException exception) {
            if (exception.getCause() instanceof InvalidFormatException cause) {
                return ExceptionResponseDto.builder()
                        .message(
                                String.format("%s: Failed to parse date", cause.getPath().getFirst().getFieldName()))
                        .build();
            }

            return getExceptionResponseDto(exception);
        }

        @ExceptionHandler({
                IllegalArgumentException.class,
                MethodArgumentTypeMismatchException.class,
        })
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public ExceptionResponseDto handleBadRequest(Exception exception) {
            return getExceptionResponseDto(exception);
        }

        /* 401 */
        @ExceptionHandler(AuthenticationException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public ExceptionResponseDto handleAuthenticationException(AuthenticationException exception) {
            return getExceptionResponseDto(exception);
        }

        @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public ExceptionResponseDto handleGoneException(AuthenticationCredentialsNotFoundException exception) {
            return getExceptionResponseDto(exception);
        }

        /* 403 */
        @ExceptionHandler(AccessDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public ExceptionResponseDto handleAccessDeniedException(AccessDeniedException exception) {
            return getExceptionResponseDto(exception);
        }

        /* 404 */
        @ExceptionHandler({
                EntityNotFoundException.class,
        })
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public ExceptionResponseDto handleNotFoundException(Exception exception) {
            return getExceptionResponseDto(exception);
        }

        /* 429 */
        @ExceptionHandler({
                RateLimitException.class,
        })
        @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
        public ExceptionResponseDto handleRateLimitException(RateLimitException exception) {
            return getExceptionResponseDto(exception);
        }

        /* 500 */
        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        public ExceptionResponseDto handleGeneralException(Exception exception) {
            ExceptionResponseDto response = getExceptionResponseDto(exception);
            response.setMessage("Возникла ошибка во время обработки запроса.");
            return response;
        }

        private ExceptionResponseDto getExceptionResponseDto(Exception exception) {
            logger.error(exception.getMessage(), exception);
            ExceptionResponseDto response = new ExceptionResponseDto();
            response.setMessage(exception.getMessage());
            return response;
        }
    }
