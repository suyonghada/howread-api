package org.howread.infra.review;

import lombok.RequiredArgsConstructor;
import org.howread.infra.book.BookJpaRepository;
import org.howread.review.application.port.BookExistencePort;
import org.springframework.stereotype.Component;

/**
 * BookExistencePort의 JPA 구현체.
 *
 * review 모듈이 book 모듈을 직접 의존하지 않도록 infra에서 Port를 구현한다.
 * infra는 두 모듈 모두에 의존하므로 cross-domain 연결 역할을 수행하기에 적합하다.
 */
@Component
@RequiredArgsConstructor
public class BookExistenceAdapter implements BookExistencePort {

    private final BookJpaRepository bookJpaRepository;

    @Override
    public boolean existsById(Long bookId) {
        return bookJpaRepository.existsById(bookId);
    }
}
