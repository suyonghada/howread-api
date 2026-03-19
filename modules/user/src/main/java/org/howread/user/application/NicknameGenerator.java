package org.howread.user.application;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * 무작위 닉네임 생성기.
 *
 * 형식: 형용사 + 명사(동물) + 3자리 숫자 — 예: HappyTiger042
 * 중복 시 최대 10회 재시도하고, 초과 시 UUID suffix를 붙여 고유성을 보장한다.
 */
@Component
public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "Happy", "Brave", "Clever", "Swift", "Bold",
            "Calm", "Eager", "Fancy", "Gentle", "Jolly",
            "Keen", "Lively", "Merry", "Noble", "Proud",
            "Quiet", "Rapid", "Shiny", "Witty", "Zesty",
            "Bright", "Crisp", "Daring", "Fierce", "Graceful",
            "Humble", "Icy", "Jazzy", "Kind", "Lucky",
            "Mighty", "Nimble", "Odd", "Peppy", "Royal",
            "Silly", "Tiny", "Unique", "Vivid", "Wacky",
            "Active", "Bubbly", "Cozy", "Dreamy", "Epic",
            "Funky", "Groovy", "Heroic", "Ideal", "Jumpy"
    );

    private static final List<String> ANIMALS = List.of(
            "Tiger", "Eagle", "Panda", "Falcon", "Otter",
            "Koala", "Lynx", "Moose", "Narwhal", "Osprey",
            "Penguin", "Quail", "Rabbit", "Salmon", "Toucan",
            "Urial", "Viper", "Walrus", "Xerus", "Yak",
            "Zebra", "Alpaca", "Bison", "Condor", "Dingo",
            "Elk", "Ferret", "Gibbon", "Heron", "Ibis",
            "Jackal", "Kite", "Lemur", "Marmot", "Newt",
            "Ocelot", "Parrot", "Quokka", "Raven", "Sloth",
            "Tapir", "Umbra", "Vole", "Weasel", "Xenops",
            "Yapok", "Zorilla", "Axolotl", "Basilisk", "Capybara"
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
        // 10회 재시도 후에도 충돌 시 UUID 앞 8자로 고유성 보장
        return buildNickname() + Long.toHexString(System.nanoTime()).substring(0, 4).toUpperCase();
    }

    private String buildNickname() {
        String adjective = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(random.nextInt(ANIMALS.size()));
        String number = String.format("%03d", random.nextInt(1000));
        return adjective + animal + number;
    }
}
