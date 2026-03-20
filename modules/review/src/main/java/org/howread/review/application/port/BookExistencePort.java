package org.howread.review.application.port;

/**
 * Book 도메인의 존재 여부를 확인하는 Port.
 *
 * review 모듈이 book 모듈을 직접 의존하지 않도록 Port 인터페이스를 정의하고,
 * 구현체(Adapter)는 infra 모듈에 위치한다.
 */
public interface BookExistencePort {

    boolean existsById(Long bookId);
}
