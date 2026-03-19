# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# 전체 빌드
./gradlew build

# 특정 모듈 컴파일
./gradlew :app:compileJava
./gradlew :modules:review:compileJava

# 전체 테스트
./gradlew test

# 특정 모듈 테스트
./gradlew :modules:book:test

# 단일 테스트 클래스 실행
./gradlew :modules:book:test --tests "org.howread.book.BookServiceTest"

# 단일 테스트 메서드 실행
./gradlew :modules:book:test --tests "org.howread.book.BookServiceTest.createBook_success"

# 실행 가능한 jar 빌드 (app 모듈만)
./gradlew :app:bootJar

# 애플리케이션 실행
./gradlew :app:bootRun

# 모듈 의존성 구조 확인
./gradlew projects
./gradlew :app:dependencies
```

## Architecture

### 멀티 모듈 구조 및 의존성 방향

```
app → infra → modules:* → common
app →         modules:* → common
```

| 모듈               | 역할            | 주요 내용                                                                                   |
|------------------|---------------|-----------------------------------------------------------------------------------------|
| `app`            | 실행 진입점        | `@SpringBootApplication`, Web/Security 설정, Controller. `bootJar` 생성 유일 모듈.              |
| `common`         | Shared Kernel | 공통 예외 계층, API 응답 래퍼, Value Object, `DomainEvent` 인터페이스. 다른 모듈에 의존하지 않음.                 |
| `infra`          | 인프라 어댑터       | `modules:*`의 Repository Port를 JPA로 구현(Adapter). Redis, Kafka Producer, 외부 API 클라이언트 위치. |
| `modules:book`   | Book 도메인      | Book 엔티티, Repository 인터페이스(Port), BookService(Use Case)                                 |
| `modules:review` | Review 도메인    | Review 엔티티, Port, ReviewService. 도메인 이벤트(→Kafka 연동 확장 포인트)                              |
| `modules:user`   | User 도메인      | User 엔티티, Port, UserService. Spring Security `UserDetailsService` 구현 포함.                |

**핵심 원칙:** `modules:*`는 `infra`를 모른다. Repository 인터페이스(Port)는 도메인 모듈에 정의하고, JPA 구현(Adapter)은 `infra`에 둔다. 이를 통해 도메인이
인프라에 오염되지 않으며, 추후 특정 모듈을 독립 서비스로 분리할 때 해당 모듈만 들고 나갈 수 있다.

### 패키지 구조 관례

```
org.howread.app.*       # app 모듈
org.howread.common.*    # common 모듈
org.howread.infra.*     # infra 모듈
org.howread.book.*      # modules:book
org.howread.review.*    # modules:review
org.howread.user.*      # modules:user
```

### 도메인 모듈 내부 구조 (예: modules:review)

```
org.howread.review
├── domain/          # 엔티티, Value Object — Rich Domain Model 지향
├── application/     # Use Case (Service), Port 인터페이스(Repository interface)
└── event/           # 도메인 이벤트 (EDA 확장 포인트)
```

### 설계 원칙

- **Rich Domain Model**: 비즈니스 로직은 Service가 아닌 도메인 엔티티 안으로 위임한다.
- **EDA 준비**: 모듈 간 직접 호출 대신 `DomainEvent`를 통해 통신한다. 현재는 `ApplicationEventPublisher`로 발행하고, 추후 Kafka로 교체할 수 있도록 추상화 계층을
  유지한다.
- **Spring Boot 플러그인**: `app` 모듈에만 적용. 나머지 모듈은 BOM(`spring-boot-dependencies:4.0.3`)으로 버전 관리만 받는다.

## Tech Stack

- Java 25, Spring Boot 4.0.3, Gradle 9.3.1
- Spring Data JPA, Spring Security, Spring Validation
- MySQL (운영), H2 (로컬/테스트)
- Lombok

## AI 코딩 및 협업 핵심 가이드라인

1. **'왜(Why)'를 먼저 설명할 것**
    - 코드를 제안하거나 기술을 선택할 때 "그냥 되는 코드"를 주지 마세요.
    - 왜 이 설계가 객체지향적인지, 유지보수 측면에서 어떤 이점이 있는지 근거를 반드시 먼저 설명하세요.

2. **풍부한 도메인 모델 (Rich Domain Model)**
    - 비즈니스 로직을 Service 계층에 몰아넣는 트랜잭션 스크립트(Transaction Script) 패턴을 극도로 지양합니다.
    - 데이터와 행위를 함께 가지는 객체지향적인 Entity 설계를 최우선으로 고려하세요.

3. **성능 최적화 및 DB 접근**
    - JPA 사용 시 발생할 수 있는 N+1 문제, 지연 로딩(Lazy Loading) 이슈를 항상 방어하는 코드를 작성하세요.
    - 필요한 경우 쿼리 튜닝이나 캐시 전략(Redis 등)에 대한 조언을 아끼지 마세요.

4. **테스트 코드 우선**
    - 비즈니스 로직을 작성한 후에는 반드시 해당 로직을 검증하는 단위 테스트 또는 통합 테스트 작성을 제안하세요.

5. **Git Workflow**
    - 커밋 메시지는 Conventional Commits 규약을 따릅니다.
    - PR 생성 시, 설계의 타당성이 잘 드러나도록 작성하세요.
    - 작업은 반드시 브랜치를 분리해서 하세요. (Github Workflow)