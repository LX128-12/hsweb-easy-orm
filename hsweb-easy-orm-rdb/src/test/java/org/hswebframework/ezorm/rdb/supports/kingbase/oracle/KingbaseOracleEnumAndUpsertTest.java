package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseOracleEnumAndUpsertTest extends AbstractKingbaseOracleJdbcTestSupport {

    @Test
    public void testInsertMultipleRows() {
        String table = randomName("test_insert_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            DatabaseOperator operator = newOperator(currentSchema());
            operator.dml().insert(table).value("id", 1).value("name", "Alice").execute().sync();
            operator.dml().insert(table).value("id", 2).value("name", "Bob").execute().sync();
            operator.dml().insert(table).value("id", 3).value("name", "Charlie").execute().sync();

            List<Map<String, Object>> rows = operator.dml()
                .query(table)
                .orderBy(SortOrder.asc("id"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(3, rows.size());
            Assert.assertEquals("Alice", rows.get(0).get("name"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testUpsert() {
        String table = randomName("test_upsert_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64), score int)");
        try {
            DatabaseOperator operator = newOperator(currentSchema());

            operator.dml().insert(table).value("id", 1).value("name", "Alice").value("score", 100).execute().sync();
            operator.dml().upsert(table).value("id", 1).value("name", "Alice Updated").value("score", 200).execute().sync();
            operator.dml().upsert(table).value("id", 2).value("name", "Bob").value("score", 300).execute().sync();

            List<Map<String, Object>> rows = operator.dml()
                .query(table)
                .orderBy(SortOrder.asc("id"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, rows.size());
            Assert.assertEquals("Alice Updated", rows.get(0).get("name"));
            Assert.assertEquals(200, ((Number) rows.get(0).get("score")).intValue());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testEnumLikeQuery() {
        String table = randomName("test_enum_like_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, status varchar(16))");
        try {
            execute("insert into " + qualified + " values (1, 'ACTIVE')");
            execute("insert into " + qualified + " values (2, 'INACTIVE')");
            execute("insert into " + qualified + " values (3, 'PENDING')");
            execute("insert into " + qualified + " values (4, 'ACTIVE')");

            DatabaseOperator operator = newOperator(currentSchema());

            List<Map<String, Object>> activeRows = operator.dml()
                .query(table)
                .where(dsl -> dsl.in("status", Arrays.asList("ACTIVE", "PENDING")))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(3, activeRows.size());
        } finally {
            dropTableQuietly(qualified);
        }
    }
}
