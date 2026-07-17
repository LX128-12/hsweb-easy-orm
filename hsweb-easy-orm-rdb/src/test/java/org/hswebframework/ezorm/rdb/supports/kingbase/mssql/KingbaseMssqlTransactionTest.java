package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseMssqlTransactionTest extends AbstractKingbaseMssqlJdbcTestSupport {

    @Test
    public void testCommitVisible() throws Exception {
        String table = randomName("test_commit_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            try (Connection conn = provider.getConnection()) {
                conn.setAutoCommit(false);
                conn.createStatement().execute("insert into " + qualified + " values (1, 'committed')");
                conn.createStatement().execute("insert into " + qualified + " values (2, 'also_committed')");
                conn.commit();
                conn.setAutoCommit(true);
            }

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> rows = operator.dml()
                .query(table)
                .fetch(mapList())
                .sync();

            Assert.assertEquals(2, rows.size());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testRollbackInvisible() throws Exception {
        String table = randomName("test_rollback_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            try (Connection conn = provider.getConnection()) {
                conn.setAutoCommit(false);
                conn.createStatement().execute("insert into " + qualified + " values (1, 'rolled_back')");
                conn.rollback();
                conn.setAutoCommit(true);
            }

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> rows = operator.dml()
                .query(table)
                .fetch(mapList())
                .sync();

            Assert.assertEquals(0, rows.size());
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testAlterColumnType() {
        String table = randomName("test_alt_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(32))");
        try {
            execute("insert into " + qualified + " values (1, 'short_name')");
            execute("alter table " + qualified + " alter column name type varchar(64)");
            execute("insert into " + qualified + " values (2, 'this_is_a_longer_name_that_needs_more_than_thirtytwo_chars')");

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> rows = operator.dml()
                .query(table)
                .orderBy(SortOrder.asc("id"))
                .fetch(mapList())
                .sync();

            Assert.assertEquals(2, rows.size());
            Assert.assertEquals("short_name", rows.get(0).get("name"));
            Assert.assertEquals("this_is_a_longer_name_that_needs_more_than_thirtytwo_chars", rows.get(1).get("name"));
        } finally {
            dropTableQuietly(qualified);
        }
    }
}
