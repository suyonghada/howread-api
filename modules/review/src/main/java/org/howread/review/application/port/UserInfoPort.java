package org.howread.review.application.port;

import org.howread.review.application.dto.UserSummary;

import java.util.Collection;
import java.util.Map;

/**
 * [Port] 리뷰 작성자 정보 조회 계약.
 *
 * review 모듈이 user 모듈에 직접 의존하지 않도록 포트로 추상화한다.
 * 구현체는 infra 모듈의 UserInfoPortAdapter에 위치한다.
 */
public interface UserInfoPort {
    Map<Long, UserSummary> findSummariesByIds(Collection<Long> userIds);
}
