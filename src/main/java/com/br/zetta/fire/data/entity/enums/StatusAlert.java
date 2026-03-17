package com.br.zetta.fire.data.entity.enums;

public enum StatusAlert {
    SENT("SENT"),
    FAILED("FAILED"),
    PENDING("PENDING");

    private final String status;

    StatusAlert(String status) {
        this.status = status;
    }

    public String getStatusAlert() {
        return status;
    }

}
