package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.metadata.DataType;
import org.hswebframework.ezorm.rdb.metadata.JdbcDataType;
import org.hswebframework.ezorm.rdb.supports.mssql.SqlServerDialect;

import java.sql.JDBCType;

public class KingbaseMssqlDialect extends SqlServerDialect {

    public KingbaseMssqlDialect() {
        // Kingbase JDBC driver (PG-based) sends PG wire-protocol types for parameters.
        // Override MSSQL types that don't match: boolean for Boolean, varchar for String.
        addDataTypeBuilder(JDBCType.BOOLEAN, (meta) -> "boolean");
        addDataTypeBuilder(JDBCType.BIT, (meta) -> "boolean");
        addDataTypeBuilder(JDBCType.VARCHAR, (meta) -> "varchar(" + meta.getLength(255) + ")");
        addDataTypeBuilder(JDBCType.NVARCHAR, (meta) -> "varchar(" + meta.getLength(255) + ")");

        // Register PG native type names returned by the JDBC driver metadata
        registerDataType("character varying", DataType.builder(JdbcDataType.of(JDBCType.VARCHAR, String.class),
                                                               column -> "varchar(" + column.getLength(255) + ")"));
        registerDataType("character", DataType.builder(JdbcDataType.of(JDBCType.CHAR, String.class),
                                                       column -> "char(" + column.getLength(255) + ")"));
        registerDataType("integer", DataType.builder(JdbcDataType.of(JDBCType.INTEGER, Integer.class),
                                                     column -> "integer"));
        registerDataType("smallint", DataType.builder(JdbcDataType.of(JDBCType.SMALLINT, Short.class),
                                                      column -> "smallint"));
        registerDataType("bigint", DataType.builder(JdbcDataType.of(JDBCType.BIGINT, Long.class),
                                                    column -> "bigint"));
        registerDataType("boolean", DataType.builder(JdbcDataType.of(JDBCType.BOOLEAN, Boolean.class),
                                                     column -> "boolean"));
        registerDataType("double precision", DataType.builder(JdbcDataType.of(JDBCType.DOUBLE, Double.class),
                                                              column -> "double precision"));
        registerDataType("real", DataType.builder(JdbcDataType.of(JDBCType.FLOAT, Float.class),
                                                  column -> "real"));
        registerDataType("timestamp without time zone", DataType.builder(JdbcDataType.of(JDBCType.TIMESTAMP, java.sql.Timestamp.class),
                                                                         column -> "datetime2"));
        registerDataType("time without time zone", DataType.builder(JdbcDataType.of(JDBCType.TIME, java.sql.Time.class),
                                                                    column -> "time"));
        registerDataType("numeric", DataType.builder(JdbcDataType.of(JDBCType.NUMERIC, java.math.BigDecimal.class),
                                                     column -> "numeric(" + column.getPrecision(38) + "," + column.getScale() + ")"));
    }

    @Override
    public String getQuoteStart() {
        return "\"";
    }

    @Override
    public String getQuoteEnd() {
        return "\"";
    }

    @Override
    public String getId() {
        return "kingbase-mssql";
    }

    @Override
    public String getName() {
        return "KingbaseMssql";
    }
}
