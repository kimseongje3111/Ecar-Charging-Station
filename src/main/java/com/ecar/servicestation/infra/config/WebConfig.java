package com.ecar.servicestation.infra.config;

import net.rakugakibox.util.YamlResourceBundle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static class YamlMessageSource extends ResourceBundleMessageSource {     // 다국어 처리
        @Override
        protected ResourceBundle doGetBundle(String basename, Locale locale) throws MissingResourceException {
            return ResourceBundle.getBundle(basename, locale, YamlResourceBundle.Control.INSTANCE);
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    public LocaleResolver localeResolver() {        // 세션 지역 설정
        SessionLocaleResolver sessionLocaleResolver = new SessionLocaleResolver();
        sessionLocaleResolver.setDefaultLocale(Locale.KOREAN);

        return sessionLocaleResolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {      // Interceptor(지역 설정 변경)
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");

        return localeChangeInterceptor;
    }

    @Bean
    public MessageSource messageSource(     // 메시지 다국어 처리를 위한 .yml 참조
            @Value("${spring.messages.basename}") String baseName,
            @Value("${spring.messages.encoding}") String encoding) {

        YamlMessageSource yamlMessageSource = new YamlMessageSource();

        yamlMessageSource.setBasename(baseName);
        yamlMessageSource.setDefaultEncoding(encoding);
        yamlMessageSource.setAlwaysUseMessageFormat(true);
        yamlMessageSource.setUseCodeAsDefaultMessage(true);
        yamlMessageSource.setFallbackToSystemLocale(true);

        return yamlMessageSource;
    }
}
