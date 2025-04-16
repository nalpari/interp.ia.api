package net.devgrr.interp.ia.api.work.history;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.work.history.entity.History;
import net.devgrr.interp.ia.api.work.issue.entity.Issue;
import net.devgrr.interp.ia.api.work.project.entity.Project;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class HistoryService {
  private final HistoryRepository historyRepository;

  public List<History> getHistoryByCategoryAndIssueId(IssueCategory category, Long issueId) {
    return historyRepository.findAllByCategoryAndIssueId(category, issueId);
  }

  @Transactional
  public void setHistory(
      IssueCategory category,
      Long issueId,
      String beforeValue,
      String afterValue,
      String fieldName,
      Member modifier)
      throws BaseException {
    try {
      historyRepository.save(
          History.builder()
              .category(category)
              .issueId(issueId)
              .beforeValue(beforeValue)
              .afterValue(afterValue)
              .fieldName(fieldName)
              .modifier(modifier)
              .build());
    } catch (Exception e) {
      throw new BaseException(ErrorCode.INTERNAL_SERVER_ERROR, e.getMessage());
    }
  }

  //  TODO : use queryDSL ??
  public List<History> getIssueHistoryByProjectId(Project project) {
    Long projectId = project.getId();

    // 프로젝트 자체 이력 조회
    List<History> histories =
        new ArrayList<>(
            historyRepository.findAllByCategoryAndIssueId(IssueCategory.PROJECT, projectId));

    // 프로젝트에 속한 이슈들에 대한 이력 조회
    Set<Long> subIssueIds =
        project.getSubIssues().stream().map(Issue::getId).collect(Collectors.toSet());

    List<History> subIssueHistories =
        historyRepository.findAllByCategory(IssueCategory.ISSUE).stream()
            .filter(h -> subIssueIds.contains(h.getIssueId()))
            .toList();

    histories.addAll(subIssueHistories);

    // modifiedDate 기준 내림차순 정렬
    histories.sort(Comparator.comparing(History::getModifiedDate).reversed());

    return histories;
  }
}
