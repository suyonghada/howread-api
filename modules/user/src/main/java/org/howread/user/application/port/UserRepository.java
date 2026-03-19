package org.howread.user.application.port;

import org.howread.user.domain.User;

import java.util.Optional;

/**
 * [Port] User 저장소 계약.
 *
 * 도메인이 외부 저장소에 요청하는 계약만 정의한다.
 * 구현체(JPA, Redis, In-Memory 등)를 전혀 알지 못하므로
 * 기술이 교체되어도 이 인터페이스는 변경되지 않는다.
 *
 * 구현체는 infra 모듈의 UserRepositoryAdapter에 위치한다.
 */
public interface UserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    User save(User user);
}
