package com.example.demo.src.comment;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.comment.entity.Comment;
import com.example.demo.src.comment.model.GetCommentRes;
import com.example.demo.src.comment.model.PostCommentReq;
import com.example.demo.src.feed.FeedRepository;
import com.example.demo.src.feed.entity.Feed;
import com.example.demo.src.feed.entity.FeedStatus;
import com.example.demo.src.feed.model.AuthorProfile;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.demo.common.response.BaseResponseStatus.*;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<GetCommentRes> getComments(Long feedId, int pageIndex, int size) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
        if (feed.getStatus() != FeedStatus.ACTIVE) throw new BaseException(NOT_FIND_FEED);
        return commentRepository.findAllByFeedId(
                        feedId,
                        PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(c -> {
                    AuthorProfile profile = new AuthorProfile(
                            c.getUser().getId(),
                            c.getUser().getLoginId(),
                            c.getUser().getName());
                    return new GetCommentRes(c.getId(), profile, c.getContent(), c.getCreatedAt());
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public GetCommentRes createComment(Long feedId, Long userId, PostCommentReq req) {
        String content = req.getContent();
        validateContent(content);
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
        if (feed.getStatus() != FeedStatus.ACTIVE) throw new BaseException(NOT_FIND_FEED);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FIND_USER));
        Comment comment = Comment.builder()
                .feed(feed)
                .user(user)
                .content(content)
                .build();
        commentRepository.save(comment);
        AuthorProfile profile = new AuthorProfile(user.getId(), user.getLoginId(), user.getName());
        return new GetCommentRes(comment.getId(), profile, comment.getContent(), comment.getCreatedAt());
    }

    private void validateContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BaseException(FEEDS_EMPTY_CONTENT);
        }
        String trimmed = content.trim();
        if (trimmed.length() > 1000) {
            throw new BaseException(POST_FEEDS_INVALID_CONTENT);
        }
    }
}
