package net.devgrr.interp.ia.api.comment;

import java.util.List;
import net.devgrr.interp.ia.api.comment.entity.Comment;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
  List<Comment> findAllByReferenceTypeAndReferenceId(IssueCategory referenceType, Long id);
}
