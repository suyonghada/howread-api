package org.howread.book.application.dto;

import org.howread.book.domain.Book;

import java.time.LocalDate;

public record BookResponse(
        Long id,
        String isbn,
        String title,
        String author,
        String publisher,
        LocalDate publishedDate,
        String thumbnailUrl,
        String description,
        double averageRating,
        int ratingCount
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
                book.getId(),
                book.getIsbn(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getPublishedDate(),
                book.getThumbnailUrl(),
                book.getDescription(),
                book.getAverageRating(),
                book.getRatingCount()
        );
    }
}
