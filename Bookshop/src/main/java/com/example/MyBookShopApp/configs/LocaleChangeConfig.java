package com.example.MyBookShopApp.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Enumeration;
import java.util.Locale;

@Configuration
public class LocaleChangeConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver("lang");
        resolver.setCookieHttpOnly(true);
        resolver.setDefaultLocaleFunction(request -> {
            Enumeration<Locale> enumeration = request.getLocales();
            while (enumeration.hasMoreElements()) {
                switch (enumeration.nextElement().getLanguage()) {
                    case "en":
                        return Locale.ENGLISH;
                    case "ru":
                        return new Locale("ru");
                }
            }
            return Locale.ENGLISH;
        });
        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor localeChangeInterceptor = new LocaleChangeInterceptor();
        localeChangeInterceptor.setParamName("lang");
        return localeChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
