package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.Savepoint;
import java.util.List;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseMysqlSavepointTest extends AbstractKingbaseMysqlJdbcTestSupport {

    @Test
    public void testSavepointRollback() throws Exception {
        String table = randomName("test_savepoint_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            try (Connection conn = provider.getConnection()) {
                conn.setAutoCommit(false);
                conn.createStatement().execute("insert into " + qualified + " values (1, 'before_sp')");

                Savepoint sp = conn.setSavepoint("sp1");
                conn.createStatement().execute("insert into " + qualified + " values (2, 'after_sp')");
                conn.rollback(sp);
                conn.commit();
                conn.setAutoCommit(true);
            }

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> rows = operator.dml()
                .query(table)
                .fetch(mapList())
                .sync();
            Assert.assertEquals(1, rows.size());
            Assert.assertEquals("before_sp", rows.get(0).get("name"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testSavepointRelease() throws Exception {
        String table = randomName("test_sp_release_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            try (Connection conn = provider.getConnection()) {
                conn.setAutoCommit(false);
                conn.createStatement().execute("insert into " + qualified + " values (1, 'first')");

                Savepoint sp = conn.setSavepoint("sp_release");
                conn.createStatement().execute("insert into " + qualified + " values (2, 'second')");
                conn.releaseSavepoint(sp);
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
}
