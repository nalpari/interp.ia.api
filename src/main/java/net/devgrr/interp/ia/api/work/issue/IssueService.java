package net.devgrr.interp.ia.api.work.issue;

import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.mapStruct.IssueMapper;
import net.devgrr.interp.ia.api.member.MemberService;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.member.entity.QMember;
import net.devgrr.interp.ia.api.work.issue.dto.IssueRequest;
import net.devgrr.interp.ia.api.work.issue.entity.Issue;
import net.devgrr.interp.ia.api.work.issue.entity.QIssue;
import net.devgrr.interp.ia.api.work.project.ProjectService;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import net.devgrr.interp.ia.api.work.project.entity.QProject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
  private final QMember qMember = QMember.member;

  public Issue getIssueWithRelatedById(Long id) throws BaseException {
    Issue issue =
        queryFactory
            .selectFrom(qIssue)
            .innerJoin(qIssue.creator, qMember)
            .innerJoin(qIssue.parentProject, qProject)
            .fetchJoin()
            .where(qIssue.id.eq(id))
            .orderBy(qIssue.creator.name.asc(), qIssue.subIssues.any().createdDate.asc())
            .fetchOne();
    if (issue == null) {
      throw new BaseException(ErrorCode.NOT_FOUND, "해당 이슈를 찾을 수 없습니다.");
    }
    List<Issue> relatedIssue =
        queryFactory.selectFrom(qIssue).where(qIssue.relatedIssues.any().id.eq(id)).fetch();
    return issueMapper.mapRelatedIssues(issue, relatedIssue);
  }

  public Issue getIssuesById(Long id) {
    return issueRepository.findById(id).orElse(null);
  }

  public List<Issue> getIssuesByIds(Set<Long> ids) {
    return issueRepository.findAllById(ids);
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
