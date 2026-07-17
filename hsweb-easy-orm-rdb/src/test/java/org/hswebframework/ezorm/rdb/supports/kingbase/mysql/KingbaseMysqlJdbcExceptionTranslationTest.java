package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.junit.Assert;
import org.junit.Test;

public class KingbaseMysqlJdbcExceptionTranslationTest extends AbstractKingbaseMysqlJdbcTestSupport {

    @Test
    public void testDuplicateKeyShouldTranslateToFrameworkException() {
        String table = randomName("test_dup_translate_");
        DatabaseOperator operator = newOperator(currentSchema());

        execute("create table " + table + "(" +
                    "id varchar(32) primary key," +
                    "name varchar(64) not null" +
                ")");
        try {
            operator.dml()
                    .insert(table)
                    .value("id", "dup-1")
                    .value("name", "first")
                    .execute()
                    .sync();

            try {
                operator.dml()
                        .insert(table)
                        .value("id", "dup-1")
                        .value("name", "second")
                        .execute()
                        .sync();
                Assert.fail("Expected DuplicateKeyException");
            } catch (Throwable error) {
                Assert.assertTrue(
                    "Expected DuplicateKeyException but got: " + error.getClass().getName(),
                    error instanceof DuplicateKeyException
                );
            }
        } finally {
            dropTableQuietly(table);
        }
    }
}
