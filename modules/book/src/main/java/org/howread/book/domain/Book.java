package org.howread.book.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.howread.shared.entity.BaseEntity;

import java.time.LocalDate;

/**
 * Book 도메인 엔티티 (Rich Domain Model).
 *
 * ISBN을 유일 식별자로 사용하여 외부 API와 내부 DB 간 중복 등록을 방지한다.
 * 책 정보는 불변에 가깝기 때문에 별도의 변경 메서드를 두지 않는다.
 */
@Entity
@Table(name = "books", indexes = {
        @Index(name = "idx_books_isbn", columnList = "isbn", unique = true)
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column
    private String publisher;

    @Column
    private LocalDate publishedDate;

    /** 카카오 API에서 받은 표지 이미지 URL */
    @Column
    private String thumbnailUrl;

    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 외부 API 검색 결과로부터 Book을 생성하는 팩토리 메서드.
     * 생성 규칙을 한 곳에서 관리하여 일관성을 보장한다.
     */
    public static Book create(String isbn, String title, String author,
                              String publisher, LocalDate publishedDate,
                              String thumbnailUrl, String description) {
        Book book = new Book();
        book.isbn = isbn;
        book.title = title;
        book.author = author;
        book.publisher = publisher;
        book.publishedDate = publishedDate;
        book.thumbnailUrl = thumbnailUrl;
        book.description = description;
        return book;
    }
}
