package com.br.zetta.fire.data.entity.enums;

import lombok.Getter;

@Getter
public enum StatusFire {
    ACTIVE("ACTIVE"),
    CONTROLLED("controlled"),
    FINISHED("finished");

    private final String statusFire;

    StatusFire(String statusFire) {
        this.statusFire = statusFire;
    }

    public String getStatusFire() {
        return statusFire;
    }

}
