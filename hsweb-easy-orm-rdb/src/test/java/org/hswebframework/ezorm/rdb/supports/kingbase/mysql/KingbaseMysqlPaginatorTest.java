package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseMysqlPaginatorTest extends AbstractKingbaseMysqlJdbcTestSupport {

    @Test
    public void testPagingFirstPage() {
        String table = randomName("test_page_first_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            for (int i = 1; i <= 30; i++) {
                execute("insert into " + qualified + " values (" + i + ", 'item" + i + "')");
            }
            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> page1 = operator.dml()
                .query(table)
                .orderBy(SortOrder.asc("id"))
                .paging(0, 10)
                .fetch(mapList())
                .sync();
            Assert.assertEquals(10, page1.size());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testPagingLastPage() {
        String table = randomName("test_page_last_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            for (int i = 1; i <= 25; i++) {
                execute("insert into " + qualified + " values (" + i + ", 'item" + i + "')");
            }
            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> page3 = operator.dml()
                .query(table)
                .orderBy(SortOrder.asc("id"))
                .paging(2, 10)
                .fetch(mapList())
                .sync();
            Assert.assertEquals(5, page3.size());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testPagingEmptyResult() {
        String table = randomName("test_page_empty_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> result = operator.dml()
                .query(table)
                .paging(0, 10)
                .fetch(mapList())
                .sync();
            Assert.assertTrue(result.isEmpty());
        } finally {
            dropTableQuietly(qualified);
        }
    }
}
