package org.howread.infra.book;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.howread.book.application.dto.BookSearchCondition;
import org.howread.book.domain.Book;
import org.howread.book.domain.QBook;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * QueryDSL 기반 Book 검색 레포지토리.
 *
 * Spring Data JPA의 @Query로는 동적 조건(조건 조합이 런타임에 결정)을 표현하기 어렵다.
 * QueryDSL은 타입 세이프한 조건 빌더를 제공하므로 동적 쿼리에 적합하다.
 * null인 조건은 자동으로 무시되어 조건 조합이 자유롭다.
 */
@Repository
@RequiredArgsConstructor
public class BookQueryRepository {

    private final JPAQueryFactory queryFactory;
    private static final QBook book = QBook.book;

    public List<Book> searchBooks(BookSearchCondition condition, Long cursorId, int size) {
        return queryFactory
                .selectFrom(book)
                .where(
                        cursorCondition(cursorId),
                        titleContains(condition.title()),
                        authorContains(condition.author()),
                        isbnEq(condition.isbn())
                )
                .orderBy(book.id.desc())
                .limit(size)
                .fetch();
    }

    /** cursorId보다 작은 id만 조회 (null이면 조건 없음 = 첫 페이지) */
    private BooleanExpression cursorCondition(Long cursorId) {
        return cursorId != null ? book.id.lt(cursorId) : null;
    }

    /** 제목 부분 일치 (null 또는 빈 문자열이면 조건 제외) */
    private BooleanExpression titleContains(String title) {
        return (title != null && !title.isBlank()) ? book.title.contains(title) : null;
    }

    /** 저자 부분 일치 */
    private BooleanExpression authorContains(String author) {
        return (author != null && !author.isBlank()) ? book.author.contains(author) : null;
    }

    /** ISBN 정확히 일치 */
    private BooleanExpression isbnEq(String isbn) {
        return (isbn != null && !isbn.isBlank()) ? book.isbn.eq(isbn) : null;
    }
}
