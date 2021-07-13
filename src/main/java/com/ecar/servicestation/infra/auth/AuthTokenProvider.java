package com.ecar.servicestation.infra.auth;

import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface AuthTokenProvider {

    String createToken(String userPk, List<String> roles);

    Authentication getAuthentication(String token);

    String getUserPk(String token);

    String resolveToken(HttpServletRequest request);

    boolean validateToken(String token);

}
