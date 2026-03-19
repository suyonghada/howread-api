package org.howread.infra.config;

import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import javax.sql.DataSource;
import java.util.Map;

/**
 * JPA 공통 설정.
 *
 * @EnableJpaAuditing: @SpringBootApplication에 직접 붙이면 @WebMvcTest 슬라이스 테스트 시
 *   JPA 컨텍스트 없이 AuditingEntityListener가 로드되어 오류 발생하므로 별도 분리.
 *
 * @EnableJpaRepositories: Spring Boot의 AutoConfigurationPackages는 @SpringBootApplication
 *   위치(org.howread.app)만 등록하므로, infra 패키지의 JPA Repository를 자동으로 스캔하지 못한다.
 *
 * entityManagerFactory: 도메인 엔티티가 modules:* 패키지에 있으므로 packagesToScan을
 *   org.howread 전체로 설정해 Spring Boot 4.x의 자동 엔티티 스캔 한계를 보완한다.
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "org.howread.infra",
        entityManagerFactoryRef = "entityManagerFactory")
public class JpaAuditingConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource) {
        return builder
                .dataSource(dataSource)
                .packages("org.howread")
                .properties(Map.of())
                .build();
    }
}
