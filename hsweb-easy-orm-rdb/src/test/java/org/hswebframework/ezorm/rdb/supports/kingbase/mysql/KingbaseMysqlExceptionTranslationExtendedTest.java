package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.junit.Assert;
import org.junit.Test;

public class KingbaseMysqlExceptionTranslationExtendedTest extends AbstractKingbaseMysqlJdbcTestSupport {

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
    public void testTranslationReturnsOriginalForNonSqlException() {
        KingbaseMysqlJDBCExceptionTranslation translation = KingbaseMysqlJDBCExceptionTranslation.of(null);
        RuntimeException original = new RuntimeException("test");
        Throwable result = translation.translate(original);
        Assert.assertSame(original, result);
    }
}
