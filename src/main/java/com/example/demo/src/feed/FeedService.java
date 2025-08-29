package com.example.demo.src.feed;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.common.response.BaseResponseStatus;
import com.example.demo.src.comment.CommentRepository;
import com.example.demo.src.feed.entity.Feed;
import com.example.demo.src.feed.entity.FeedMedia;
import com.example.demo.src.feed.entity.FeedMediaType;
import com.example.demo.src.feed.entity.FeedStatus;
import com.example.demo.src.feed.model.AuthorProfile;
import com.example.demo.src.feed.model.GetFeedRes;
import com.example.demo.src.feed.model.PatchFeedReq;
import com.example.demo.src.feed.model.PostFeedReq;
import com.example.demo.src.like.LikeRepository;
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
    private final FeedMediaRepository feedMediaRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public List<GetFeedRes> getFeeds(int pageIndex, int size) {
        return feedRepository.findAllByStatusOrderByCreatedAtDesc(
                        FeedStatus.ACTIVE,
                        PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt")))
                .stream()
                .map(f -> {
                    AuthorProfile profile = new AuthorProfile(
                            f.getUser().getId(),
                            f.getUser().getLoginId(),
                            f.getUser().getName());
                    List<String> imageUrls = feedMediaRepository
                            .findAllByFeedIdAndMediaType(f.getId(), FeedMediaType.IMAGE)
                            .stream()
                            .map(FeedMedia::getMediaUrl)
                            .collect(Collectors.toList());
                    List<String> videoUrls = feedMediaRepository
                            .findAllByFeedIdAndMediaType(f.getId(), FeedMediaType.VIDEO)
                            .stream()
                            .map(FeedMedia::getMediaUrl)
                            .collect(Collectors.toList());
                    Long likeCount = likeRepository.countByFeedId(f.getId());
                    return new GetFeedRes(f.getId(), f.getUser().getId(), f.getContent(), f.getCreatedAt(), profile, imageUrls, videoUrls, likeCount);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GetFeedRes getFeed(Long feedId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
        if (feed.getStatus() != FeedStatus.ACTIVE) throw new BaseException(NOT_FIND_FEED);
        AuthorProfile profile = new AuthorProfile(
                feed.getUser().getId(),
                feed.getUser().getLoginId(),
                feed.getUser().getName());
        List<String> imageUrls = feedMediaRepository
                .findAllByFeedIdAndMediaType(feed.getId(), FeedMediaType.IMAGE)
                .stream()
                .map(FeedMedia::getMediaUrl)
                .collect(Collectors.toList());
        List<String> videoUrls = feedMediaRepository
                .findAllByFeedIdAndMediaType(feed.getId(), FeedMediaType.VIDEO)
                .stream()
                .map(FeedMedia::getMediaUrl)
                .collect(Collectors.toList());
        Long likeCount = likeRepository.countByFeedId(feed.getId());
        return new GetFeedRes(feed.getId(), feed.getUser().getId(), feed.getContent(), feed.getCreatedAt(), profile, imageUrls, videoUrls, likeCount);
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
        feedRepository.save(feed);
        List<String> imageUrls = req.getImageUrls();
        if (imageUrls != null) {
            imageUrls.forEach(url ->
                    feedMediaRepository.save(
                            FeedMedia.builder()
                                    .feed(feed)
                                    .mediaUrl(url)
                                    .mediaType(FeedMediaType.IMAGE)
                                    .build()));
        }
        List<String> videoUrls = req.getVideoUrls();
        if (videoUrls != null) {
            videoUrls.forEach(url ->
                    feedMediaRepository.save(
                            FeedMedia.builder()
                                    .feed(feed)
                                    .mediaUrl(url)
                                    .mediaType(FeedMediaType.VIDEO)
                                    .build()));
        }
        return feed.getId();
    }

    @Transactional
    public void updateFeed(Long feedId, Long userId, PatchFeedReq req) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
        if (!feed.getUser().getId().equals(userId)) throw new BaseException(INVALID_FEED_USER);
        String content = req.getContent();
        validateContent(content);
        feed.updateContent(content);
        List<String> imageUrls = req.getImageUrls();
        List<String> videoUrls = req.getVideoUrls();
        if (imageUrls != null || videoUrls != null) {
            feedMediaRepository.deleteAll(feedMediaRepository.findAllByFeedId(feedId));
            if (imageUrls != null) {
                imageUrls.forEach(url ->
                        feedMediaRepository.save(
                                FeedMedia.builder()
                                        .feed(feed)
                                        .mediaUrl(url)
                                        .mediaType(FeedMediaType.IMAGE)
                                        .build()));
            }
            if (videoUrls != null) {
                videoUrls.forEach(url ->
                        feedMediaRepository.save(
                                FeedMedia.builder()
                                        .feed(feed)
                                        .mediaUrl(url)
                                        .mediaType(FeedMediaType.VIDEO)
                                        .build()));
            }
        }
    }

    @Transactional
    public void deleteFeed(Long feedId, Long userId) {
        Feed feed = feedRepository.findById(feedId)
                .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
        if (!feed.getUser().getId().equals(userId)) throw new BaseException(INVALID_FEED_USER);
        likeRepository.deleteAll(likeRepository.findAllByFeedId(feedId));
        commentRepository.deleteAllByFeedId(feedId);
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


