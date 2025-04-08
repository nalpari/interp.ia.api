package net.devgrr.interp.ia.api.work.issue;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.IssueType;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.config.mapStruct.IssueMapper;
import net.devgrr.interp.ia.api.member.MemberService;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.member.entity.QMember;
import net.devgrr.interp.ia.api.util.DateUtil;
import net.devgrr.interp.ia.api.work.history.HistoryService;
import net.devgrr.interp.ia.api.work.issue.dto.IssueRequest;
import net.devgrr.interp.ia.api.work.issue.dto.IssueResponse;
import net.devgrr.interp.ia.api.work.issue.entity.Issue;
import net.devgrr.interp.ia.api.work.issue.entity.QIssue;
import net.devgrr.interp.ia.api.work.issue.file.IssueFileService;
import net.devgrr.interp.ia.api.work.project.ProjectService;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import net.devgrr.interp.ia.api.work.project.entity.QProject;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class IssueService {

  private final IssueRepository issueRepository;
  private final JPAQueryFactory queryFactory;
  private final IssueMapper issueMapper;
  private final IssueFileService issueFileService;
  private final MemberService memberService;
  private final ProjectService projectService;
  private final HistoryService historyService;

  private final QIssue qIssue = QIssue.issue;
  private final QProject qProject = QProject.project;
  private final QMember qCreator = new QMember("qCreator");
  private final QMember qAssignee = new QMember("qAssignee");

  public List<Issue> getIssues(Long projectId, Long issueId) throws BaseException {
    try {
      List<Issue> issues =
          queryFactory
              .selectFrom(qIssue)
              .innerJoin(qIssue.creator, qCreator)
              .innerJoin(qIssue.parentProject, qProject)
              .leftJoin(qIssue.assignee, qAssignee)
              .fetchJoin()
              .where(
                  qIssue.isDeleted.isFalse(),
                  projectId != null && projectId > 0 ? qIssue.parentProject.id.eq(projectId) : null,
                  issueId != null && issueId > 0
                      ? qIssue.id.eq(issueId)
                      : qIssue.parentIssue.id.isNull())
              .orderBy(qIssue.createdDate.asc(), qAssignee.name.asc())
              .fetch();

      List<Issue> structuredIssues = new ArrayList<>();
      for (Issue issue : issues) {
        List<Issue> subIssues = getSubIssuesById(issue.getId());
        List<Issue> relatedIssue = getRelatedIssuesById(issue.getId());
        structuredIssues.add(issueMapper.mapInvolvedIssues(issue, subIssues, relatedIssue));
      }
      return structuredIssues;

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public List<Issue> getIssuesByKeywords(
      Long projectId,
      Long parentIssueId,
      Long issueId,
      IssueType type,
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
          .selectFrom(qIssue)
          .innerJoin(qIssue.creator, qCreator)
          .innerJoin(qIssue.parentProject, qProject)
          .leftJoin(qIssue.assignee, qAssignee)
          .fetchJoin()
          .where(
              qIssue.isDeleted.isFalse(),
              issueId != null && issueId > 0 ? qIssue.id.eq(issueId) : null,
              projectId != null && projectId > 0 ? qIssue.parentProject.id.eq(projectId) : null,
              parentIssueId != null && parentIssueId > 0
                  ? qIssue.parentIssue.id.eq(parentIssueId)
                  : null,
              type != null ? qIssue.type.eq(type) : null,
              status != null ? qIssue.status.eq(status) : null,
              priority != null ? qIssue.priority.eq(priority) : null,
              StringUtils.hasText(title) ? qIssue.title.contains(title) : null,
              StringUtils.hasText(subTitle) ? qIssue.subTitle.contains(subTitle) : null,
              creatorId != null && creatorId > 0 ? qIssue.creator.id.eq(creatorId) : null,
              assigneeId != null && !assigneeId.isEmpty()
                  ? qIssue.assignee.any().id.in(assigneeId)
                  : null,
              DateUtil.isValidDateRange(createdDateFrom, createdDateTo)
                  ? qIssue.createdDate.between(
                      createdDateFrom.atStartOfDay(), createdDateTo.atStartOfDay())
                  : null,
              DateUtil.isValidDateRange(updatedDateFrom, updatedDateTo)
                  ? qIssue.updatedDate.between(
                      updatedDateFrom.atStartOfDay(), updatedDateTo.atStartOfDay())
                  : null,
              DateUtil.isValidDateRange(dueDateFrom, dueDateTo)
                  ? qIssue.dueDate.between(dueDateFrom, dueDateTo)
                  : null,
              DateUtil.isValidDateRange(startDateFrom, startDateTo)
                  ? qIssue.startDate.between(startDateFrom, startDateTo)
                  : null,
              DateUtil.isValidDateRange(endDateFrom, endDateTo)
                  ? qIssue.endDate.between(endDateFrom, endDateTo)
                  : null,
              tag != null && !tag.isEmpty() ? qIssue.tag.any().in(tag) : null)
          .orderBy(qIssue.createdDate.desc(), qAssignee.name.asc())
          .fetch();

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  public Issue getIssuesById(Long id) throws BaseException {
    Issue issue =
        queryFactory
            .selectFrom(qIssue)
            .innerJoin(qIssue.creator, qCreator)
            .innerJoin(qIssue.parentProject, qProject)
            .leftJoin(qIssue.assignee, qAssignee)
            .fetchJoin()
            .where(qIssue.isDeleted.isFalse(), qIssue.id.eq(id))
            .orderBy(qAssignee.name.asc())
            .fetchOne();
    if (issue == null) {
      throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 이슈입니다. (id: " + id + ")");
    }
    return issue;
  }

  public Set<Issue> getIssuesByIds(Set<Long> ids) {
    return new HashSet<>(issueRepository.findAllByIdInAndIsDeletedFalse(ids.stream().toList()));
  }

  public Issue getIssueWithRelatedById(Long id) throws BaseException {
    Issue issue = getIssuesById(id);

    List<Issue> subIssues = getSubIssuesById(id);
    Set<Issue> relatedIssue = issue.getRelatedIssues();
    relatedIssue.addAll(getRelatedIssuesById(id));

    return issueMapper.mapInvolvedIssues(issue, subIssues, relatedIssue.stream().toList());
  }

  public List<Issue> getSubIssuesById(Long pId) {
    List<Issue> subIssues =
        queryFactory
            .selectFrom(qIssue)
            .innerJoin(qIssue.creator, qCreator)
            .leftJoin(qIssue.assignee, qAssignee)
            .fetchJoin()
            .where(qIssue.isDeleted.isFalse(), qIssue.parentIssue.id.eq(pId))
            .orderBy(qIssue.createdDate.asc(), qAssignee.name.asc())
            .fetch();
    subIssues.forEach(issue -> issueMapper.mapSubIssues(issue, 0, getSubIssuesById(issue.getId())));
    return subIssues.isEmpty() ? null : subIssues;
  }

  public List<Issue> getRelatedIssuesById(Long id) {
    return queryFactory
        .selectFrom(qIssue)
        .innerJoin(qIssue.creator, qCreator)
        .leftJoin(qIssue.assignee, qAssignee)
        .fetchJoin()
        .where(qIssue.isDeleted.isFalse(), qIssue.relatedIssues.any().id.eq(id))
        .orderBy(qIssue.createdDate.asc(), qAssignee.name.asc())
        .fetch();
  }

  @Transactional
  public Issue setIssues(IssueRequest req, String username) throws BaseException {
    try {
      Project parentProject = projectService.getProjectsById(req.parentProjectId());
      Member creator = memberService.getUsersByEmail(username);
      Set<Member> assignees =
          req.assigneeId() != null && !req.assigneeId().isEmpty()
              ? memberService.getUsersByIds(req.assigneeId())
              : null;
      Issue parentIssue =
          req.parentIssueId() != null && req.parentIssueId() > 0
              ? getIssuesById(req.parentIssueId())
              : null;
      Set<Issue> relatedIssues =
          req.relatedIssuesId() != null && !req.relatedIssuesId().isEmpty()
              ? getIssuesByIds(req.relatedIssuesId())
              : null;

      Issue newIssue =
          issueMapper.mappingIssue(
              issueMapper.toIssue(req),
              creator,
              assignees,
              parentProject,
              parentIssue,
              relatedIssues);

      return issueRepository.save(newIssue);

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  @Transactional
  public void putIssuesById(Long id, Map<String, Object> req, UserDetails userDetails)
      throws BaseException {
    Issue originIssue = getIssuesById(id);
    try {
      String key = req.keySet().iterator().next();
      Object value = req.values().iterator().next();
      Member modifier = memberService.getUsersByEmail(userDetails.getUsername());
      String beforeValue;
      String afterValue;

      switch (key) {
        case "type":
          IssueType newType = IssueType.valueOf(value.toString());
          beforeValue = originIssue.getType().toString();
          afterValue = newType.toString();
          issueRepository.save(issueMapper.putIssueType(originIssue, 0, newType));
          break;
        case "status":
          IssueStatus newStatus = IssueStatus.valueOf(value.toString());
          beforeValue = Objects.toString(originIssue.getStatus(), null);
          afterValue = newStatus.toString();
          issueRepository.save(issueMapper.putIssueStatus(originIssue, 0, newStatus));
          break;
        case "priority":
          Priority newPriority = Priority.valueOf(value.toString());
          beforeValue = Objects.toString(originIssue.getPriority(), null);
          afterValue = newPriority.toString();
          issueRepository.save(issueMapper.putIssuePriority(originIssue, 0, newPriority));
          break;

        case "title":
          beforeValue = originIssue.getTitle();
          afterValue = value.toString();
          issueRepository.save(issueMapper.putIssueTitle(originIssue, value.toString()));
          break;
        case "subTitle":
          String newSubTitle = Objects.toString(value, null);
          beforeValue = originIssue.getSubTitle();
          afterValue = newSubTitle;
          issueRepository.save(issueMapper.putIssueSubTitle(originIssue, 0, newSubTitle));
          break;
        case "assigneeId":
          Set<Member> newAssignee =
              value != null
                  ? memberService.getUsersByIds(new HashSet<>((List<Integer>) value))
                  : null;
          beforeValue =
              !originIssue.getAssignee().isEmpty()
                  ? originIssue.getAssignee().stream().map(Member::getId).toList().toString()
                  : null;
          afterValue =
              newAssignee != null
                  ? newAssignee.stream().map(Member::getId).toList().toString()
                  : null;
          issueRepository.save(issueMapper.putIssueAssignee(originIssue, 0, newAssignee));
          break;
        case "dueDate":
        case "startDate":
        case "endDate":
          LocalDate newDate =
              value != null
                  ? LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                  : null;
          beforeValue =
              key.equals("dueDate")
                  ? Objects.toString(originIssue.getDueDate(), null)
                  : key.equals("startDate")
                      ? Objects.toString(originIssue.getStartDate(), null)
                      : Objects.toString(originIssue.getEndDate(), null);
          afterValue = newDate != null ? newDate.toString() : null;
          issueRepository.save(updateIssueDateField(originIssue, key, newDate));
          break;
        case "description":
          String newDescription = Objects.toString(value, null);
          beforeValue = originIssue.getDescription();
          afterValue = newDescription;
          issueRepository.save(issueMapper.putIssueDescription(originIssue, 0, newDescription));
          break;
        case "tag":
          Set<String> newTag = value != null ? new HashSet<>((List<String>) value) : null;
          beforeValue = !originIssue.getTag().isEmpty() ? originIssue.getTag().toString() : null;
          afterValue = Objects.toString(newTag, null);
          issueRepository.save(issueMapper.putIssueTag(originIssue, 0, newTag));
          break;
        case "relatedIssuesId":
          Set<Issue> newRelatedIssues =
              value != null ? getIssuesByIds(new HashSet<>((List<Long>) value)) : null;
          beforeValue =
              !originIssue.getRelatedIssues().isEmpty()
                  ? originIssue.getRelatedIssues().stream().map(Issue::getId).toList().toString()
                  : null;
          afterValue =
              newRelatedIssues != null
                  ? newRelatedIssues.stream().map(Issue::getId).toList().toString()
                  : null;
          issueRepository.save(issueMapper.putIssueRelatedIssues(originIssue, 0, newRelatedIssues));
          break;
        default:
          throw new BaseException(ErrorCode.INVALID_INPUT_VALUE);
      }

      historyService.setHistory(IssueCategory.PROJECT, id, beforeValue, afterValue, key, modifier);

    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private Issue updateIssueDateField(Issue issue, String key, LocalDate date) throws BaseException {
    return switch (key) {
      case "dueDate" -> issueMapper.putIssueDueDate(issue, 0, date);
      case "startDate" -> issueMapper.putIssueStartDate(issue, 0, date);
      case "endDate" -> issueMapper.putIssueEndDate(issue, 0, date);
      default -> throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "날짜 필드가 아닙니다.");
    };
  }

  @Transactional
  public void putIssuesDeletedFlagById(Long id, Boolean flag) throws BaseException {
    Issue issue =
        issueRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new BaseException(
                        ErrorCode.INVALID_INPUT_VALUE, "존재하지 않는 이슈입니다. (id: " + id + ")"));
    try {
      List<Long> ids = getAllChildIssueIds(issue.getId());
      queryFactory
          .update(qIssue)
          .set(qIssue.isDeleted, flag)
          .set(qIssue.updatedDate, LocalDateTime.now())
          .where(qIssue.id.in(ids))
          .execute();
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  private List<Long> getAllChildIssueIds(Long parentId) {
    List<Long> ids = new ArrayList<>(List.of(parentId));
    Queue<Long> queue = new ArrayDeque<>(List.of(parentId));
    while (!queue.isEmpty()) {
      Long id = queue.poll();
      List<Long> subIssueIds =
          queryFactory.select(qIssue.id).from(qIssue).where(qIssue.parentIssue.id.eq(id)).fetch();
      queue.addAll(subIssueIds);
      ids.addAll(subIssueIds);
    }
    return ids;
  }

  public boolean existById(Long id) {
    return issueRepository.existsById(id);
  }

  public void exportIssues(String format, Long projectId, List<Long> ids, OutputStream outputStream)
      throws IOException {

    List<IssueResponse> issues;
    if (ids != null) {
      issues =
          issueRepository.findAllByIdInAndIsDeletedFalse(ids).stream()
              .map(issueMapper::toResponse)
              .toList();
    } else {
      issues =
          issueRepository.findAllByParentProjectId(projectId).stream()
              .map(issueMapper::toResponse)
              .toList();
    }

    try {
      if ("csv".equals(format)) {
        issueFileService.exportIssuesToCsv(issues, outputStream);
      } else if ("xlsx".equals(format)) {
        issueFileService.exportIssuesToXlsx(issues, outputStream);
      }
    } catch (IOException e) {
      throw new IOException(e.getMessage(), e);
    }
  }
}
