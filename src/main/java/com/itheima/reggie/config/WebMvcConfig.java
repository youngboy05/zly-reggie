package com.itheima.reggie.config;

import com.itheima.reggie.common.JacksonObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /*
    默认情况下，maven的静态资源是必须放在 resources/static和 resources/template目录下，
    如果想修改，则需要自行设置静态资源映射；
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/backend/**").addResourceLocations("classpath:/backend/");
        registry.addResourceHandler("/front/**").addResourceLocations("classpath:/front/");
    }

    /*
     * 扩展WebMVC的消息转换器
     * WebMVC在于前端交互的过程中，有许多默认的消息转换器，包括 JSON <-> Java对象；
     * 为了将Java的Long型数据转为String。我们需要自定义一个消息转换器，并将其置于优先使用；
     */

    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        //1、创建消息转换器，该类由 WebMVC提供
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //2、设置对象转换器，底层是用Jackson将 java对象 转化为 json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //3、将消息转换器对象添加到 MVC提供的消息转换器集合中
        converters.add(0,messageConverter); // 将自己的转换器放在集合的首位，优先使用；
    }
}
