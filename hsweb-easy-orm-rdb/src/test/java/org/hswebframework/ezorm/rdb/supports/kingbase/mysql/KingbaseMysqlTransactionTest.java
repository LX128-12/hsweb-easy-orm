package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.util.List;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseMysqlTransactionTest extends AbstractKingbaseMysqlJdbcTestSupport {

    @Test
    public void testCommitVisible() throws Exception {
        String table = randomName("test_commit_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            // Insert within a transaction via raw JDBC
            try (Connection conn = provider.getConnection()) {
                conn.setAutoCommit(false);
                conn.createStatement().execute("insert into " + qualified + " values (1, 'committed')");
                conn.createStatement().execute("insert into " + qualified + " values (2, 'also_committed')");
                conn.commit();
                conn.setAutoCommit(true);
            }

            // Verify data is visible from a different connection
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
            // Insert within a transaction then rollback
            try (Connection conn = provider.getConnection()) {
                conn.setAutoCommit(false);
                conn.createStatement().execute("insert into " + qualified + " values (1, 'rolled_back')");
                conn.rollback();
                conn.setAutoCommit(true);
            }

            // Verify data is NOT visible
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
        // Use a refixed name to avoid table-name length issues
        String table = randomName("test_alt_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(32))");
        try {
            // Insert data that fits within 32 chars
            execute("insert into " + qualified + " values (1, 'short_name')");

            // Alter the column to varchar(64)
            execute("alter table " + qualified + " alter column name type varchar(64)");

            // Now insert data that requires 64 chars
            execute("insert into " + qualified + " values (2, 'this_is_a_longer_name_that_needs_more_than_thirtytwo_chars')");

            // Verify both rows are present
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
