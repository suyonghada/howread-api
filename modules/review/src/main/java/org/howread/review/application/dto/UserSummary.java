package org.howread.review.application.dto;

/**
 * 리뷰 응답에 포함할 작성자 요약 정보.
 *
 * review 모듈이 user 모듈을 직접 참조하지 않도록
 * UserInfoPort를 통해 infra 계층에서 조회한 결과를 이 VO로 전달받는다.
 */
public record UserSummary(
        Long id,
        String nickname,
        String profileImageUrl
) {
}
