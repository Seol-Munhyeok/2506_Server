package com.example.demo.src.feed;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.feed.entity.Feed;
import com.example.demo.src.feed.entity.FeedStatus;
import com.example.demo.src.feed.model.GetFeedRes;
import com.example.demo.src.feed.model.PatchFeedReq;
import com.example.demo.src.feed.model.PostFeedReq;
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
public class FeedService {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<GetFeedRes> getFeeds(int pageIndex, int size) {
        return feedRepository.findAllByStatusOrderByCreatedAtDesc(
                        FeedStatus.ACTIVE,
                        PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(f -> new GetFeedRes(f.getId(), f.getUser().getId(), f.getContent(), f.getCreatedAt()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GetFeedRes getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
        if (feed.getStatus() != FeedStatus.ACTIVE) throw new BaseException(NOT_FIND_FEED);
        return new GetFeedRes(feed.getId(), feed.getUser().getId(), feed.getContent(), feed.getCreatedAt());
    }

    @Transactional
    public Long createFeed(Long userId, PostFeedReq req) {
        String content = req.getContent();
        validateContent(content);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(BaseResponseStatus.NOT_FIND_USER));
        Feed feed = Feed.builder()
                .user(user)
                .content(content)
                .status(FeedStatus.ACTIVE)
                .build();
        return feedRepository.save(feed).getId();
    }

    @Transactional
    public void updateFeed(Long feedId, Long userId, PatchFeedReq req) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
        if (!feed.getUser().getId().equals(userId)) throw new BaseException(INVALID_FEED_USER);
        String content = req.getContent();
        validateContent(content);
        feed.updateContent(content);
    }

    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
        if (!feed.getUser().getId().equals(userId)) throw new BaseException(INVALID_FEED_USER);
        feed.changeStatus(FeedStatus.INACTIVE);
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


