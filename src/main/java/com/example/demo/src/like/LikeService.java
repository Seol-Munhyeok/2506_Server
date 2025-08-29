package com.example.demo.src.like;

import com.example.demo.common.exceptions.BaseException;
import com.example.demo.src.feed.FeedRepository;
import com.example.demo.src.feed.entity.Feed;
import com.example.demo.src.feed.entity.FeedStatus;
import com.example.demo.src.like.entity.Like;
import com.example.demo.src.user.UserRepository;
import com.example.demo.src.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.demo.common.response.BaseResponseStatus.NOT_FIND_FEED;
import static com.example.demo.common.response.BaseResponseStatus.NOT_FIND_USER;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final FeedRepository feedRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long toggleLike(Long feedId, Long userId) {
        if (likeRepository.existsByFeedIdAndUserId(feedId, userId)) {
            likeRepository.deleteByFeedIdAndUserId(feedId, userId);
        } else {
            Feed feed = feedRepository.findById(feedId)
                    .orElseThrow(() -> new BaseException(NOT_FIND_FEED));
            if (feed.getStatus() != FeedStatus.ACTIVE) throw new BaseException(NOT_FIND_FEED);
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BaseException(NOT_FIND_USER));
            likeRepository.save(Like.builder().feed(feed).user(user).build());
        }
        return likeRepository.countByFeedId(feedId);
    }

    @Transactional
    public Long cancelLike(Long feedId, Long userId) {
        if (likeRepository.existsByFeedIdAndUserId(feedId, userId)) {
            likeRepository.deleteByFeedIdAndUserId(feedId, userId);
        }
        return likeRepository.countByFeedId(feedId);
    }
}
