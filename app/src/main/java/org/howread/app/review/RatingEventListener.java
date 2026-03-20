package org.howread.app.review;

import lombok.RequiredArgsConstructor;
import org.howread.book.application.BookService;
import org.howread.review.event.RatingChangedEvent;
import org.howread.review.event.RatingCreatedEvent;
import org.howread.review.event.RatingDeletedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 별점 도메인 이벤트를 수신하여 Book 평균 별점 통계를 비동기로 갱신하는 이벤트 리스너.
 *
 * 비동기 구조 설계:
 * - @TransactionalEventListener(phase = AFTER_COMMIT): 별점 트랜잭션이 커밋된 후에 실행된다.
 *   커밋 전 실패 시에는 이벤트가 발행되지 않으므로 Book 통계가 오염되지 않는다.
 * - @Async: 별도 스레드에서 실행되어 별점 등록 응답이 Book 통계 갱신을 기다리지 않는다.
 *   Eventually Consistent 모델이다. Kafka 전환 시 이 리스너만 교체하면 된다.
 *
 * BookService의 각 메서드는 @Transactional(readOnly=false)이므로 비동기 스레드에서
 * 새로운 트랜잭션을 시작하여 Book 통계를 갱신한다.
 */
@Component
@RequiredArgsConstructor
public class RatingEventListener {

    private final BookService bookService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRatingCreated(RatingCreatedEvent event) {
        bookService.addRatingStats(event.bookId(), event.rating());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRatingDeleted(RatingDeletedEvent event) {
        bookService.removeRatingStats(event.bookId(), event.rating());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onRatingChanged(RatingChangedEvent event) {
        bookService.changeRating(event.bookId(), event.oldRating(), event.newRating());
    }
}
