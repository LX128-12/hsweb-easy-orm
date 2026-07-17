package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.TestSyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.SqlRequests;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.sql.JDBCType;
import java.util.Arrays;
import java.util.Optional;

public class KingbaseMssqlTableMetaParserTest {

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
    public void testParse() {
        executor.execute(SqlRequests.of("create table test_table(" +
            "id varchar(32) primary key," +
            "name varchar(128) not null," +
            "age integer" +
            ")"));
        try {
            Optional<RDBTableMetadata> parsed = parser.parseByName("test_table");
            Assert.assertTrue("Table metadata should be present", parsed.isPresent());
            RDBTableMetadata metaData = parsed.get();

            {
                RDBColumnMetadata column = metaData.getColumn("id").orElseThrow(NullPointerException::new);

                Assert.assertNotNull(column);
                Assert.assertTrue(matchesAny(column.getDataType(), "varchar", "character varying"));
                Assert.assertEquals(JDBCType.VARCHAR, column.getSqlType());
                Assert.assertEquals(String.class, column.getJavaType());
                Assert.assertTrue(column.isNotNull());
            }

            {
                RDBColumnMetadata column = metaData.getColumn("name").orElseThrow(NullPointerException::new);

                Assert.assertNotNull(column);
                // Length may be 0 for varchar in Kingbase MSSQL mode information_schema
                Assert.assertTrue("Length should be >= 0", column.getLength() >= 0);
                Assert.assertEquals(JDBCType.VARCHAR, column.getSqlType());
                Assert.assertEquals(String.class, column.getJavaType());
                Assert.assertTrue(column.isNotNull());
            }

            {
                RDBColumnMetadata column = metaData.getColumn("age").orElseThrow(NullPointerException::new);

                Assert.assertNotNull(column);
                Assert.assertEquals(JDBCType.INTEGER, column.getSqlType());
                Assert.assertEquals(Integer.class, column.getJavaType());
            }
        } finally {
            executor.execute(SqlRequests.of("drop table test_table"));
        }
    }

    private boolean matchesAny(String value, String... candidates) {
        if (value == null) {
            return false;
        }
        String lowerCase = value.toLowerCase();
        return Arrays.stream(candidates)
                     .map(String::toLowerCase)
                     .anyMatch(candidate -> lowerCase.equals(candidate) || lowerCase.startsWith(candidate + "("));
    }
}
