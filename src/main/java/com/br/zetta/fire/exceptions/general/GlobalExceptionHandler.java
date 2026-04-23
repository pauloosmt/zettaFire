package com.br.zetta.fire.exceptions.general;

import ch.qos.logback.classic.Logger;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.br.zetta.fire.exceptions.custom.AlertProcessingException;
import com.br.zetta.fire.exceptions.message.RestErrorMessage;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Order(1)
public class GlobalExceptionHandler {

    private static final Logger logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<RestErrorMessage> handleJwtError(JWTVerificationException ex) {
        RestErrorMessage error = new RestErrorMessage(HttpStatus.FORBIDDEN, ex.getMessage());
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<RestErrorMessage>> handleValidation (MethodArgumentNotValidException ex) {
        List<RestErrorMessage> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(err -> new RestErrorMessage(HttpStatus.BAD_REQUEST, err.getField() + ": " + err.getDefaultMessage()))
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<RestErrorMessage> handleNotFound(EntityNotFoundException ex) {
        RestErrorMessage error = new RestErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(AlertProcessingException.class)
    public ResponseEntity<RestErrorMessage> handleAlertError(AlertProcessingException ex) {
        RestErrorMessage error = new RestErrorMessage(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<RestErrorMessage> handleDataBaseError(DataAccessException ex) {
        RestErrorMessage error = new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());

        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(MailException.class)
    public ResponseEntity<RestErrorMessage> handleMailFailure(MailException ex) {
        logger.error("Falha no serviço de e-mail: ", ex);
        RestErrorMessage error = new RestErrorMessage(
                HttpStatus.SERVICE_UNAVAILABLE,
                "O serviço de e-mail está temporariamente indisponível."
        );
        return ResponseEntity.status(error.status()).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RestErrorMessage> handleConflict(DataIntegrityViolationException ex) {
        RestErrorMessage error = new RestErrorMessage(HttpStatus.CONFLICT, ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RestErrorMessage> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Tentativa de login com credenciais inválidas.");

        // Criamos a mensagem padronizada usando seu DTO
        RestErrorMessage error = new RestErrorMessage(
                HttpStatus.UNAUTHORIZED,
                "E-mail ou senha incorretos."
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

}
