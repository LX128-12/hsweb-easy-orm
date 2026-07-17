package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

public class KingbaseOracleTableCommentTest {

    private SyncSqlExecutor executor;
    private KingbaseOracleTableMetadataParser parser;

    @Before
    public void init() {
        String schema = KingbaseOracleConnectionProvider.getSchema();
        KingbaseOracleSchemaMetadata schemaMeta = new KingbaseOracleSchemaMetadata(schema);
        executor = new TestSyncSqlExecutor(new KingbaseOracleConnectionProvider());
        schemaMeta.addFeature(executor);
        parser = new KingbaseOracleTableMetadataParser(schemaMeta);
    }

    @Test
    public void testWriteAndReadTableComment() {
        executor.execute(SqlRequests.of(
            "create table test_comment_tbl (id int primary key, name varchar(64))"));
        try {
            executor.execute(SqlRequests.of(
                "comment on table test_comment_tbl is 'table comment oracle'"));
            executor.execute(SqlRequests.of(
                "comment on column test_comment_tbl.name is 'name column comment'"));

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
