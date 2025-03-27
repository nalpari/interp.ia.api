package net.devgrr.interp.ia.api.config.mapStruct;

import java.util.List;
import java.util.Set;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.IssueType;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.work.issue.dto.IssueRequest;
import net.devgrr.interp.ia.api.work.issue.dto.IssueResponse;
import net.devgrr.interp.ia.api.work.issue.entity.Issue;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface IssueMapper {

  @Named("toIssueType")
  static IssueType toIssueType(String type) {
    return IssueType.valueOf(type);
  }

  @Named("toIssueStatus")
  static IssueStatus toIssueStatus(String status) {
    return IssueStatus.valueOf(status);
  }

  @Named("toIssuePriority")
  static Priority toIssuePriority(String priority) {
    return Priority.valueOf(priority);
  }

  IssueResponse toResponse(Issue issue);

  @Mapping(target = "subIssues", source = "subIssues")
  void mapSubIssues(@MappingTarget Issue issue, Integer dummy, List<Issue> subIssues);

  @Mapping(target = "subIssues", source = "subIssues")
  @Mapping(target = "relatedIssues", source = "relatedIssue")
  Issue mapInvolvedIssues(
      @MappingTarget Issue issue, List<Issue> subIssues, List<Issue> relatedIssue);

  @Mapping(target = "creator", source = "creator")
  @Mapping(target = "assignee", source = "assignee")
  @Mapping(target = "parentProject", source = "parentProject")
  @Mapping(target = "parentIssue", source = "parentIssue")
  @Mapping(target = "relatedIssues", source = "relatedIssues")
  @BeanMapping(ignoreByDefault = true)
  Issue mappingIssue(
      @MappingTarget Issue issue,
      Member creator,
      Set<Member> assignee,
      Project parentProject,
      Issue parentIssue,
      Set<Issue> relatedIssues);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "type", source = "req.type", qualifiedByName = "toIssueType")
  @Mapping(target = "status", source = "req.status", qualifiedByName = "toIssueStatus")
  @Mapping(target = "priority", source = "req.priority", qualifiedByName = "toIssuePriority")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue toIssue(IssueRequest req);
}
