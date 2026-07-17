package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.dml.query.Selects;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseMssqlExtendedQueryTest extends AbstractKingbaseMssqlJdbcTestSupport {

    @Test
    public void testAggregateCount() {
        String table = randomName("test_agg_count_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64), amount int)");
        try {
            execute("insert into " + qualified + " values (1, 'a', 10)");
            execute("insert into " + qualified + " values (2, 'b', 20)");
            execute("insert into " + qualified + " values (3, 'c', 30)");

            DatabaseOperator operator = newOperator(currentSchema());

            List<Map<String, Object>> result = operator.dml()
                .query(table)
                .select(Selects.count("id").as("cnt_id"), Selects.count("name").as("cnt_name"))
                .fetch(mapList())
                .sync();

            Assert.assertEquals(1, result.size());
            Map<String, Object> row = result.get(0);
            Assert.assertEquals(3L, ((Number) row.get("cnt_id")).longValue());
            Assert.assertEquals(3L, ((Number) row.get("cnt_name")).longValue());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testAggregateSum() {
        String table = randomName("test_agg_sum_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, amount int)");
        try {
            execute("insert into " + qualified + " values (1, 100)");
            execute("insert into " + qualified + " values (2, 200)");
            execute("insert into " + qualified + " values (3, 300)");

            DatabaseOperator operator = newOperator(currentSchema());

            Map<String, Object> row = operator.dml()
                .query(table)
                .select(Selects.sum("amount").as("total_amount"))
                .fetch(singleMap())
                .sync();

            Assert.assertEquals(600L, ((Number) row.get("total_amount")).longValue());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testAggregateAvgMaxMin() {
        String table = randomName("test_agg_avg_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, score int)");
        try {
            execute("insert into " + qualified + " values (1, 10)");
            execute("insert into " + qualified + " values (2, 20)");
            execute("insert into " + qualified + " values (3, 30)");
            execute("insert into " + qualified + " values (4, 40)");

            DatabaseOperator operator = newOperator(currentSchema());

            Map<String, Object> row = operator.dml()
                .query(table)
                .select(Selects.avg("score").as("avg_score"),
                        Selects.max("score").as("max_score"),
                        Selects.min("score").as("min_score"))
                .fetch(singleMap())
                .sync();

            Assert.assertEquals(25.0, ((Number) row.get("avg_score")).doubleValue(), 0.01);
            Assert.assertEquals(40L, ((Number) row.get("max_score")).longValue());
            Assert.assertEquals(10L, ((Number) row.get("min_score")).longValue());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testGroupBy() {
        String table = randomName("test_groupby_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, category varchar(32), amount int)");
        try {
            execute("insert into " + qualified + " values (1, 'A', 10)");
            execute("insert into " + qualified + " values (2, 'A', 20)");
            execute("insert into " + qualified + " values (3, 'B', 100)");

            DatabaseOperator operator = newOperator(currentSchema());

            List<Map<String, Object>> result = operator.dml()
                .query(table)
                .select(Selects.column("category"),
                        Selects.count("id").as("cnt"),
                        Selects.sum("amount").as("total"))
                .groupBy(Selects.column("category").get())
                .fetch(mapList())
                .sync();

            Assert.assertEquals(2, result.size());

            Map<String, Object> catA = result.stream()
                .filter(m -> "A".equals(m.get("category")))
                .findFirst().orElseThrow(AssertionError::new);
            Assert.assertEquals(2L, ((Number) catA.get("cnt")).longValue());
            Assert.assertEquals(30L, ((Number) catA.get("total")).longValue());

            Map<String, Object> catB = result.stream()
                .filter(m -> "B".equals(m.get("category")))
                .findFirst().orElseThrow(AssertionError::new);
            Assert.assertEquals(1L, ((Number) catB.get("cnt")).longValue());
            Assert.assertEquals(100L, ((Number) catB.get("total")).longValue());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testLikeQuery() {
        String table = randomName("test_like_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            execute("insert into " + qualified + " values (1, 'hello_world')");
            execute("insert into " + qualified + " values (2, 'hello_mars')");
            execute("insert into " + qualified + " values (3, 'hi_world')");
            execute("insert into " + qualified + " values (4, 'bonjour')");

            DatabaseOperator operator = newOperator(currentSchema());

            List<Map<String, Object>> exact = operator.dml()
                .query(table)
                .where(dsl -> dsl.like("name", "hello_world"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(1, exact.size());

            List<Map<String, Object>> prefix = operator.dml()
                .query(table)
                .where(dsl -> dsl.like$("name", "hello"))
                .fetch(mapList())
                .sync();
            List<String> prefixNames = prefix.stream().map(m -> (String) m.get("name")).collect(Collectors.toList());
            Assert.assertEquals(2, prefixNames.size());
            Assert.assertTrue(prefixNames.contains("hello_world"));
            Assert.assertTrue(prefixNames.contains("hello_mars"));

            List<Map<String, Object>> suffix = operator.dml()
                .query(table)
                .where(dsl -> dsl.$like("name", "world"))
                .fetch(mapList())
                .sync();
            List<String> suffixNames = suffix.stream().map(m -> (String) m.get("name")).collect(Collectors.toList());
            Assert.assertEquals(2, suffixNames.size());
            Assert.assertTrue(suffixNames.contains("hello_world"));
            Assert.assertTrue(suffixNames.contains("hi_world"));

            List<Map<String, Object>> contains = operator.dml()
                .query(table)
                .where(dsl -> dsl.$like$("name", "hello"))
                .fetch(mapList())
                .sync();
            List<String> containsNames = contains.stream().map(m -> (String) m.get("name")).collect(Collectors.toList());
            Assert.assertEquals(2, containsNames.size());
            Assert.assertTrue(containsNames.contains("hello_world"));
            Assert.assertTrue(containsNames.contains("hello_mars"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testInNotIn() {
        String table = randomName("test_in_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            execute("insert into " + qualified + " values (1, 'Alice')");
            execute("insert into " + qualified + " values (2, 'Bob')");
            execute("insert into " + qualified + " values (3, 'Charlie')");
            execute("insert into " + qualified + " values (4, 'Diana')");

            DatabaseOperator operator = newOperator(currentSchema());

            List<Map<String, Object>> inResult = operator.dml()
                .query(table)
                .select("id", "name")
                .where(dsl -> dsl.in("id", Arrays.asList(1, 3)))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, inResult.size());
            List<String> inNames = inResult.stream().map(m -> (String) m.get("name")).collect(Collectors.toList());
            Assert.assertTrue(inNames.contains("Alice"));
            Assert.assertTrue(inNames.contains("Charlie"));

            List<Map<String, Object>> notInResult = operator.dml()
                .query(table)
                .select("id", "name")
                .where(dsl -> dsl.notIn("id", Arrays.asList(1, 3)))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, notInResult.size());
            List<String> notInNames = notInResult.stream().map(m -> (String) m.get("name")).collect(Collectors.toList());
            Assert.assertTrue(notInNames.contains("Bob"));
            Assert.assertTrue(notInNames.contains("Diana"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testIsNullNotNull() {
        String table = randomName("test_null_check_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64), remark varchar(128))");
        try {
            execute("insert into " + qualified + " values (1, 'Alice', 'has remark')");
            execute("insert into " + qualified + " values (2, 'Bob', null)");
            execute("insert into " + qualified + " values (3, 'Charlie', null)");

            DatabaseOperator operator = newOperator(currentSchema());

            List<Map<String, Object>> nullResults = operator.dml()
                .query(table)
                .where(dsl -> dsl.isNull("remark"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, nullResults.size());

            List<Map<String, Object>> notNullResults = operator.dml()
                .query(table)
                .where(dsl -> dsl.notNull("remark"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(1, notNullResults.size());
            Assert.assertEquals("Alice", notNullResults.get(0).get("name"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testNullValueRoundtrip() {
        String table = randomName("test_null_roundtrip_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64), opt_field varchar(128))");
        try {
            DatabaseOperator operator = newOperator(currentSchema());

            operator.dml()
                   .insert(table)
                   .value("id", 1)
                   .value("name", "Alice")
                   .value("opt_field", null)
                   .execute()
                   .sync();

            List<Map<String, Object>> nullRows = operator.dml()
                .query(table)
                .where(dsl -> dsl.isNull("opt_field"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(1, nullRows.size());
            Assert.assertEquals("Alice", nullRows.get(0).get("name"));
            Assert.assertNull(nullRows.get(0).get("opt_field"));

            operator.dml()
                   .insert(table)
                   .value("id", 2)
                   .value("name", "Bob")
                   .execute()
                   .sync();

            Map<String, Object> bob = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 2))
                .fetch(singleMap())
                .sync();
            Assert.assertEquals("Bob", bob.get("name"));
            Assert.assertNull(bob.get("opt_field"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testLargeTextRoundtrip() {
        String table = randomName("test_large_text_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, content text)");
        try {
            DatabaseOperator operator = newOperator(currentSchema());

            StringBuilder sb = new StringBuilder(10 * 1024);
            for (int i = 0; i < 1000; i++) {
                sb.append(String.format("Line %04d: the quick brown fox jumps over the lazy dog.\n", i));
            }
            String largeText = sb.toString();

            operator.dml()
                   .insert(table)
                   .value("id", 1)
                   .value("content", largeText)
                   .execute()
                   .sync();

            Map<String, Object> row = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 1))
                .fetch(singleMap())
                .sync();

            String retrieved = (String) row.get("content");
            Assert.assertNotNull(retrieved);
            Assert.assertEquals(largeText.length(), retrieved.length());
            Assert.assertEquals(largeText, retrieved);
        } finally {
            dropTableQuietly(qualified);
        }
    }
}
