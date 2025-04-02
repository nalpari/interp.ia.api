package net.devgrr.interp.ia.api.config.mapStruct;

import java.time.LocalDate;
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
  @Mapping(target = "isDeleted", constant = "false")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue toIssue(IssueRequest req);

  /*
   * 참고
   * - null 데이터도 그대로 업데이트 하기 위해 dummy 파라미터 추가
   * - List 유형을 파라미터로 넘겼을 때 아래와 같은 에러를 막기 위해 dummy 파라미터 추가
   *      ERROR: Can't generate mapping method from iterable type from java stdlib to non-iterable type.
   * */

  @Mapping(target = "type", source = "type")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueType(@MappingTarget Issue issue, Integer dummy, IssueType type);

  @Mapping(target = "status", source = "status")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueStatus(@MappingTarget Issue issue, Integer dummy, IssueStatus status);

  @Mapping(target = "priority", source = "priority")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssuePriority(@MappingTarget Issue issue, Integer dummy, Priority priority);

  @Mapping(target = "title", source = "title")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueTitle(@MappingTarget Issue issue, String title);

  @Mapping(target = "subTitle", source = "subTitle")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueSubTitle(@MappingTarget Issue issue, Integer dummy, String subTitle);

  @Mapping(target = "dueDate", source = "dueDate")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueDueDate(@MappingTarget Issue issue, Integer dummy, LocalDate dueDate);

  @Mapping(target = "startDate", source = "startDate")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueStartDate(@MappingTarget Issue issue, Integer dummy, LocalDate startDate);

  @Mapping(target = "endDate", source = "endDate")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueEndDate(@MappingTarget Issue issue, Integer dummy, LocalDate endDate);

  @Mapping(target = "description", source = "description")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueDescription(@MappingTarget Issue issue, Integer dummy, String description);

  @Mapping(target = "assignee", source = "assignee")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueAssignee(@MappingTarget Issue issue, Integer dummy, Set<Member> assignee);

  @Mapping(target = "tag", source = "tag")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueTag(@MappingTarget Issue issue, Integer dummy, Set<String> tag);

  @Mapping(target = "relatedIssues", source = "relatedIssues")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Issue putIssueRelatedIssues(@MappingTarget Issue issue, Integer dummy, Set<Issue> relatedIssues);
}
