package dev.ulisses.highperformanceapi.domain.enums;

public enum AuditAction {

    CREATE,

    UPDATE,

    DELETE,

    LOGIN,

    LOGOUT,

    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    ACCOUNT_LOCKED,
    ACCOUNT_UNLOCKED,
    REFRESH_TOKEN_CREATED,
    REFRESH_TOKEN_ROTATED,
    REFRESH_TOKEN_REVOKED,
    RATE_LIMIT_EXCEEDED
}
