package org.howread.user.application.port;

import org.springframework.web.multipart.MultipartFile;

/**
 * [Port] 프로필 이미지 저장소 계약.
 *
 * 도메인이 S3 등 구체적인 스토리지 기술에 의존하지 않도록 인터페이스로 추상화한다.
 * 구현체는 infra 모듈의 S3Service에 위치한다.
 */
public interface ProfileImagePort {

    /**
     * 프로필 이미지를 업로드하고 S3 오브젝트 키를 반환한다.
     *
     * @param userId 이미지를 소유한 사용자 ID (S3 key 경로 구성에 사용)
     * @param file   업로드할 이미지 파일
     * @return S3 오브젝트 키 (예: "profile/42/uuid.jpg")
     */
    String upload(Long userId, MultipartFile file);

    /**
     * S3 오브젝트 키로 이미지를 삭제한다.
     *
     * @param key S3 오브젝트 키 (예: "profile/42/uuid.jpg")
     */
    void delete(String key);

    /**
     * S3 오브젝트 키로 CloudFront URL을 생성한다.
     *
     * @param key S3 오브젝트 키 (예: "profile/42/uuid.jpg")
     * @return CloudFront URL
     */
    String buildUrl(String key);
}
