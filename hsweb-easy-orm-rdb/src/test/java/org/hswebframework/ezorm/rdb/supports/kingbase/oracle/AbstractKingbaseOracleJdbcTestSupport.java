package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.DefaultDatabaseOperator;
import org.junit.Before;

import java.util.UUID;

abstract class AbstractKingbaseOracleJdbcTestSupport {

    protected KingbaseOracleConnectionProvider provider;
    protected SyncSqlExecutor executor;

    @Before
    public void initKingbaseOracleJdbcSupport() {
        provider = new KingbaseOracleConnectionProvider();
        executor = new TestSyncSqlExecutor(provider);
    }

    protected String currentSchema() {
        return KingbaseOracleConnectionProvider.getSchema();
    }

    protected String randomName(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "");
    }

    protected DatabaseOperator newOperator(String schemaName) {
        RDBDatabaseMetadata database = new RDBDatabaseMetadata(new KingbaseOracleDialect());
        database.addFeature(executor);

        KingbaseOracleSchemaMetadata schema = new KingbaseOracleSchemaMetadata(schemaName);
        database.addSchema(schema);
        database.setCurrentSchema(schema);

        return DefaultDatabaseOperator.of(database);
    }

    protected void execute(String sql, Object... parameters) {
        executor.execute(SqlRequests.of(sql, parameters));
    }

    protected void dropTableQuietly(String qualifiedTableName) {
        try {
            execute("drop table " + qualifiedTableName);
        } catch (Exception ignore) {
        }
    }
}