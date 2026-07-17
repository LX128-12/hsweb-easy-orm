package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

public class KingbaseMssqlTableCommentTest {

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
    public void testWriteAndReadTableComment() {
        executor.execute(SqlRequests.of(
            "create table test_comment_tbl (id int primary key, name varchar(64))"));
        try {
            executor.execute(SqlRequests.of(
                "comment on table test_comment_tbl is 'table comment mssql'"));
            executor.execute(SqlRequests.of(
                "comment on column test_comment_tbl.name is 'name column comment mssql'"));

            Optional<RDBTableMetadata> parsed = parser.parseByName("test_comment_tbl");
            Assert.assertTrue(parsed.isPresent());
        } finally {
            executor.execute(SqlRequests.of("drop table test_comment_tbl"));
        }
    }

    @Test
    public void testCommentSqlTemplates() {
        String tableMetaSql = parser.getTableMetaSql("test");
        Assert.assertNotNull(tableMetaSql);
        Assert.assertTrue(tableMetaSql.contains("information_schema.columns"));

        String commentSql = parser.getTableCommentSql("test");
        Assert.assertNotNull(commentSql);

        String allTablesSql = parser.getAllTableSql();
        Assert.assertNotNull(allTablesSql);

        String existsSql = parser.getTableExistsSql();
        Assert.assertNotNull(existsSql);
    }
}
