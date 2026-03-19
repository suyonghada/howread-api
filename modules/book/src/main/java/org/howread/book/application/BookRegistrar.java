package org.howread.book.application;

import lombok.RequiredArgsConstructor;
import org.howread.book.application.dto.BookResponse;
import org.howread.book.application.dto.BookSearchResult;
import org.howread.book.application.port.BookRepository;
import org.howread.book.domain.Book;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Book 등록(find-or-create)을 전담하는 Application 컴포넌트.
 *
 * BookService.registerBook()은 외부 API 호출을 트랜잭션 밖에서 수행해야 한다.
 * 이후 DB 저장은 별도의 쓰기 트랜잭션이 필요한데, 같은 빈의 메서드를 self-call하면
 * Spring AOP 프록시를 우회하여 @Transactional이 무시된다.
 * 책임을 이 컴포넌트로 분리함으로써 의존성을 명시적으로 드러내고
 * 트랜잭션 경계도 프록시를 통해 정상 동작하게 한다.
 */
@Component
@RequiredArgsConstructor
public class BookRegistrar {

    private final BookRepository bookRepository;

    /**
     * BookSearchResult를 DB에 저장한다 (idempotent).
     *
     * 동시 요청에 의한 중복 등록을 방어하기 위해 저장 직전에 ISBN을 재확인한다.
     */
    @Transactional
    public BookResponse register(BookSearchResult result) {
        return bookRepository.findByIsbn(result.isbn())
                .map(BookResponse::from)
                .orElseGet(() -> {
                    Book book = Book.create(
                            result.isbn(), result.title(), result.author(),
                            result.publisher(), result.publishedDate(),
                            result.thumbnailUrl(), result.description()
                    );
                    return BookResponse.from(bookRepository.save(book));
                });
    }
}
