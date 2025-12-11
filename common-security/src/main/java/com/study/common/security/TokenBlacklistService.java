package com.study.common.security;

public interface TokenBlacklistService {
    boolean isBlacklisted(String token);
}
