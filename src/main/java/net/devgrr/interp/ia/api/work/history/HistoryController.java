package net.devgrr.interp.ia.api.work.history;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import net.devgrr.interp.ia.api.config.mapStruct.HistoryMapper;
import net.devgrr.interp.ia.api.work.history.dto.HistoryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/histories")
@RequiredArgsConstructor
@Tag(name = "HistoryController", description = "변경이력 API")
@RestController
public class HistoryController {

  private final HistoryService historyService;
  private final HistoryMapper historyMapper;

  @Operation(description = "이슈의 변경이력을 조회한다.")
  @GetMapping("/{issueId}")
  public List<HistoryResponse> getHistoryByIssueId(
      @PathVariable("issueId") @Parameter(description = "이슈 ID") Long issueId) {
    return historyService.getHistoryByIssueId(issueId).stream()
        .map(historyMapper::toResponse)
        .collect(Collectors.toList());
  }
}
