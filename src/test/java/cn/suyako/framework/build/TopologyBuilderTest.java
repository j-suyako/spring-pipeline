package cn.suyako.framework.build;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("cn.suyako.framework")
class TopologyBuilderTest {
    public static void main(String[] args) {
        SpringApplication.run(TopologyBuilderTest.class, args);
    }
}