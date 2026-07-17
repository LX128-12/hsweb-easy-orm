package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBIndexMetadata;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class KingbaseMssqlIndexMetadataParserTest {

    @Test
    public void test() {
        String schemaName = KingbaseMssqlConnectionProvider.getSchema();
        SyncSqlExecutor sqlExecutor = new TestSyncSqlExecutor(new KingbaseMssqlConnectionProvider());
        try {
            KingbaseMssqlSchemaMetadata schema = new KingbaseMssqlSchemaMetadata(schemaName);
            KingbaseMssqlIndexMetadataParser parser = new KingbaseMssqlIndexMetadataParser(schema);

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

            Assert.assertFalse("Should find at least the primary key index", list.isEmpty());

            for (RDBIndexMetadata index : list) {
                Assert.assertNotNull("Index name should not be null", index.getName());
                Assert.assertNotNull("Index table name should not be null", index.getTableName());
                Assert.assertFalse("Index columns should not be empty", index.getColumns().isEmpty());
            }
        } finally {
            try {
                sqlExecutor.execute(SqlRequests.of("drop table " + schemaName + ".test_index_parser"));
            } catch (Exception ignore) {
            }
        }
    }
}
