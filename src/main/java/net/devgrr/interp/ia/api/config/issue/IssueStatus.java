package net.devgrr.interp.ia.api.config.issue;

import lombok.Getter;

@Getter
public enum IssueStatus {
  TODO("ToDo"),
  IN_PROGRESS("In Progress"),
  DONE("Done"),
  CANCELED("Canceled"),
  ANALYSIS("Analysis"),
  UNPRODUCIBLE("Unproducible");

  private final String value;

  IssueStatus(String value) {
    this.value = value;
  }
}
