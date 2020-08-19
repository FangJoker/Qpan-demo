package cn.chavez.qpan;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import java.util.List;

/**
 * @Author: ChavezQiu
 * @description:
 * @Date: 2020/4/14 13:19
 */
@Configuration
public class RestTemplateConfig extends WebMvcConfigurationSupport {

    @Bean
    @LoadBalanced         //负载均衡
    RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
//        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        //添加上GSON的转换器
//        messageConverters.add(7, new GsonHttpMessageConverter());
        return restTemplate;
    }


}
