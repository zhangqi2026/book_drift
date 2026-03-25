package com.book_drift;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.book_drift.mapper")
public class BookDriftApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookDriftApplication.class, args);
    }

}
