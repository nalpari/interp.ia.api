package net.devgrr.interp.ia.api.work.history;

import java.util.List;
import net.devgrr.interp.ia.api.work.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {

  List<History> findAllByCategoryAndIssueId(String category, Long issueId);
}
