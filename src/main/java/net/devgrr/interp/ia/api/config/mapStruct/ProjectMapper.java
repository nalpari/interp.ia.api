package net.devgrr.interp.ia.api.config.mapStruct;

import java.time.LocalDate;
import java.util.Set;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.work.project.dto.ProjectRequest;
import net.devgrr.interp.ia.api.work.project.dto.ProjectResponse;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

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

  /*
   * 참고
   * - null 데이터도 그대로 업데이트 하기 위해 dummy 파라미터 추가
   * - List 유형을 파라미터로 넘겼을 때 아래와 같은 에러를 막기 위해 dummy 파라미터 추가
   *      ERROR: Can't generate mapping method from iterable type from java stdlib to non-iterable type.
   * */

  @Mapping(target = "status", source = "status")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectStatus(@MappingTarget Project project, Integer dummy, IssueStatus status);

  @Mapping(target = "priority", source = "priority")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectPriority(@MappingTarget Project project, Integer dummy, Priority priority);

  @Mapping(target = "title", source = "title")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectTitle(@MappingTarget Project project, String title);

  @Mapping(target = "subTitle", source = "subTitle")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectSubTitle(@MappingTarget Project project, Integer dummy, String subTitle);

  @Mapping(target = "dueDate", source = "dueDate")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectDueDate(@MappingTarget Project project, Integer dummy, LocalDate dueDate);

  @Mapping(target = "startDate", source = "startDate")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectStartDate(@MappingTarget Project project, Integer dummy, LocalDate startDate);

  @Mapping(target = "endDate", source = "endDate")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectEndDate(@MappingTarget Project project, Integer dummy, LocalDate endDate);

  @Mapping(target = "description", source = "description")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectDescription(@MappingTarget Project project, Integer dummy, String description);

  @Mapping(target = "assignee", source = "assignee")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectAssignee(@MappingTarget Project project, Integer dummy, Set<Member> assignee);

  @Mapping(target = "tag", source = "tag")
  @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
  Project putProjectTag(@MappingTarget Project project, Integer dummy, Set<String> tag);
}
