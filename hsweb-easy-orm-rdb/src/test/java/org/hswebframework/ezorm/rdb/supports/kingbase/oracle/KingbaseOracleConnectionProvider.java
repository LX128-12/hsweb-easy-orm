package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import lombok.SneakyThrows;
import org.hswebframework.ezorm.rdb.ConnectionProvider;

import java.sql.Connection;
import java.sql.DriverManager;

public class KingbaseOracleConnectionProvider implements ConnectionProvider {

    static final String DEFAULT_DRIVER = "com.kingbase8.Driver";
    static final String DEFAULT_SCHEMA = "public";

    @SneakyThrows
    public KingbaseOracleConnectionProvider() {
        Class.forName(getConfig(
            "kingbase.oracle.driver",
            "KINGBASE_ORACLE_DRIVER",
            "kingbase.driver",
            "KINGBASE_DRIVER",
            DEFAULT_DRIVER,
            false
        ));
    }

    public static String getSchema() {
        return getConfig(
            "kingbase.oracle.schema",
            "KINGBASE_ORACLE_SCHEMA",
            "kingbase.schema",
            "KINGBASE_SCHEMA",
            DEFAULT_SCHEMA,
            false
        );
    }

    @Override
    @SneakyThrows
    public Connection getConnection() {
        return DriverManager.getConnection(
            getConfig("kingbase.oracle.url", "KINGBASE_ORACLE_URL", "kingbase.url", "KINGBASE_URL", null, true),
            getConfig("kingbase.oracle.username", "KINGBASE_ORACLE_USERNAME", "kingbase.username", "KINGBASE_USERNAME", null, true),
            getConfig("kingbase.oracle.password", "KINGBASE_ORACLE_PASSWORD", "kingbase.password", "KINGBASE_PASSWORD", null, true)
        );
    }

    @Override
    @SneakyThrows
    public void releaseConnect(Connection connection) {
        connection.close();
    }

    private static String getConfig(String property,
                                    String env,
                                    String fallbackProperty,
                                    String fallbackEnv,
                                    String defaultValue,
                                    boolean required) {
        String value = System.getProperty(property);
        if (value == null || value.isEmpty()) {
            value = System.getenv(env);
        }
        if ((value == null || value.isEmpty()) && fallbackProperty != null) {
            value = System.getProperty(fallbackProperty);
        }
        if ((value == null || value.isEmpty()) && fallbackEnv != null) {
            value = System.getenv(fallbackEnv);
        }
        if ((value == null || value.isEmpty()) && required) {
            throw new IllegalStateException(
                "Missing Kingbase Oracle test config: set -D" + property + "=... or env " + env
            );
        }
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}