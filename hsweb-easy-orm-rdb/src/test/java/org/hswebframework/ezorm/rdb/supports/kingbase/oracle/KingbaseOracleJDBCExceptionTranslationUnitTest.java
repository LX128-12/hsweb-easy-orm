package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.junit.Assert;
import org.junit.Test;

import java.sql.SQLException;

public class KingbaseOracleJDBCExceptionTranslationUnitTest {

    @Test
    public void testTranslateDuplicateKeyVariants() {
        KingbaseOracleJDBCExceptionTranslation translation =
            KingbaseOracleJDBCExceptionTranslation.of(new KingbaseOracleSchemaMetadata("public"));

        assertDuplicateKey(translation.translate(new SQLException("duplicate", "23505", 0)));
        assertDuplicateKey(translation.translate(new SQLException("duplicate", "23000", 0)));
        assertDuplicateKey(translation.translate(new SQLException("duplicate", "42000", 1062)));
        assertDuplicateKey(translation.translate(new SQLException("duplicate", "42000", 1022)));
        assertDuplicateKey(translation.translate(new SQLException("duplicate key value", "42000", 0)));
        assertDuplicateKey(translation.translate(new SQLException("duplicated key value", "42000", 0)));
        assertDuplicateKey(translation.translate(new SQLException("violates unique constraint", "42000", 0)));
    }

    @Test
    public void testShouldKeepOriginalExceptionWhenNotDuplicateKey() {
        KingbaseOracleJDBCExceptionTranslation translation =
            KingbaseOracleJDBCExceptionTranslation.of(new KingbaseOracleSchemaMetadata("public"));

        Throwable nonSqlException = new IllegalStateException("boom");
        Assert.assertSame(nonSqlException, translation.translate(nonSqlException));

        SQLException nullMessage = new SQLException(null, "42000", 0);
        Assert.assertSame(nullMessage, translation.translate(nullMessage));

        SQLException unrelated = new SQLException("syntax error", "42000", 0);
        Assert.assertSame(unrelated, translation.translate(unrelated));
    }

    private void assertDuplicateKey(Throwable error) {
        Assert.assertTrue(error instanceof DuplicateKeyException);
        Assert.assertTrue(error.getCause() instanceof SQLException);
    }
}