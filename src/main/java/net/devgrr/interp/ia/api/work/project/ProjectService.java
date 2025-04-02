package net.devgrr.interp.ia.api.work.project;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.config.mapStruct.ProjectMapper;
import net.devgrr.interp.ia.api.member.MemberService;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.member.entity.QMember;
import net.devgrr.interp.ia.api.util.DateUtil;
import net.devgrr.interp.ia.api.work.history.HistoryService;
import net.devgrr.interp.ia.api.work.issue.entity.QIssue;
import net.devgrr.interp.ia.api.work.project.dto.ProjectRequest;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import net.devgrr.interp.ia.api.work.project.entity.QProject;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final JPAQueryFactory queryFactory;
  private final ProjectMapper projectMapper;
  private final MemberService memberService;
  private final HistoryService historyService;

  private final QProject qProject = QProject.project;
  private final QMember qMember = QMember.member;
  private final QIssue qIssue = QIssue.issue;

  public List<Project> getProjectsByKeywords(
      IssueStatus status,
      Priority priority,
      String title,
      String subTitle,
      Long creatorId,
      List<Long> assigneeId,
      LocalDate createdDateFrom,
      LocalDate createdDateTo,
      LocalDate updatedDateFrom,
      LocalDate updatedDateTo,
      LocalDate dueDateFrom,
      LocalDate dueDateTo,
      LocalDate startDateFrom,
      LocalDate startDateTo,
      LocalDate endDateFrom,
      LocalDate endDateTo,
      Set<String> tag)
      throws BaseException {
    try {
      return queryFactory
          .selectFrom(qProject)
          .innerJoin(qProject.creator, qMember)
          .leftJoin(qProject.subIssues, qIssue)
          .fetchJoin()
          .where(
              qProject.isDeleted.isFalse(),
              status != null ? qProject.status.eq(status) : null,
              priority != null ? qProject.priority.eq(priority) : null,
              StringUtils.hasText(title) ? qProject.title.like("%" + title + "%") : null,
              StringUtils.hasText(subTitle) ? qProject.subTitle.like("%" + subTitle + "%") : null,
              creatorId != null && creatorId > 0 ? qProject.creator.id.eq(creatorId) : null,
              assigneeId != null && !assigneeId.isEmpty()
                  ? qProject.assignee.any().id.in(assigneeId)
                  : null,
              DateUtil.isValidDateRange(createdDateFrom, createdDateTo)
                  ? qProject.createdDate.between(
                      createdDateFrom.atStartOfDay(), createdDateTo.atStartOfDay())
                  : null,
              DateUtil.isValidDateRange(updatedDateFrom, updatedDateTo)
                  ? qProject.updatedDate.between(
                      updatedDateFrom.atStartOfDay(), updatedDateTo.atStartOfDay())
                  : null,
              DateUtil.isValidDateRange(dueDateFrom, dueDateTo)
                  ? qProject.dueDate.between(dueDateFrom, dueDateTo)
                  : null,
              DateUtil.isValidDateRange(startDateFrom, startDateTo)
                  ? qProject.startDate.between(startDateFrom, startDateTo)
                  : null,
              DateUtil.isValidDateRange(endDateFrom, endDateTo)
                  ? qProject.endDate.between(endDateFrom, endDateTo)
                  : null,
              tag != null && !tag.isEmpty() ? qProject.tag.any().in(tag) : null,
              qIssue.parentIssue.isNull())
          .orderBy(
              qProject.createdDate.asc(), qProject.creator.name.asc(), qIssue.createdDate.asc())
          .fetch();

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public Project getProjectsById(Long id) throws BaseException {
    Project project =
        queryFactory
            .selectFrom(qProject)
            .innerJoin(qProject.creator, qMember)
            .leftJoin(qProject.subIssues, qIssue)
            .fetchJoin()
            .where(qProject.isDeleted.isFalse(), qProject.id.eq(id), qIssue.parentIssue.isNull())
            .orderBy(qProject.creator.name.asc(), qIssue.createdDate.asc())
            .fetchOne();
    if (project == null) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 프로젝트입니다. (id: " + id + ")");
    }
    return project;
  }

  @Transactional
  public Project setProjects(ProjectRequest req, String username) throws BaseException {
    try {
      Member creator = memberService.getUsersByEmail(username);
      Set<Member> assignee = null;
      if (req.assigneeId() != null && !req.assigneeId().isEmpty()) {
        assignee = memberService.getUsersByIds(req.assigneeId());
      }
      return projectRepository.save(projectMapper.toProject(req, creator, assignee));
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Transactional
  public void putProjectsById(Long id, Map<String, Object> req, UserDetails userDetails)
      throws BaseException {
    Project originProject = getProjectsById(id);
    try {
      String key = req.keySet().iterator().next();
      Object value = req.values().iterator().next();
      Member modifier = memberService.getUsersByEmail(userDetails.getUsername());
      String beforeValue;
      String afterValue;

      switch (key) {
        case "status":
          IssueStatus newStatus = IssueStatus.valueOf(value.toString());
          beforeValue = Objects.toString(originProject.getStatus(), null);
          afterValue = newStatus.toString();
          projectRepository.save(projectMapper.putProjectStatus(originProject, 0, newStatus));
          break;
        case "priority":
          Priority newPriority = Priority.valueOf(value.toString());
          beforeValue = Objects.toString(originProject.getPriority(), null);
          afterValue = newPriority.toString();
          projectRepository.save(projectMapper.putProjectPriority(originProject, 0, newPriority));
          break;
        case "title":
          beforeValue = originProject.getTitle();
          afterValue = value.toString();
          projectRepository.save(projectMapper.putProjectTitle(originProject, value.toString()));
          break;
        case "subTitle":
          String newSubTitle = Objects.toString(value, null);
          beforeValue = originProject.getSubTitle();
          afterValue = newSubTitle;
          projectRepository.save(projectMapper.putProjectSubTitle(originProject, 0, newSubTitle));
          break;
        case "assigneeId":
          String originAssignee =
              !originProject.getAssignee().isEmpty()
                  ? originProject.getAssignee().stream().map(Member::getId).toList().toString()
                  : null;
          Set<Member> newAssignee =
              value != null
                  ? memberService.getUsersByIds(new HashSet<>((List<Integer>) value))
                  : null;
          beforeValue = originAssignee;
          afterValue =
              newAssignee != null
                  ? newAssignee.stream().map(Member::getId).toList().toString()
                  : null;
          projectRepository.save(projectMapper.putProjectAssignee(originProject, 0, newAssignee));
          break;
        case "dueDate":
        case "startDate":
        case "endDate":
          String originDate =
              key.equals("dueDate")
                  ? Objects.toString(originProject.getDueDate(), null)
                  : key.equals("startDate")
                      ? Objects.toString(originProject.getStartDate(), null)
                      : Objects.toString(originProject.getEndDate(), null);
          LocalDate newDate =
              value != null
                  ? LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                  : null;
          beforeValue = originDate;
          afterValue = newDate != null ? newDate.toString() : null;
          projectRepository.save(updateProjectDateField(originProject, key, newDate));
          break;
        case "description":
          String newDescription = Objects.toString(value, null);
          beforeValue = originProject.getDescription();
          afterValue = newDescription;
          projectRepository.save(
              projectMapper.putProjectDescription(originProject, 0, newDescription));
          break;
        case "tag":
          Set<String> newTag = value != null ? new HashSet<>((List<String>) value) : null;
          beforeValue =
              !originProject.getTag().isEmpty() ? originProject.getTag().toString() : null;
          afterValue = Objects.toString(newTag, null);
          projectRepository.save(projectMapper.putProjectTag(originProject, 0, newTag));
          break;
        default:
          throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
      }

      historyService.setHistory(
          IssueCategory.PROJECT, id, beforeValue, afterValue, key, modifier);

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private Project updateProjectDateField(Project project, String key, LocalDate date)
      throws BaseException {
    return switch (key) {
      case "dueDate" -> projectMapper.putProjectDueDate(project, 0, date);
      case "startDate" -> projectMapper.putProjectStartDate(project, 0, date);
      case "endDate" -> projectMapper.putProjectEndDate(project, 0, date);
      default -> throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "날짜 필드가 아닙니다.");
    };
  }

  @Transactional
  public void putProjectsDeletedFlagById(Long id, Boolean flag) throws BaseException {
    Project project =
        projectRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new BaseException(
                        ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 프로젝트입니다. (id: " + id + ")"));
    try {
      queryFactory
          .update(qIssue)
          .set(qIssue.isDeleted, flag)
          .set(qIssue.updatedDate, LocalDateTime.now())
          .where(qIssue.parentProject.id.eq(id))
          .execute();
      projectMapper.putProjectDeletedFlag(project, flag);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
