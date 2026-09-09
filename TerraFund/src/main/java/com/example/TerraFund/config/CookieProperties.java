package com.example.TerraFund.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class CookieProperties {

    @Value("${application.security.cookies.secure:false}")
    private boolean secure;
}