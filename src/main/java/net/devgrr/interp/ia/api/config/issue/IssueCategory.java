package net.devgrr.interp.ia.api.config.issue;

import lombok.Getter;

@Getter
public enum IssueCategory {
  PROJECT("Project"),
  ISSUE("Issue");

  private final String value;

  IssueCategory(String value) {
    this.value = value;
  }
}
