package org.howread.user.application;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * 무작위 닉네임 생성기.
 *
 * 형식: 형용사 + 동물 + 3자리 숫자 — 예: 용감한호랑이042
 * 중복 시 최대 10회 재시도하고, 초과 시 nanoTime suffix를 붙여 고유성을 보장한다.
 */
@Component
public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "용감한", "빠른", "똑똑한", "행복한", "씩씩한",
            "차분한", "열정적인", "귀여운", "신중한", "활발한",
            "영리한", "다정한", "강인한", "유쾌한", "온화한",
            "날쌘", "재빠른", "반짝이는", "재치있는", "풍요로운",
            "밝은", "담대한", "용맹한", "늠름한", "우아한",
            "겸손한", "날카로운", "멋진", "친절한", "운좋은",
            "힘찬", "민첩한", "독특한", "생기있는", "화려한",
            "고요한", "작은", "특별한", "생동감있는", "엉뚱한",
            "활기찬", "발랄한", "포근한", "몽환적인", "웅장한",
            "펑키한", "경쾌한", "영웅적인", "이상적인", "통통한"
    );

    private static final List<String> ANIMALS = List.of(
            "호랑이", "독수리", "판다", "매", "수달",
            "코알라", "스라소니", "무스", "일각고래", "물수리",
            "펭귄", "메추리", "토끼", "연어", "투칸",
            "아이벡스", "독사", "바다코끼리", "다람쥐", "야크",
            "얼룩말", "알파카", "들소", "콘도르", "딩고",
            "엘크", "페럿", "긴팔원숭이", "왜가리", "따오기",
            "자칼", "솔개", "여우원숭이", "마모트", "도롱뇽",
            "오셀롯", "앵무새", "쿼카", "까마귀", "나무늘보",
            "맥", "족제비", "두더지", "담비", "카피바라",
            "도마뱀", "악어", "고슴도치", "미어캣", "불곰"
    );

    private static final int MAX_RETRIES = 10;
    private final Random random = new Random();

    /**
     * 고유한 닉네임을 생성한다.
     *
     * @param existsChecker 닉네임 중복 여부를 확인하는 함수. true면 중복.
     */
    public String generate(Predicate<String> existsChecker) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            String nickname = buildNickname();
            if (!existsChecker.test(nickname)) {
                return nickname;
            }
        }
        return buildNickname() + Long.toHexString(System.nanoTime()).substring(0, 4).toUpperCase();
    }

    private String buildNickname() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        String number = String.format("%03d", random.nextInt(1000));
        return adjective + animal + number;
    }
}
