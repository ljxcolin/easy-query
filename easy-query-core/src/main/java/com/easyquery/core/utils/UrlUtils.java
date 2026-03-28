package com.easyquery.core.utils;

public class UrlUtils {

    public static String extractDatabase(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }

        if (url.contains("mysql")) {
            return extractMySqlSchema(url);
        } else if (url.contains("postgresql")) {
            return extractPostgreSqlSchema(url);
        } else if (url.contains("oracle")) {
            return extractOracleSchema(url);
        } else if (url.contains("sqlserver")) {
            return extractSqlServerSchema(url);
        }

        return null;
    }

    private static String extractMySqlSchema(String url) {
        int protocolIndex = url.indexOf("://");
        if (protocolIndex == -1) return null;
        int start = url.indexOf("/", protocolIndex + 3);
        if (start == -1) return null;
        int end = url.indexOf("?", start + 1);
        if (end == -1) {
            return url.substring(start + 1);
        }
        return url.substring(start + 1, end);
    }

    private static String extractPostgreSqlSchema(String url) {
        int start = url.lastIndexOf("/");
        if (start == -1) return null;
        int end = url.indexOf("?", start + 1);
        if (end == -1) {
            return url.substring(start + 1);
        }
        return url.substring(start + 1, end);
    }

    private static String extractOracleSchema(String url) {
        int atIndex = url.indexOf("@");
        if (atIndex == -1) return null;
        String afterAt = url.substring(atIndex + 1);
        int colonIndex = afterAt.lastIndexOf(":");
        if (colonIndex == -1) return null;
        String schema = afterAt.substring(colonIndex + 1);
        int semicolonIndex = schema.indexOf(";");
        if (semicolonIndex != -1) {
            schema = schema.substring(0, semicolonIndex);
        }
        return schema;
    }

    private static String extractSqlServerSchema(String url) {
        String databaseName = "databaseName=";
        int index = url.indexOf(databaseName);
        if (index == -1) return null;
        int start = index + databaseName.length();
        int end = url.indexOf(";", start);
        if (end == -1) {
            return url.substring(start);
        }
        return url.substring(start, end);
    }
}
