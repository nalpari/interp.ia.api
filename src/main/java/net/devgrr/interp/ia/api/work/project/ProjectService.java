package net.devgrr.interp.ia.api.work.project;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

      projectRepository.save(saveProject);
      /* TODO: project save(update)가 수행되었을 때만 history save 하도록 처리 추가 필요 */
      historyService.setHistory(history.build());

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  /* TODO: null 일 경우의 처리 추가 필요 */
  private Project updateProjectField(
      Project originProject, String key, Object value, HistoryBuilder history)
      throws BaseException {
    Project saveProject = null;
    switch (key) {
      case "status":
        String originStatus = originProject.getStatus().toString();
        IssueStatus status = IssueStatus.valueOf(value.toString());
        saveProject = projectMapper.putProjectStatus(originProject, status);
        history.fieldName(key).beforeValue(originStatus).afterValue(status.toString());
        break;
      case "priority":
        String originPriority = originProject.getPriority().toString();
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
        String originSubTitle = originProject.getSubTitle();
        saveProject = projectMapper.putProjectSubTitle(originProject, value.toString());
        history.fieldName(key).beforeValue(originSubTitle).afterValue(value.toString());
        break;
      case "assignee":
        String originAssignee =
            originProject.getAssignee().stream().map(Member::getId).toList().toString();
        Set<Member> assignee = memberService.getUsersByIds(new HashSet<>((List<Integer>) value));
        saveProject = projectMapper.putProjectAssignee(originProject, 0, assignee);
        history
            .fieldName(key)
            .beforeValue(originAssignee)
            .afterValue(assignee.stream().map(Member::getId).toList().toString());
        break;
      case "dueDate":
        String originDueDate = originProject.getDueDate().toString();
        LocalDateTime dueDate =
            LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay();
        saveProject = projectMapper.putProjectDueDate(originProject, dueDate);
        history.fieldName(key).beforeValue(originDueDate).afterValue(dueDate.toString());
        break;
      case "startDate":
        String originStartDate = originProject.getStartDate().toString();
        LocalDateTime startDate =
            LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay();
        saveProject = projectMapper.putProjectStartDate(originProject, startDate);
        history.fieldName(key).beforeValue(originStartDate).afterValue(startDate.toString());
        break;
      case "endDate":
        String originEndDate = originProject.getEndDate().toString();
        LocalDateTime endDate =
            LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                .atStartOfDay();
        saveProject = projectMapper.putProjectEndDate(originProject, endDate);
        history.fieldName(key).beforeValue(originEndDate).afterValue(endDate.toString());
        break;
      case "description":
        String originDescription = originProject.getDescription();
        saveProject = projectMapper.putProjectDescription(originProject, value.toString());
        history.fieldName(key).beforeValue(originDescription).afterValue(value.toString());
        break;
      case "tag":
        String originTag = originProject.getTag().toString();
        saveProject =
            projectMapper.putProjectTag(originProject, 0, new HashSet<>((List<String>) value));
        history.fieldName(key).beforeValue(originTag).afterValue(value.toString());
        break;
      default:
        break;
    }
    return saveProject;
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
