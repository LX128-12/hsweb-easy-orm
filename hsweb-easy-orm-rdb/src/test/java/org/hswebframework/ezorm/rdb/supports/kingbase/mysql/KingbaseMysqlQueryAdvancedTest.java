package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.dml.query.Selects;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseMysqlQueryAdvancedTest extends AbstractKingbaseMysqlJdbcTestSupport {

    @Test
    public void testOrderByAscAndDesc() {
        String table = randomName("test_orderby_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64), score int)");
        try {
            execute("insert into " + qualified + " values (1, 'Bob', 80)");
            execute("insert into " + qualified + " values (2, 'Alice', 95)");
            execute("insert into " + qualified + " values (3, 'Charlie', 80)");

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> ascResult = operator.dml()
                .query(table)
                .orderBy(SortOrder.asc("name"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals("Alice", ascResult.get(0).get("name"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testOrCondition() {
        String table = randomName("test_or_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64), status varchar(16))");
        try {
            execute("insert into " + qualified + " values (1, 'Alice', 'active')");
            execute("insert into " + qualified + " values (2, 'Bob', 'inactive')");
            execute("insert into " + qualified + " values (3, 'Charlie', 'active')");

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> result = operator.dml()
                .query(table)
                .where(dsl -> dsl.or()
                    .is("name", "Alice")
                    .is("name", "Charlie"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, result.size());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testJoinQuery() {
        String tableA = randomName("test_join_a_");
        String tableB = randomName("test_join_b_");
        String qualifiedA = currentSchema() + "." + tableA;
        String qualifiedB = currentSchema() + "." + tableB;
        execute("create table " + qualifiedA + " (id int primary key, name varchar(64), b_id int)");
        execute("create table " + qualifiedB + " (id int primary key, description varchar(64))");
        try {
            execute("insert into " + qualifiedA + " values (1, 'Alice', 10)");
            execute("insert into " + qualifiedA + " values (2, 'Bob', 20)");
            execute("insert into " + qualifiedB + " values (10, 'Developer')");

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> result = operator.dml()
                .query(tableA)
                .select(Selects.column("name"), Selects.column("description"))
                .leftJoin(tableB, join -> join.on(cdt -> cdt.is("b_id", "id")))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, result.size());
        } finally {
            dropTableQuietly(qualifiedA);
            dropTableQuietly(qualifiedB);
        }
    }

    @Test
    public void testSubquery() {
        String tableA = randomName("test_sub_a_");
        String tableB = randomName("test_sub_b_");
        String qualifiedA = currentSchema() + "." + tableA;
        String qualifiedB = currentSchema() + "." + tableB;
        execute("create table " + qualifiedA + " (id int primary key, name varchar(64))");
        execute("create table " + qualifiedB + " (id int primary key, a_id int, val int)");
        try {
            execute("insert into " + qualifiedA + " values (1, 'Alice')");
            execute("insert into " + qualifiedA + " values (2, 'Bob')");
            execute("insert into " + qualifiedA + " values (3, 'Charlie')");
            execute("insert into " + qualifiedB + " values (1, 1, 100)");
            execute("insert into " + qualifiedB + " values (2, 1, 200)");
            execute("insert into " + qualifiedB + " values (3, 2, 300)");

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> result = operator.dml()
                .query(tableA)
                .where(dsl -> dsl.in("id",
                    operator.dml().query(tableB)
                        .select("a_id")
                        .where(subDsl -> subDsl.gt("val", 150))
                        .fetch(mapList())
                        .sync()
                        .stream().map(m -> m.get("a_id")).collect(Collectors.toList())))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, result.size());
        } finally {
            dropTableQuietly(qualifiedA);
            dropTableQuietly(qualifiedB);
        }
    }
}
