package org.howread.infra.book;

import org.howread.book.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookJpaRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    List<Book> findByTitleContaining(String keyword);

    /**
     * 커서 기반 페이지네이션 쿼리.
     *
     * cursorId가 null이면 전체에서 최신순 size개,
     * cursorId가 있으면 id < cursorId 조건으로 최신순 size개를 반환한다.
     *
     * JPQL의 IS NULL / 조건 분기를 한 쿼리로 처리하기 위해 @Query를 사용한다.
     */
    @Query("SELECT b FROM Book b WHERE (:cursorId IS NULL OR b.id < :cursorId) ORDER BY b.id DESC LIMIT :size")
    List<Book> findBooksBeforeCursor(@Param("cursorId") Long cursorId, @Param("size") int size);
}
