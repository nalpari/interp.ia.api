package net.devgrr.interp.ia.api.work.project;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class ProjectService {

  private final ProjectRepository projectRepository;
  private final ProjectMapper projectMapper;
  private final MemberService memberService;
  private final HistoryService historyService;

  public List<Project> getProjects() {
    return projectRepository.findAll();
  }

  public List<Project> getProjectsByKeywords(
      String status,
      String priority,
      String title,
      String subTitle,
      String creator,
      String assignee,
      LocalDateTime createdDate,
      LocalDateTime updatedDate,
      LocalDateTime dueDate,
      LocalDateTime startDate,
      LocalDateTime endDate,
      Set<String> tag)
      throws BaseException {
    try {
      return projectRepository.findAll(
          (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null && !status.trim().isEmpty()) {
              predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (priority != null && !priority.trim().isEmpty()) {
              predicates.add(criteriaBuilder.equal(root.get("priority"), priority));
            }
            if (title != null && !title.trim().isEmpty()) {
              predicates.add(criteriaBuilder.like(root.get("title"), "%" + title + "%"));
            }
            if (subTitle != null && !subTitle.trim().isEmpty()) {
              predicates.add(criteriaBuilder.like(root.get("subTitle"), "%" + subTitle + "%"));
            }
            if (creator != null && !creator.trim().isEmpty()) {
              predicates.add(criteriaBuilder.equal(root.get("creator").get("name"), creator));
            }
            if (assignee != null && !assignee.trim().isEmpty()) {
              predicates.add(criteriaBuilder.equal(root.join("assignee").get("name"), assignee));
            }
            if (createdDate != null && !createdDate.equals(LocalDateTime.MIN)) {
              predicates.add(criteriaBuilder.equal(root.get("createdDate"), createdDate));
            }
            if (updatedDate != null && !updatedDate.equals(LocalDateTime.MIN)) {
              predicates.add(criteriaBuilder.equal(root.get("updatedDate"), updatedDate));
            }
            if (dueDate != null && !dueDate.equals(LocalDateTime.MIN)) {
              predicates.add(criteriaBuilder.equal(root.get("dueDate"), dueDate));
            }
            if (startDate != null && !startDate.equals(LocalDateTime.MIN)) {
              predicates.add(criteriaBuilder.equal(root.get("startDate"), startDate));
            }
            if (endDate != null && !endDate.equals(LocalDateTime.MIN)) {
              predicates.add(criteriaBuilder.equal(root.get("endDate"), endDate));
            }
            if (tag != null && !tag.isEmpty()) {
              predicates.add(
                  criteriaBuilder.or(
                      tag.stream()
                          .map(t -> criteriaBuilder.like(root.get("tag"), "%" + t + "%"))
                          .toArray(Predicate[]::new)));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
          });
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
      historyService.setHistory(history.build());

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
