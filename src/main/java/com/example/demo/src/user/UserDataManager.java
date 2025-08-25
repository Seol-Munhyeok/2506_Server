package com.example.demo.src.user;

import com.example.demo.src.user.entity.User;
import lombok.RequiredArgsConstructor;
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

    public List<User> findAllByEmailAndState(String email, State state) {
        return userRepository.findAllByEmailAndState(email, state);
    }

    public List<User> findAllByState(State state) {
        return userRepository.findAllByState(state);
    }

    public List<User> findByPrivacyConsentDateBeforeAndPrivacyConsentStatus(LocalDateTime date, boolean status) {
        return userRepository.findByPrivacyConsentDateBeforeAndPrivacyConsentStatus(date, status);
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
