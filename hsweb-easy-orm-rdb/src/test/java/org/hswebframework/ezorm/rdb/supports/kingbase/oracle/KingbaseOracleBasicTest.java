package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.dialect.Dialect;
import org.hswebframework.ezorm.rdb.supports.BasicCommonTests;

public class KingbaseOracleBasicTest extends BasicCommonTests {

    @Override
    protected RDBSchemaMetadata getSchema() {
        return new KingbaseOracleSchemaMetadata(KingbaseOracleConnectionProvider.getSchema());
    }

    @Override
    protected Dialect getDialect() {
        return new KingbaseOracleDialect();
    }

    @Override
    protected SyncSqlExecutor getSqlExecutor() {
        return new TestSyncSqlExecutor(new KingbaseOracleConnectionProvider());
    }
}