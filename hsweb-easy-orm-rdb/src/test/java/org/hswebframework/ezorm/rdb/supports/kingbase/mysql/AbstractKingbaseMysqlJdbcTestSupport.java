package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.DefaultDatabaseOperator;
import org.junit.Before;

import java.util.UUID;

abstract class AbstractKingbaseMysqlJdbcTestSupport {

    protected KingbaseMysqlConnectionProvider provider;
    protected SyncSqlExecutor executor;

    @Before
    public void initKingbaseMysqlJdbcSupport() {
        provider = new KingbaseMysqlConnectionProvider();
        executor = new TestSyncSqlExecutor(provider);
    }

    protected String currentSchema() {
        return KingbaseMysqlConnectionProvider.getSchema();
    }

    protected String randomName(String prefix) {
        return prefix + UUID.randomUUID().toString().replace("-", "");
    }

    protected DatabaseOperator newOperator(String schemaName) {
        RDBDatabaseMetadata database = new RDBDatabaseMetadata(new KingbaseMysqlDialect());
        database.addFeature(executor);

        KingbaseMysqlSchemaMetadata schema = new KingbaseMysqlSchemaMetadata(schemaName);
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
