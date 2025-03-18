package net.devgrr.interp.ia.api.work.project;

import com.querydsl.core.BooleanBuilder;
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
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.config.mapStruct.ProjectMapper;
import net.devgrr.interp.ia.api.member.MemberService;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.work.history.HistoryService;
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

  public List<Project> getProjects() {
    return projectRepository.findAll();
  }

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
      BooleanBuilder builder = new BooleanBuilder();

      if (status != null) {
        builder.and(qProject.status.eq(status));
      }
      if (priority != null) {
        builder.and(qProject.priority.eq(priority));
      }
      if (StringUtils.hasText(title)) {
        builder.and(qProject.title.like("%" + title + "%"));
      }
      if (StringUtils.hasText(subTitle)) {
        builder.and(qProject.subTitle.like("%" + subTitle + "%"));
      }
      if (creatorId != null && creatorId > 0) {
        builder.and(qProject.creator.id.eq(creatorId));
      }
      if (assigneeId != null && !assigneeId.isEmpty()) {
        builder.and(qProject.assignee.any().id.in(assigneeId));
      }
      if (isValidDateRange(createdDateFrom, createdDateTo)) {
        builder.and(
            qProject.createdDate.between(
                createdDateFrom.atStartOfDay(), createdDateTo.atStartOfDay()));
      }
      if (isValidDateRange(updatedDateFrom, updatedDateTo)) {
        builder.and(
            qProject.updatedDate.between(
                updatedDateFrom.atStartOfDay(), updatedDateTo.atStartOfDay()));
      }
      if (isValidDateRange(dueDateFrom, dueDateTo)) {
        builder.and(qProject.dueDate.between(dueDateFrom.atStartOfDay(), dueDateTo.atStartOfDay()));
      }
      if (isValidDateRange(startDateFrom, startDateTo)) {
        builder.and(
            qProject.startDate.between(startDateFrom.atStartOfDay(), startDateTo.atStartOfDay()));
      }
      if (isValidDateRange(endDateFrom, endDateTo)) {
        builder.and(qProject.endDate.between(endDateFrom.atStartOfDay(), endDateTo.atStartOfDay()));
      }
      if (tag != null && !tag.isEmpty()) {
        builder.and(qProject.tag.any().in(tag));
      }

      return builder.hasValue()
          ? queryFactory.selectFrom(qProject).where(builder).fetch()
          : getProjects();

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private boolean isValidDateRange(LocalDate dateFrom, LocalDate dateTo) {
    return dateTo != null
        && dateFrom != null
        && !dateTo.equals(LocalDate.MIN)
        && !dateFrom.equals(LocalDate.MIN)
        && !dateFrom.isAfter(dateTo);
  }

  public Project getProjectsById(Long id) {
    return projectRepository.findById(id).orElse(null);
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
    try {

      Project originProject = getProjectsById(id);
      if (originProject == null) {
        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 프로젝트입니다.");
      }

      String key = req.keySet().iterator().next();
      Object value = req.values().iterator().next();
      Member modifier = memberService.getUsersByEmail(userDetails.getUsername());

      switch (key) {
        case "status":
          IssueStatus newStatus = IssueStatus.valueOf(value.toString());
          historyService.setHistory(
              id,
              Objects.toString(originProject.getStatus(), null),
              newStatus.toString(),
              key,
              modifier);
          projectRepository.save(projectMapper.putProjectStatus(originProject, 0, newStatus));
          break;
        case "priority":
          Priority newPriority = Priority.valueOf(value.toString());
          historyService.setHistory(
              id,
              Objects.toString(originProject.getPriority(), null),
              newPriority.toString(),
              key,
              modifier);
          projectRepository.save(projectMapper.putProjectPriority(originProject, 0, newPriority));
          break;
        case "title":
          historyService.setHistory(id, originProject.getTitle(), value.toString(), key, modifier);
          projectRepository.save(projectMapper.putProjectTitle(originProject, 0, value.toString()));
          break;
        case "subTitle":
          String newSubTitle = Objects.toString(value, null);
          historyService.setHistory(id, originProject.getSubTitle(), newSubTitle, key, modifier);
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
          historyService.setHistory(
              id,
              originAssignee,
              newAssignee != null
                  ? newAssignee.stream().map(Member::getId).toList().toString()
                  : null,
              key,
              modifier);
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
          LocalDateTime newDate =
              value != null
                  ? LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                      .atStartOfDay()
                  : null;
          historyService.setHistory(
              id, originDate, newDate != null ? newDate.toString() : null, key, modifier);
          projectRepository.save(updateProjectDateField(originProject, key, newDate));
          break;
        case "description":
          String newDescription = Objects.toString(value, null);
          historyService.setHistory(
              id, originProject.getDescription(), newDescription, key, modifier);
          projectRepository.save(
              projectMapper.putProjectDescription(originProject, 0, newDescription));
          break;
        case "tag":
          Set<String> newTag = value != null ? new HashSet<>((List<String>) value) : null;
          historyService.setHistory(
              id,
              !originProject.getTag().isEmpty() ? originProject.getTag().toString() : null,
              Objects.toString(newTag, null),
              key,
              modifier);
          projectRepository.save(projectMapper.putProjectTag(originProject, 0, newTag));
          break;
        default:
          throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
      }

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private Project updateProjectDateField(Project project, String key, LocalDateTime date)
      throws BaseException {
    return switch (key) {
      case "dueDate" -> projectMapper.putProjectDueDate(project, 0, date);
      case "startDate" -> projectMapper.putProjectStartDate(project, 0, date);
      case "endDate" -> projectMapper.putProjectEndDate(project, 0, date);
      default -> throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "날짜 필드가 아닙니다.");
    };
  }

  @Transactional
  public void delProjectsById(Long id) throws BaseException {
    try {
      projectRepository.deleteById(id);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
