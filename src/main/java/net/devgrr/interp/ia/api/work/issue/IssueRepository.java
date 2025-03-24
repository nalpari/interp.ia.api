package net.devgrr.interp.ia.api.work.issue;

import net.devgrr.interp.ia.api.work.issue.entity.Issue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IssueRepository extends JpaRepository<Issue, Long> {}
