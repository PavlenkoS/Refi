package com.example.Refi.entity;

import lombok.Getter;

@Getter
public enum DealFieldTypeEnum {
    TOF("TOF"), CUSTOM("Custom"), CUSTOM_CALCULATED("Custom Calculated");

    private final String label;

    DealFieldTypeEnum(final String label) {
        this.label = label;
    }
}
