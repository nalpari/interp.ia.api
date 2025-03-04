package net.devgrr.interp.ia.api.config.issue;

import lombok.Getter;

@Getter
public enum IssueType {
  PROJECT("Project"),
  EPIC("Epic"),
  STORY("Story"),
  TASK("Task"),
  BUG("Bug"),
  SUB_TASK("Sub Task");

  private final String value;

  IssueType(String value) {
    this.value = value;
  }
}
