package net.devgrr.interp.ia.api.work.issue;

import java.util.List;
import net.devgrr.interp.ia.api.work.issue.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {

  List<Issue> findAllByIdInAndIsDeletedFalse(List<Long> ids);

  List<Issue> findAllByParentProjectId(Long projectId);
}
