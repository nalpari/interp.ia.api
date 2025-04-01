package net.devgrr.interp.ia.api.config.mapStruct;

import java.util.List;
import net.devgrr.interp.ia.api.comment.dto.CommentRequest;
import net.devgrr.interp.ia.api.comment.dto.CommentResponse;
import net.devgrr.interp.ia.api.comment.entity.Comment;
import net.devgrr.interp.ia.api.config.exception.BaseException;
import net.devgrr.interp.ia.api.config.exception.ErrorCode;
import net.devgrr.interp.ia.api.config.issue.IssueCategory;
import net.devgrr.interp.ia.api.member.entity.Member;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Named("toReferenceType")
    static IssueCategory toReferenceType(String type) throws BaseException {
        if (type == null) {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "참조 타입이 비어 있습니다. (issue/project)");
        }
        String r = type.toUpperCase();
        if("ISSUE".equals(r)) {
            return IssueCategory.ISSUE;
        } else if("PROJECT".equals(r)) {
            return IssueCategory.PROJECT;
        } else {
            throw new BaseException(ErrorCode.INVALID_INPUT_VALUE, "참조 타입이 잘못되었습니다. (issue/project)");
        }
    }

    @Mapping(source = "comment.member.email", target = "writerEmail")
    @Mapping(source = "comment.member.name", target = "writerName")
    CommentResponse toResponse(Comment comment);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "member", target = "member")
    @Mapping(source = "req.referenceType", target = "referenceType", qualifiedByName = "toReferenceType")
    Comment toComment(CommentRequest req, Member member);

    @Mapping(source = "parentComment.member.email", target = "writerEmail")
    @Mapping(source = "parentComment.member.name", target = "writerName")
    @Mapping(source = "childComments", target = "childComment")
    CommentResponse toResponseWithChildren(Comment parentComment, List<CommentResponse> childComments);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "content", source = "req.content")
    Comment updateComment(CommentRequest req, @MappingTarget Comment comment);
}
