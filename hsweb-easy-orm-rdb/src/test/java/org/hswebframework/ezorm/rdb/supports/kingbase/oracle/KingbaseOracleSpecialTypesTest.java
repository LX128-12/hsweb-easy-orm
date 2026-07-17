package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseOracleSpecialTypesTest extends AbstractKingbaseOracleJdbcTestSupport {

    @Test
    public void testDecimalRoundtrip() {
        String table = randomName("test_decimal_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, amount number(18,4))");
        try {
            DatabaseOperator operator = newOperator(currentSchema());
            operator.dml()
                .insert(table)
                .value("id", 1)
                .value("amount", new BigDecimal("12345.6789"))
                .execute()
                .sync();

            Map<String, Object> row = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 1))
                .fetch(singleMap())
                .sync();
            Assert.assertNotNull(row.get("amount"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testBooleanRoundtrip() {
        String table = randomName("test_bool_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, active boolean)");
        try {
            DatabaseOperator operator = newOperator(currentSchema());
            operator.dml()
                .insert(table)
                .value("id", 1)
                .value("active", true)
                .execute()
                .sync();
            operator.dml()
                .insert(table)
                .value("id", 2)
                .value("active", false)
                .execute()
                .sync();

            Map<String, Object> rowTrue = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 1))
                .fetch(singleMap())
                .sync();
            Assert.assertEquals(Boolean.TRUE, rowTrue.get("active"));

            Map<String, Object> rowFalse = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 2))
                .fetch(singleMap())
                .sync();
            Assert.assertEquals(Boolean.FALSE, rowFalse.get("active"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testClobRoundtrip() {
        String table = randomName("test_clob_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, content clob)");
        try {
            DatabaseOperator operator = newOperator(currentSchema());
            String largeContent = generateLargeString(5000);
            operator.dml()
                .insert(table)
                .value("id", 1)
                .value("content", largeContent)
                .execute()
                .sync();

            Map<String, Object> row = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 1))
                .fetch(singleMap())
                .sync();
            Assert.assertNotNull(row.get("content"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testTimestampRoundtrip() {
        String table = randomName("test_ts_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, created_at timestamp)");
        try {
            java.sql.Timestamp ts = java.sql.Timestamp.valueOf("2025-01-15 10:30:00");
            DatabaseOperator operator = newOperator(currentSchema());
            operator.dml()
                .insert(table)
                .value("id", 1)
                .value("created_at", ts)
                .execute()
                .sync();

            Map<String, Object> row = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 1))
                .fetch(singleMap())
                .sync();
            Assert.assertNotNull(row.get("created_at"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return new BigDecimal(value.toString());
        }
        return null;
    }

    private String generateLargeString(int size) {
        StringBuilder sb = new StringBuilder(size);
        for (int i = 0; i < size; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        return sb.toString();
    }
}
