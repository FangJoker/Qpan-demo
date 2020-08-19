package cn.chavez.qpan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/14 13:17
 */
@EnableDiscoveryClient
@EnableFeignClients
@SpringBootApplication
@EnableHystrix       //断路器
public class Application  implements WebMvcConfigurer {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
