package com.example.demo.src.user;

import com.example.demo.src.user.entity.AccountStatus;
import com.example.demo.src.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.demo.common.entity.BaseEntity.State;

@RequiredArgsConstructor
@Repository
public class UserDataManager {
    private final UserRepository userRepository;

    public Optional<User> findByIdAndState(Long id, State state) {
        return userRepository.findByIdAndState(id, state);
    }

    public Optional<User> findByEmailAndState(String email, State state) {
        return userRepository.findByEmailAndState(email, state);
    }

    public Optional<User> findByLoginIdAndState(String loginId, State state) {
        return userRepository.findByLoginIdAndState(loginId, state);
    }

    public List<User> findAllByEmailAndState(String email, State state, Pageable pageable) {
        return userRepository.findAllByEmailAndState(email, state, pageable);
    }

    public List<User> findAllByState(State state, Pageable pageable) {
        return userRepository.findAllByState(state, pageable);
    }

    public List<User> findByPrivacyConsentDateBeforeAndPrivacyConsentStatus(LocalDateTime date, boolean status) {
        return userRepository.findByPrivacyConsentDateBeforeAndPrivacyConsentStatus(date, status);
    }

    public List<User> searchUsers(Long userId, String name, LocalDateTime joinedStart, LocalDateTime joinedEnd,
                                  AccountStatus status, Pageable pageable) {
        Specification<User> spec = Specification.where((root, query, cb) -> cb.equal(root.get("state"), State.ACTIVE));

        if (userId != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("id"), userId));
        }
        if (name != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("name"), name));
        }
        if (joinedStart != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("joinedStart"), joinedStart));
        }
        if (joinedEnd != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("joinedEnd"), joinedEnd));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("accountStatus"), status));
        }

        return userRepository.findAll(spec, pageable).getContent();
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
