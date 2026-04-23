package com.br.zetta.fire.exceptions.custom;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class AlertProcessingException extends RuntimeException {

    public AlertProcessingException(String message) {
        super(message);
    }
}