package org.howread.common.event;

import java.time.LocalDateTime;

/**
 * EDA(Event-Driven Architecture) 마커 인터페이스.
 *
 * 모든 도메인 이벤트는 이 인터페이스를 구현한다.
 * 현재는 ApplicationEventPublisher로 발행하고, 추후 Kafka로 교체 시
 * 이 인터페이스를 구현한 클래스를 그대로 사용할 수 있도록 추상화 계층을 유지한다.
 *
 * 모듈 간 직접 의존 대신 이벤트를 통해 결합을 최소화한다.
 * 예: ReviewCreatedEvent 발행 → BookStatisticsUpdatedEvent 구독
 */
public interface DomainEvent {

    /**
     * 이벤트 발생 시각. 이벤트 소싱 및 감사(Audit) 추적에 활용한다.
     */
    LocalDateTime occurredAt();
}
