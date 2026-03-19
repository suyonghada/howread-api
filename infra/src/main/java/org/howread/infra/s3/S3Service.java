package org.howread.infra.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.howread.common.exception.BusinessException;
import org.howread.user.application.UserErrorCode;
import org.howread.user.application.port.ProfileImagePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * [Adapter] ProfileImagePort를 AWS S3로 구현.
 *
 * 업로드 흐름: 파일 검증 → Thumbnailator 리사이징(최대 800×800, JPEG) → S3 PutObject → S3 key 반환.
 * 리사이징 이유: 프로필 이미지는 표시 크기가 작으므로 800×800으로 제한하여 S3 저장 비용과
 * 클라이언트 로딩 시간을 절감한다. 원본이 이미 작으면 불필요한 재인코딩을 건너뛴다.
 *
 * 기술 교체 시 이 클래스만 변경하면 된다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service implements ProfileImagePort {

    private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10MB
    private static final int MAX_DIMENSION = 800;
    private static final float JPEG_QUALITY = 0.85f;

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucket;

    @Value("${aws.cloudfront.domain}")
    private String cloudfrontDomain;

    /**
     * 이미지를 검증·리사이징 후 S3에 업로드하고 S3 오브젝트 키를 반환한다.
     *
     * @return S3 오브젝트 키 (예: "profile/42/550e8400-...jpg")
     */
    @Override
    public String upload(Long userId, MultipartFile file) {
        validate(file);

        byte[] imageBytes = resize(file);
        String key = "profile/" + userId + "/" + UUID.randomUUID() + ".jpg";

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType("image/jpeg")
                .contentLength((long) imageBytes.length)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromInputStream(
                new ByteArrayInputStream(imageBytes), imageBytes.length));

        log.info("프로필 이미지 업로드 완료: key={}", key);
        return key;
    }

    @Override
    public void delete(String key) {
        s3Client.deleteObject(DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build());
        log.info("프로필 이미지 삭제 완료: key={}", key);
    }

    @Override
    public String buildUrl(String key) {
        return "https://" + cloudfrontDomain + "/" + key;
    }

    private void validate(MultipartFile file) {
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException(UserErrorCode.FILE_TOO_LARGE);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException(UserErrorCode.INVALID_FILE_TYPE);
        }
    }

    /**
     * 이미지를 최대 800×800으로 리사이징하여 JPEG 바이트 배열로 반환한다.
     * 원본이 이미 800×800 이하인 경우에도 JPEG로 재인코딩하여 포맷을 통일한다.
     */
    private byte[] resize(MultipartFile file) {
        try (var out = new ByteArrayOutputStream()) {
            Thumbnails.of(file.getInputStream())
                    .size(MAX_DIMENSION, MAX_DIMENSION)
                    .keepAspectRatio(true)
                    .outputFormat("jpg")
                    .outputQuality(JPEG_QUALITY)
                    .toOutputStream(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("이미지 리사이징 실패", e);
            throw new RuntimeException("이미지 처리 중 오류가 발생했습니다.", e);
        }
    }
}
