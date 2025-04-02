package net.devgrr.interp.ia.api.config.mapStruct;

import java.util.List;
import net.devgrr.interp.ia.api.comment.dto.CommentRequest;
import net.devgrr.interp.ia.api.comment.dto.CommentResponse;
import net.devgrr.interp.ia.api.comment.entity.Comment;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CommentMapper {
  
  @Mapping(source = "comment.member.email", target = "writerEmail")
  @Mapping(source = "comment.member.name", target = "writerName")
  CommentResponse toResponse(Comment comment);

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "member", target = "member")
  Comment toComment(CommentRequest req, Member member);

  @Mapping(source = "parentComment.member.email", target = "writerEmail")
  @Mapping(source = "parentComment.member.name", target = "writerName")
  @Mapping(source = "childComments", target = "childComment")
  CommentResponse toResponseWithChildren(
      Comment parentComment, List<CommentResponse> childComments);

  @Mapping(target = "content", source = "req.content")
  @Mapping(target = "id", ignore = true)
  @Mapping(target = "parentCommentId", ignore = true)
  @Mapping(target = "member", ignore = true)
  @Mapping(target = "referenceType", ignore = true)
  @Mapping(target = "referenceId", ignore = true)
  Comment updateComment(CommentRequest req, @MappingTarget Comment comment);
}
