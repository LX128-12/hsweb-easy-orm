package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

public class KingbaseMysqlTableCommentTest {

    private SyncSqlExecutor executor;
    private KingbaseMysqlTableMetadataParser parser;

    @Before
    public void init() {
        String schema = KingbaseMysqlConnectionProvider.getSchema();
        KingbaseMysqlSchemaMetadata schemaMeta = new KingbaseMysqlSchemaMetadata(schema);
        executor = new TestSyncSqlExecutor(new KingbaseMysqlConnectionProvider());
        schemaMeta.addFeature(executor);
        parser = new KingbaseMysqlTableMetadataParser(schemaMeta);
    }

    @Test
    public void testWriteAndReadTableComment() {
        executor.execute(SqlRequests.of(
            "create table test_comment_tbl (id int primary key, name varchar(64))"));
        try {
            executor.execute(SqlRequests.of(
                "comment on table test_comment_tbl is 'table comment mysql'"));
            executor.execute(SqlRequests.of(
                "comment on column test_comment_tbl.name is 'name column comment mysql'"));

            Optional<RDBTableMetadata> parsed = parser.parseByName("test_comment_tbl");
            Assert.assertTrue(parsed.isPresent());
        } finally {
            executor.execute(SqlRequests.of("drop table test_comment_tbl"));
        }
    }
}
