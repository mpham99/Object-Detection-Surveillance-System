package com.jetson.rest.response;

import com.jetson.rest.security.JwtTokenUtil;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AuthResponse implements Serializable {

    private static final long serialVersionUID = -7578367699137373998L;

    private String accessToken;
    private long expiration = JwtTokenUtil.JWT_TOKEN_VALIDITY;
    private String refreshToken;
}
