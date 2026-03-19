package org.howread.infra.config;

import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA 공통 설정.
 *
 * @EnableJpaAuditing: @SpringBootApplication에 직접 붙이면 @WebMvcTest 슬라이스 테스트 시
 *   JPA 컨텍스트 없이 AuditingEntityListener가 로드되어 오류 발생하므로 별도 분리.
 *
 * @EnableJpaRepositories: Spring Boot의 AutoConfigurationPackages는 @SpringBootApplication
 *   위치(org.howread.app)만 등록하므로, infra 패키지의 JPA Repository를 자동으로 스캔하지 못한다.
 *
 * @EntityScan: 도메인 엔티티가 modules:* 패키지에 위치하므로, Spring Boot 4.x 기준
 *   org.springframework.boot.persistence.autoconfigure.EntityScan으로 전체 스캔 범위를 확장한다.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "org.howread.infra")
@EntityScan(basePackages = "org.howread")
public class JpaAuditingConfig {
}
