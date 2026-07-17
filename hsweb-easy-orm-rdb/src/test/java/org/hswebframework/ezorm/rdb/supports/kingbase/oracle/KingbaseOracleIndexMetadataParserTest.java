package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBIndexMetadata;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class KingbaseOracleIndexMetadataParserTest {

    @Test
    public void test() {
        String schemaName = KingbaseOracleConnectionProvider.getSchema();
        SyncSqlExecutor sqlExecutor = new TestSyncSqlExecutor(new KingbaseOracleConnectionProvider());
        try {
            KingbaseOracleSchemaMetadata schema = new KingbaseOracleSchemaMetadata(schemaName);
            KingbaseOracleIndexMetadataParser parser = KingbaseOracleIndexMetadataParser.of(schema);

            parser.getSchema().addFeature(sqlExecutor);

            sqlExecutor.execute(SqlRequests.of(
                "create table " + schemaName + ".test_index_parser(" +
                    "id varchar(32) primary key," +
                    "name varchar(32) not null," +
                    "age int," +
                    "addr varchar(128))"));

            sqlExecutor.execute(SqlRequests.of(
                "create index test_index on " + schemaName + ".test_index_parser (age)"));
            sqlExecutor.execute(SqlRequests.of(
                "create unique index test_index_2 on " + schemaName + ".test_index_parser (name)"));
            sqlExecutor.execute(SqlRequests.of(
                "create index test_index_3 on " + schemaName + ".test_index_parser (name,addr desc)"));

            List<RDBIndexMetadata> list = parser.parseTableIndex("test_index_parser");

            Assert.assertEquals(4, list.size());
        } finally {
            try {
                sqlExecutor.execute(SqlRequests.of("drop table " + schemaName + ".test_index_parser"));
            } catch (Exception ignore) {
            }
        }
    }
}