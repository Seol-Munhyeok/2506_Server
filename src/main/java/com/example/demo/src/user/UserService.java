package com.example.demo.src.user;



import com.example.demo.common.entity.BaseEntity.State;
import com.example.demo.common.exceptions.BaseException;
import com.example.demo.src.user.entity.AccountStatus;
import com.example.demo.src.user.entity.User;
import com.example.demo.src.user.model.*;
import com.example.demo.utils.JwtService;
import com.example.demo.utils.SHA256;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.example.demo.common.entity.BaseEntity.State.ACTIVE;
import static com.example.demo.common.response.BaseResponseStatus.*;

// Service Create, Update, Delete 의 로직 처리
@Transactional
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserDataManager userDataManager;
    private final JwtService jwtService;


    //POST
    public PostUserRes createUser(PostUserReq postUserReq) {
        //이메일 중복 체크
        Optional<User> checkUser = userDataManager.findByEmailAndState(postUserReq.getEmail(), ACTIVE);
        if (checkUser.isPresent()) {
            throw new BaseException(POST_USERS_EXISTS_EMAIL);
        }

        // LoginId 중복 체크
        if (userDataManager.findByLoginIdAndState(postUserReq.getLoginId(), ACTIVE).isPresent()) {
            throw new BaseException(POST_USERS_EXISTS_LOGIN_ID);
        }

        String encryptPwd;
        try {
            encryptPwd = SHA256.encrypt(postUserReq.getPassword());
            postUserReq.setPassword(encryptPwd);
        } catch (Exception exception) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        User saveUser = userDataManager.save(postUserReq.toEntity());

        String jwtToken = jwtService.createJwt(saveUser.getId());
        return new PostUserRes(saveUser.getId(), jwtToken);
    }

    public PostUserRes createOAuthUser(User user) {
        User saveUser = userDataManager.save(user);

        // JWT 발급
        String jwtToken = jwtService.createJwt(saveUser.getId());
        return new PostUserRes(saveUser.getId(), jwtToken);
    }

    public void modifyUserName(Long userId, PatchUserReq patchUserReq) {
        User user = userDataManager.findByIdAndState(userId, ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        user.updateName(patchUserReq.getName());
    }

    public void deleteUser(Long userId) {
        User user = userDataManager.findByIdAndState(userId, ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        user.deleteUser();
    }

    public void suspendUser(Long userId) {
        User user = userDataManager.findByIdAndState(userId, ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        user.suspendUser();
        userDataManager.save(user);
    }


    @Transactional(readOnly = true)
    public List<GetUserRes> getUsers(Long userId, String name, LocalDateTime joinedStart,
                                     LocalDateTime joinedEnd, AccountStatus status,
                                     int pageIndex, int size) {
        PageRequest pageRequest = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "joinedAt"));
        return userDataManager.searchUsers(userId, name, joinedStart, joinedEnd, status, pageRequest)
                .stream()
                .map(GetUserRes::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GetUserRes> getUsersByEmail(String email, int pageIndex, int size) {
        PageRequest pageRequest = PageRequest.of(pageIndex, size);
        List<GetUserRes> users = userDataManager.findAllByEmailAndState(email, ACTIVE, pageRequest)
                .stream()
                .map(GetUserRes::new)
                .collect(Collectors.toList());
        if (users.isEmpty()) {
            throw new BaseException(NOT_FIND_USER);
        }

        return users;
    }


    @Transactional(readOnly = true)
    public GetUserRes getUser(Long userId) {
        User user = userDataManager.findByIdAndState(userId, ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));
        return new GetUserRes(user);
    }

    @Transactional(readOnly = true)
    public boolean checkUserByEmail(String email) {
        Optional<User> result = userDataManager.findByEmailAndState(email, ACTIVE);
        if (result.isPresent()) return true;
        return false;
    }

    public PostLoginRes logIn(PostLoginReq postLoginReq) {
        User user = userDataManager.findByLoginIdAndState(postLoginReq.getLoginId(), ACTIVE)
                .orElseThrow(() -> new BaseException(NOT_FIND_USER));

        switch (user.getAccountStatus()) {
            case DORMANT: throw new BaseException(DORMANT_USER);
            case BLOCKED: throw new BaseException(BLOCKED_USER);
            case SUSPENDED: throw new BaseException(SUSPENDED_USER);
            case WITHDRAWN: throw new BaseException(WITHDRAWN_USER);
        }

        String encryptPwd;
        try {
            encryptPwd = SHA256.encrypt(postLoginReq.getPassword());
        } catch (Exception exception) {
            throw new BaseException(PASSWORD_ENCRYPTION_ERROR);
        }

        if(user.getPassword().equals(encryptPwd)){
            user.setLastLoginAt(LocalDateTime.now());
            userDataManager.save(user);
            Long userId = user.getId();
            String jwt = jwtService.createJwt(userId);
            return new PostLoginRes(userId, jwt);
        } else {
            throw new BaseException(FAILED_TO_LOGIN);
        }

    }

    public GetUserRes getUserByEmail(String email) {
        User user = userDataManager.findByEmailAndState(email, ACTIVE).orElseThrow(() -> new BaseException(NOT_FIND_USER));
        return new GetUserRes(user);
    }

    @Transactional(readOnly = true)
    public boolean isLoginIdDuplicate(String loginId) {
        return userDataManager.findByLoginIdAndState(loginId, ACTIVE).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isEmailDuplicate(String email) {
        return userDataManager.findByEmailAndState(email, ACTIVE).isPresent();
    }

    @Transactional(readOnly = true)
    public boolean isAdmin(Long userId) {
        return userDataManager.findByIdAndState(userId, ACTIVE)
                .map(user -> "admin".equals(user.getLoginId()))
                .orElse(false);
    }
}
