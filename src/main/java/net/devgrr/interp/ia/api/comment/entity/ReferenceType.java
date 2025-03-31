package net.devgrr.interp.ia.api.comment.entity;

import lombok.Getter;

@Getter
public enum ReferenceType {
    ISSUE("issue"),
    PROJECT("project");

    private final String value;

    ReferenceType(String value) {
        this.value = value;
    }
}
