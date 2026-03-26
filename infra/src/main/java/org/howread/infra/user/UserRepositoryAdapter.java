package org.howread.infra.user;

import lombok.RequiredArgsConstructor;
import org.howread.user.application.port.UserRepository;
import org.howread.user.domain.User;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * [Adapter] UserRepository Port를 JPA로 구현.
 *
 * 기술(JPA → Redis 등) 교체 시 이 클래스만 변경한다.
 * modules:user의 User 엔티티를 그대로 영속화하므로 별도 매핑 객체가 불필요하다.
 */
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return jpaRepository.existsByNickname(nickname);
    }

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll();
    }
}
