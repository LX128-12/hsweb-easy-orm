package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class KingbaseMssqlSchemaMetadataTest {

    private SyncSqlExecutor executor;
    private KingbaseMssqlTableMetadataParser parser;

    @Before
    public void init() {
        String schema = KingbaseMssqlConnectionProvider.getSchema();
        KingbaseMssqlSchemaMetadata schemaMeta = new KingbaseMssqlSchemaMetadata(schema);
        executor = new TestSyncSqlExecutor(new KingbaseMssqlConnectionProvider());
        schemaMeta.addFeature(executor);
        parser = new KingbaseMssqlTableMetadataParser(schemaMeta);
    }

    @Test
    public void testTableExists() {
        executor.execute(SqlRequests.of("create table test_schema_exists_t (id int primary key)"));
        try {
            String existsSql = parser.getTableExistsSql();
            Assert.assertNotNull(existsSql);
            Assert.assertTrue(existsSql.contains("information_schema.tables"));
            Assert.assertTrue(existsSql.contains("table_schema"));
            Assert.assertTrue(existsSql.contains("table_name"));
        } finally {
            executor.execute(SqlRequests.of("drop table test_schema_exists_t"));
        }
    }

    @Test
    public void testParseAllListsTables() {
        executor.execute(SqlRequests.of("create table test_schema_all_1 (id int primary key)"));
        executor.execute(SqlRequests.of("create table test_schema_all_2 (id int primary key)"));
        try {
            List<RDBTableMetadata> tables = parser.parseAll();
            Assert.assertTrue(tables.size() >= 2);
            boolean hasTable1 = tables.stream().anyMatch(t -> "test_schema_all_1".equalsIgnoreCase(t.getName()));
            boolean hasTable2 = tables.stream().anyMatch(t -> "test_schema_all_2".equalsIgnoreCase(t.getName()));
            Assert.assertTrue(hasTable1);
            Assert.assertTrue(hasTable2);
        } finally {
            executor.execute(SqlRequests.of("drop table test_schema_all_1"));
            executor.execute(SqlRequests.of("drop table test_schema_all_2"));
        }
    }

    @Test
    public void testParseTableWithComment() {
        executor.execute(SqlRequests.of(
            "create table test_schema_comment (id int primary key, name varchar(64))"));
        try {
            executor.execute(SqlRequests.of(
                "comment on table test_schema_comment is 'schema test table comment mssql'"));
            java.util.Optional<RDBTableMetadata> parsed = parser.parseByName("test_schema_comment");
            Assert.assertTrue(parsed.isPresent());
            RDBTableMetadata table = parsed.get();
            Assert.assertEquals("test_schema_comment", table.getName().toLowerCase());
        } finally {
            executor.execute(SqlRequests.of("drop table test_schema_comment"));
        }
    }
}
