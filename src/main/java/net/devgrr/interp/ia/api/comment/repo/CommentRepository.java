package net.devgrr.interp.ia.api.comment.repo;

import net.devgrr.interp.ia.api.comment.entity.Comment;
import net.devgrr.interp.ia.api.comment.entity.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, CommentRepositoryCustom {
    List<Comment> findAllByReferenceTypeAndId(ReferenceType referenceType, Long id);
}
