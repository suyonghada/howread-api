package org.howread.user.application;

import lombok.RequiredArgsConstructor;
import org.howread.user.application.port.UserRepository;
import org.howread.user.domain.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security의 UserDetailsService 구현.
 *
 * CLAUDE.md에 명시된 대로 modules:user에 위치한다.
 * loadUserByUsername은 이메일로 사용자를 조회하며,
 * 탈퇴 계정의 경우 Spring Security의 enabled=false 처리로 인증을 차단한다.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(String.valueOf(user.getId()))
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(user.getRole().toGrantedAuthority())))
                .disabled(user.isDeleted())
                .build();
    }
}
