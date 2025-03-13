package net.devgrr.interp.ia.api.work.project;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.querydsl.jpa.impl.JPAUpdateClause;
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
import net.devgrr.interp.ia.api.work.history.entity.History;
import net.devgrr.interp.ia.api.work.history.entity.History.HistoryBuilder;
import net.devgrr.interp.ia.api.work.project.dto.ProjectRequest;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import net.devgrr.interp.ia.api.work.project.entity.QProject;
import net.devgrr.interp.ia.api.work.project.entity.QProjectAssignee;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  private final QProjectAssignee qProjectAssignee = QProjectAssignee.projectAssignee;

  public List<Project> getProjects() {
    return projectRepository.findAll();
  }

  public List<Project> getProjectsByKeywords(
      String status,
      String priority,
      String title,
      String subTitle,
      Long creatorId,
      List<Long> assigneeId,
      LocalDate createdDate,
      LocalDate updatedDate,
      LocalDate dueDate,
      LocalDate startDate,
      LocalDate endDate,
      Set<String> tag)
      throws BaseException {
    try {
      BooleanBuilder builder = new BooleanBuilder();

      if (status != null && !status.trim().isEmpty()) {
        builder.and(qProject.status.eq(IssueStatus.valueOf(status)));
      }
      if (priority != null && !priority.trim().isEmpty()) {
        builder.and(qProject.priority.eq(Priority.valueOf(priority)));
      }
      if (title != null && !title.trim().isEmpty()) {
        builder.and(qProject.title.like("%" + title + "%"));
      }
      if (subTitle != null && !subTitle.trim().isEmpty()) {
        builder.and(qProject.subTitle.like("%" + subTitle + "%"));
      }
      if (creatorId != null && creatorId > 0) {
        builder.and(qProject.creator.id.eq(creatorId));
      }
      if (assigneeId != null && !assigneeId.isEmpty()) {
        builder.and(qProject.assignee.any().id.in(assigneeId));
      }
      if (createdDate != null && !createdDate.equals(LocalDate.MIN)) {
        builder.and(qProject.createdDate.eq(createdDate.atStartOfDay()));
      }
      if (updatedDate != null && !updatedDate.equals(LocalDate.MIN)) {
        builder.and(qProject.updatedDate.eq(updatedDate.atStartOfDay()));
      }
      if (dueDate != null && !dueDate.equals(LocalDate.MIN)) {
        builder.and(qProject.dueDate.eq(dueDate.atStartOfDay()));
      }
      if (startDate != null && !startDate.equals(LocalDate.MIN)) {
        builder.and(qProject.startDate.eq(startDate.atStartOfDay()));
      }
      if (endDate != null && !endDate.equals(LocalDate.MIN)) {
        builder.and(qProject.endDate.eq(endDate.atStartOfDay()));
      }
      if (tag != null && !tag.isEmpty()) {
        builder.and(qProject.tag.contains((Expression<String>) tag));
      }

      return builder.hasValue()
          ? queryFactory.selectFrom(qProject).where(builder).fetch()
          : getProjects();

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
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
  public void updateProjectsById(Long id, Map<String, Object> req, UserDetails userDetails)
      throws BaseException {
    try {
      Project originProject = getProjectsById(id);
      if (originProject == null) {
        throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 프로젝트입니다.");
      }

      String key = req.keySet().iterator().next();
      Object value = req.values().iterator().next();
      Member modifier = memberService.getUsersByEmail(userDetails.getUsername());

      JPAUpdateClause updateProject =
          queryFactory
              .update(qProject)
              .set(qProject.updatedDate, LocalDateTime.now())
              .where(qProject.id.eq(id));

      switch (key) {
        case "status":
          IssueStatus newStatus = IssueStatus.valueOf(value.toString());
          updateProject.set(qProject.status, newStatus).execute();
          historyService.setHistory(
              id,
              Objects.toString(originProject.getStatus(), null),
              newStatus.toString(),
              key,
              modifier);
          break;
        case "priority":
          Priority newPriority = Priority.valueOf(value.toString());
          updateProject.set(qProject.priority, newPriority).execute();
          historyService.setHistory(
              id,
              Objects.toString(originProject.getPriority(), null),
              newPriority.toString(),
              key,
              modifier);
          break;
        case "title":
          updateProject.set(qProject.title, value.toString()).execute();
          historyService.setHistory(id, originProject.getTitle(), value.toString(), key, modifier);
          break;
        case "subTitle":
          String newSubTitle = Objects.toString(value, null);
          updateProject.set(qProject.subTitle, newSubTitle).execute();
          historyService.setHistory(id, originProject.getSubTitle(), newSubTitle, key, modifier);
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
          //          querydsl 에러로 인해 임시 주석 처리
          //          queryFactory
          //              .delete(qProjectAssignee)
          //              .where((qProjectAssignee.project.id.eq(id)))
          //              .execute();
          //          for (Member assignee : newAssignee) {
          //            queryFactory
          //                .insert(qProjectAssignee)
          //                .columns(qProjectAssignee.project.id, qProjectAssignee.member.id)
          //                .values(id, assignee.getId())
          //                .execute();
          //          }
          projectRepository.save(projectMapper.putProjectAssignee(originProject, 0, newAssignee));
          historyService.setHistory(
              id,
              originAssignee,
              newAssignee != null
                  ? newAssignee.stream().map(Member::getId).toList().toString()
                  : null,
              key,
              modifier);
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
          DateTimePath<LocalDateTime> updateTarget =
              key.equals("dueDate")
                  ? qProject.dueDate
                  : key.equals("startDate") ? qProject.startDate : qProject.endDate;
          updateProject.set(updateTarget, newDate).execute();
          historyService.setHistory(
              id, originDate, newDate != null ? newDate.toString() : null, key, modifier);
          break;
        case "description":
          String newDescription = Objects.toString(value, null);
          updateProject.set(qProject.description, newDescription);
          historyService.setHistory(
              id, originProject.getDescription(), newDescription, key, modifier);
          break;
        case "tag":
          Set<String> newTag = value != null ? new HashSet<>((List<String>) value) : null;
          updateProject.set(qProject.tag, newTag).execute();
          historyService.setHistory(
              id,
              Objects.toString(originProject.getTag(), null),
              Objects.toString(newTag, null),
              key,
              modifier);
          break;
        default:
          throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
      }

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Transactional
  public void putProjectsById(Long id, Map<String, Object> req, UserDetails userDetails)
      throws BaseException {
    Project originProject = getProjectsById(id);
    if (originProject == null) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 프로젝트입니다.");
    }

    try {
      HistoryBuilder history =
          History.builder()
              .issueId(id)
              .modifier(memberService.getUsersByEmail(userDetails.getUsername()));
      String key = req.keySet().iterator().next();
      Object value = req.values().iterator().next();
      Project saveProject = updateProjectField(originProject, key, value, history);

      /* MEMO: 이전 데이터와의 변경 여부는 Front 에서 처리 후 넘어오기 때문에 값 변경 validation 처리 생략하고 업데이트 */
      projectRepository.save(saveProject);
      //      historyService.setHistory(history.build());

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private Project updateProjectField(
      Project originProject, String key, Object value, HistoryBuilder history)
      throws BaseException {
    try {
      Project saveProject = null;
      switch (key) {
        case "status":
          String originStatus = Objects.toString(originProject.getStatus(), null);
          IssueStatus status = IssueStatus.valueOf(value.toString());
          saveProject = projectMapper.putProjectStatus(originProject, status);
          history.fieldName(key).beforeValue(originStatus).afterValue(status.toString());
          break;
        case "priority":
          String originPriority = Objects.toString(originProject.getPriority(), null);
          Priority priority = Priority.valueOf(value.toString());
          saveProject = projectMapper.putProjectPriority(originProject, priority);
          history.fieldName(key).beforeValue(originPriority).afterValue(priority.toString());
          break;
        case "title":
          String originTitle = originProject.getTitle();
          saveProject = projectMapper.putProjectTitle(originProject, value.toString());
          history.fieldName(key).beforeValue(originTitle).afterValue(value.toString());
          break;
        case "subTitle":
          String originSubTitle = Objects.toString(originProject.getSubTitle(), null);
          String subTitle = Objects.toString(value, null);
          saveProject = projectMapper.putProjectSubTitle(originProject, subTitle);
          history.fieldName(key).beforeValue(originSubTitle).afterValue(subTitle);
          break;
        case "assignee":
          String originAssignee =
              Objects.toString(
                  originProject.getAssignee().stream().map(Member::getId).toList(), null);
          Set<Member> assignee =
              value != null
                  ? memberService.getUsersByIds(new HashSet<>((List<Integer>) value))
                  : null;
          saveProject = projectMapper.putProjectAssignee(originProject, 0, assignee);
          history
              .fieldName(key)
              .beforeValue(originAssignee)
              .afterValue(Objects.toString(assignee.stream().map(Member::getId).toList(), null));
          break;
        case "dueDate":
        case "startDate":
        case "endDate":
          String originDate = Objects.toString(originProject.getEndDate(), null);
          LocalDateTime newDate =
              value != null
                  ? LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                      .atStartOfDay()
                  : LocalDateTime.MIN;
          saveProject = updateProjectDateField(originProject, key, newDate);
          history
              .fieldName(key)
              .beforeValue(originDate)
              .afterValue(Objects.toString(newDate, null));
          break;
        case "description":
          String originDescription = Objects.toString(originProject.getDescription(), null);
          String subDescription = Objects.toString(value, null);
          saveProject = projectMapper.putProjectDescription(originProject, subDescription);
          history.fieldName(key).beforeValue(originDescription).afterValue(subDescription);
          break;
        case "tag":
          String originTag = Objects.toString(originProject.getTag(), null);
          Set<String> tag = value != null ? new HashSet<>((List<String>) value) : null;
          saveProject = projectMapper.putProjectTag(originProject, 0, tag);
          history.fieldName(key).beforeValue(originTag).afterValue(Objects.toString(tag, null));
          break;
        default:
          throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
      }
      return saveProject;
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private Project updateProjectDateField(Project project, String key, LocalDateTime date) {
    switch (key) {
      case "dueDate":
        return projectMapper.putProjectDueDate(project, date);
      case "startDate":
        return projectMapper.putProjectStartDate(project, date);
      case "endDate":
        return projectMapper.putProjectEndDate(project, date);
      default:
        return null;
    }
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
