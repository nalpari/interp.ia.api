package net.devgrr.interp.ia.api.work.issue;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.issue.IssueStatus;
import net.devgrr.interp.ia.api.config.issue.IssueType;
import net.devgrr.interp.ia.api.config.issue.Priority;
import net.devgrr.interp.ia.api.config.mapStruct.IssueMapper;
import net.devgrr.interp.ia.api.member.MemberService;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.member.entity.QMember;
import net.devgrr.interp.ia.api.util.DateUtil;
import net.devgrr.interp.ia.api.work.issue.dto.IssueRequest;
import net.devgrr.interp.ia.api.work.issue.entity.Issue;
import net.devgrr.interp.ia.api.work.issue.entity.QIssue;
import net.devgrr.interp.ia.api.work.project.ProjectService;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import net.devgrr.interp.ia.api.work.project.entity.QProject;
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
  private final MemberService memberService;
  private final ProjectService projectService;

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
      Long parentProjectId,
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
              parentProjectId != null && parentProjectId > 0
                  ? qIssue.parentProject.id.eq(parentProjectId)
                  : null,
              parentIssueId != null && parentIssueId > 0
                  ? qIssue.parentIssue.id.eq(parentIssueId)
                  : null,
              issueId != null && issueId > 0 ? qIssue.id.eq(issueId) : null,
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

  public Issue getIssuesById(Long id) {
    return queryFactory
        .selectFrom(qIssue)
        .innerJoin(qIssue.creator, qCreator)
        .innerJoin(qIssue.parentProject, qProject)
        .leftJoin(qIssue.assignee, qAssignee)
        .fetchJoin()
        .where(qIssue.id.eq(id))
        .orderBy(qAssignee.name.asc())
        .fetchOne();
  }

  public List<Issue> getIssuesByIds(Set<Long> ids) {
    return issueRepository.findAllById(ids);
  }

  public Issue getIssueWithRelatedById(Long id) throws BaseException {
    Issue issue = getIssuesById(id);
    if (issue == null) {
      throw new BaseException(ErrorCode.NOT_FOUND, "해당 이슈를 찾을 수 없습니다.");
    }

    List<Issue> subIssues = getSubIssuesById(id);
    List<Issue> relatedIssue = getRelatedIssuesById(id);

    return issueMapper.mapInvolvedIssues(issue, subIssues, relatedIssue);
  }

  public List<Issue> getSubIssuesById(Long pId) {
    List<Issue> subIssues =
        queryFactory
            .selectFrom(qIssue)
            .innerJoin(qIssue.creator, qCreator)
            .leftJoin(qIssue.assignee, qAssignee)
            .fetchJoin()
            .where(qIssue.parentIssue.id.eq(pId))
            .orderBy(qIssue.createdDate.asc(), qAssignee.name.asc())
            .fetch();
    subIssues.forEach(issue -> issueMapper.mapSubIssues(issue, 0, getSubIssuesById(issue.getId())));
    return subIssues;
  }

  public List<Issue> getRelatedIssuesById(Long id) {
    return queryFactory
        .selectFrom(qIssue)
        .innerJoin(qIssue.creator, qCreator)
        .leftJoin(qIssue.assignee, qAssignee)
        .fetchJoin()
        .where(qIssue.relatedIssues.any().id.eq(id))
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
              ? new HashSet<>(getIssuesByIds(req.relatedIssuesId()))
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
  public void deleteIssuesById(Long id) throws BaseException {
    try {
      issueRepository.deleteById(id);
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }
}
