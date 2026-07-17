package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class KingbaseOracleExceptionTranslationExtendedTest extends AbstractKingbaseOracleJdbcTestSupport {

    @Test
    public void testTableNotFound() {
        try {
            execute("select * from non_existent_table_test_xyz");
            Assert.fail("Expected exception");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testColumnNotFound() {
        String table = randomName("test_col_not_found_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key)");
        try {
            execute("select non_existent_col from " + qualified);
            Assert.fail("Expected exception");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testSyntaxError() {
        try {
            execute("CREAT TABLE bad_syntax (id int)");
            Assert.fail("Expected exception");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }
    }

    @Test
    public void testNotNullViolation() {
        String table = randomName("test_notnull_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64) not null)");
        try {
            execute("insert into " + qualified + " values (1, null)");
            Assert.fail("Expected exception");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testDataTypeMismatch() {
        String table = randomName("test_type_mismatch_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, score int)");
        try {
            execute("insert into " + qualified + " values (1, 'not_a_number')");
            Assert.fail("Expected exception");
        } catch (Exception e) {
            Assert.assertNotNull(e);
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testTranslationReturnsOriginalForNonSqlException() {
        KingbaseOracleJDBCExceptionTranslation translation = KingbaseOracleJDBCExceptionTranslation.of(null);
        RuntimeException original = new RuntimeException("test");
        Throwable result = translation.translate(original);
        Assert.assertSame(original, result);
        Assert.assertSame("test", result.getMessage());
    }
}
