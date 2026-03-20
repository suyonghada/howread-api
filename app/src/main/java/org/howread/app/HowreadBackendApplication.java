package org.howread.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(scanBasePackages = "org.howread")
public class HowreadBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(HowreadBackendApplication.class, args);
    }

}
