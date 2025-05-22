package com.unipay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@EnableJpaAuditing
@SpringBootApplication
@EnableAspectJAutoProxy
public class UniPayApplication {

   public static void main(String[] args) {
        SpringApplication.run(UniPayApplication.class, args);
    }
}