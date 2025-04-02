package net.devgrr.interp.ia.api.work.history;

import java.util.List;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import net.devgrr.interp.ia.api.member.entity.Member;
import net.devgrr.interp.ia.api.work.history.entity.History;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class HistoryService {
  private final HistoryRepository historyRepository;

  public List<History> getHistoryByCategoryAndIssueId(IssueCategory category, Long issueId) {
    return historyRepository.findAllByCategoryAndIssueId(category.getValue(), issueId);
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
}
