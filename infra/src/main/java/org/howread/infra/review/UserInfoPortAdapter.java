package org.howread.infra.review;

import lombok.RequiredArgsConstructor;
import org.howread.review.application.dto.UserSummary;
import org.howread.review.application.port.UserInfoPort;
import org.howread.user.application.port.ProfileImagePort;
import org.howread.infra.user.UserJpaRepository;
import org.howread.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * [Adapter] UserInfoPort 구현체.
 *
 * review 모듈이 user 모듈을 직접 참조하지 않도록
 * infra 계층에서 User JPA 레포지토리를 통해 작성자 정보를 조회한다.
 */
@Component
@RequiredArgsConstructor
public class UserInfoPortAdapter implements UserInfoPort {

    private final UserJpaRepository userJpaRepository;
    private final ProfileImagePort profileImagePort;

    @Override
    public Map<Long, UserSummary> findSummariesByIds(Collection<Long> userIds) {
        return userJpaRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(
                        User::getId,
                        u -> new UserSummary(
                                u.getId(),
                                u.getNickname(),
                                u.getProfileImageKey() != null
                                        ? profileImagePort.buildUrl(u.getProfileImageKey())
                                        : null
                        )
                ));
    }
}
