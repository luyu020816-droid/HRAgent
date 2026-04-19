package com.ai.hragent;

import org.dromara.dynamictp.spring.annotation.EnableDynamicTp;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.ai.hragent.mapper")
@EnableDynamicTp
public class HrAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(HrAgentApplication.class, args);
    }

}
