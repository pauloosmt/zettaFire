package com.br.zetta.fire.data.entity.enums;

import lombok.Getter;

@Getter
public enum StatusAlert {
    SENT("SENT"),
    FAILED("FAILED"),
    PENDING("PENDING");

    private final String status;

    StatusAlert(String status) {
        this.status = status;
    }

}
