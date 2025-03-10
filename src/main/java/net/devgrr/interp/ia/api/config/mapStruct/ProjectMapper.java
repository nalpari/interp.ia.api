package net.devgrr.interp.ia.api.config.mapStruct;

import java.time.LocalDateTime;
import java.util.Set;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.work.project.dto.ProjectRequest;
import net.devgrr.interp.ia.api.work.project.dto.ProjectResponse;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

  @Named("toIssueStatus")
  static IssueStatus toIssueStatus(String status) {
    return IssueStatus.valueOf(status);
  }

  @Named("toIssuePriority")
  static Priority toIssuePriority(String priority) {
    return Priority.valueOf(priority);
  }

  @Named("toDate")
  static LocalDateTime toDate(LocalDateTime date) {
    return date.equals(LocalDateTime.MIN) ? null : date;
  }

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "subIssues", ignore = true)
  @Mapping(
      target = "type",
      expression = "java(net.devgrr.interp.ia.api.config.issue.IssueType.PROJECT)")
  @Mapping(target = "status", source = "req.status", qualifiedByName = "toIssueStatus")
  @Mapping(target = "priority", source = "req.priority", qualifiedByName = "toIssuePriority")
  @Mapping(target = "creator", source = "creator")
  @Mapping(target = "assignee", source = "assignee")
  Project toProject(ProjectRequest req, Member creator, Set<Member> assignee);

  ProjectResponse toResponse(Project project);

  @Mapping(target = "status", source = "status")
  Project putProjectStatus(@MappingTarget Project project, IssueStatus status);

  @Mapping(target = "priority", source = "priority")
  Project putProjectPriority(@MappingTarget Project project, Priority priority);

  @Mapping(target = "title", source = "title")
  Project putProjectTitle(@MappingTarget Project project, String title);

  @Mapping(target = "subTitle", source = "subTitle")
  Project putProjectSubTitle(@MappingTarget Project project, String subTitle);

  @Mapping(target = "dueDate", source = "dueDate", qualifiedByName = "toDate")
  Project putProjectDueDate(@MappingTarget Project project, LocalDateTime dueDate);

  @Mapping(target = "startDate", source = "startDate", qualifiedByName = "toDate")
  Project putProjectStartDate(@MappingTarget Project project, LocalDateTime startDate);

  @Mapping(target = "endDate", source = "endDate", qualifiedByName = "toDate")
  Project putProjectEndDate(@MappingTarget Project project, LocalDateTime endDate);

  @Mapping(target = "description", source = "description")
  Project putProjectDescription(@MappingTarget Project project, String description);

  /*
   * 참고
   * List 유형을 파라미터로 넘겼을 때 아래와 같은 에러를 막기 위해 dummy 파라미터를 추가함
   * ERROR: Can't generate mapping method from iterable type from java stdlib to non-iterable type.
   * */
  @Mapping(target = "assignee", source = "assignee")
  Project putProjectAssignee(@MappingTarget Project project, Integer dummy, Set<Member> assignee);

  @Mapping(target = "tag", source = "tag")
  Project putProjectTag(@MappingTarget Project project, Integer dummy, Set<String> tag);
}
