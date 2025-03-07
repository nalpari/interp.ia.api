package net.devgrr.interp.ia.api.config.issue;

import lombok.Getter;

@Getter
public enum Priority {
  EMERGENCY("Emergency"), // 긴급
  HIGH("High"), // 높음
  MEDIUM("Medium"), // 중간
  LOW("Low"); // 낮음

  private final String value;

  Priority(String value) {
    this.value = value;
  }
}
