package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import lombok.SneakyThrows;
import org.hswebframework.ezorm.rdb.ConnectionProvider;

import java.sql.Connection;
import java.sql.DriverManager;

public class KingbaseMssqlConnectionProvider implements ConnectionProvider {

    static final String DEFAULT_DRIVER = "com.kingbase8.Driver";
    static final String DEFAULT_SCHEMA = "public";

    @SneakyThrows
    public KingbaseMssqlConnectionProvider() {
        Class.forName(getConfig(
            "kingbase.mssql.driver",
            "KINGBASE_MSSQL_DRIVER",
            "kingbase.driver",
            "KINGBASE_DRIVER",
            DEFAULT_DRIVER,
            false
        ));
    }

    public static String getSchema() {
        return getConfig(
            "kingbase.mssql.schema",
            "KINGBASE_MSSQL_SCHEMA",
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
            getConfig("kingbase.mssql.url", "KINGBASE_MSSQL_URL", "kingbase.url", "KINGBASE_URL", null, true),
            getConfig("kingbase.mssql.username", "KINGBASE_MSSQL_USERNAME", "kingbase.username", "KINGBASE_USERNAME", null, true),
            getConfig("kingbase.mssql.password", "KINGBASE_MSSQL_PASSWORD", "kingbase.password", "KINGBASE_PASSWORD", null, true)
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
                "Missing Kingbase MSSQL test config: set -D" + property + "=... or env " + env
            );
        }
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return value;
    }
}
