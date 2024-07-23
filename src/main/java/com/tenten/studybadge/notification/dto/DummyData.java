package com.tenten.studybadge.notification.dto;

import lombok.Getter;

@Getter
public class DummyData {
    private String message;
    private Boolean isDummy;

    public DummyData(String message) {
        this.message = message;
        this.isDummy = true;
    }
}
