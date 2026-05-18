package com.itheima.reggie;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j
@SpringBootApplication
@MapperScan("com.itheima.reggie.mapper")
@ServletComponentScan
@EnableTransactionManagement //开启对事物管理的支持
public class ApplicationRunAction {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationRunAction.class, args);
        System.out.println("Hello World!");
    }
}
