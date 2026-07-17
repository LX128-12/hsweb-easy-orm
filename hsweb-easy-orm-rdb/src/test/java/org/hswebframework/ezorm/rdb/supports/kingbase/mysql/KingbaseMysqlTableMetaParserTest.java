package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

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

import static org.hswebframework.ezorm.rdb.executor.SqlRequests.prepare;

public class KingbaseMysqlTableMetaParserTest {

    private SyncSqlExecutor executor;
    private KingbaseMysqlTableMetadataParser parser;

    @Before
    public void init() {
        KingbaseMysqlSchemaMetadata schema = new KingbaseMysqlSchemaMetadata(KingbaseMysqlConnectionProvider.getSchema());
        executor = new TestSyncSqlExecutor(new KingbaseMysqlConnectionProvider());
        schema.addFeature(executor);
        parser = new KingbaseMysqlTableMetadataParser(schema);
    }

    @Test
    public void testParse() {
        executor.execute(SqlRequests.of("CREATE TABLE IF NOT EXISTS test_table(" +
            "id varchar(32) primary key," +
            "name varchar(128) not null," +
            "age int" +
            ")"));
        try {
            RDBTableMetadata metaData = parser.parseByName("test_table").orElseThrow(NullPointerException::new);

            {
                RDBColumnMetadata column = metaData.getColumn("id").orElseThrow(NullPointerException::new);

                Assert.assertNotNull(column);
                Assert.assertTrue(matchesAny(column.getDataType(), "varchar(32)", "varchar"));
                Assert.assertEquals(JDBCType.VARCHAR, column.getSqlType());
                Assert.assertEquals(String.class, column.getJavaType());
                Assert.assertTrue(column.isNotNull());
            }

            {
                RDBColumnMetadata column = metaData.getColumn("name").orElseThrow(NullPointerException::new);

                Assert.assertNotNull(column);
                Assert.assertTrue(matchesAny(column.getDataType(), "varchar(128)", "varchar"));
                Assert.assertEquals(128, column.getLength());
                Assert.assertEquals(JDBCType.VARCHAR, column.getSqlType());
                Assert.assertEquals(String.class, column.getJavaType());
                Assert.assertTrue(column.isNotNull());
            }

            {
                RDBColumnMetadata column = metaData.getColumn("age").orElseThrow(NullPointerException::new);

                Assert.assertNotNull(column);
                Assert.assertEquals(JDBCType.INTEGER, column.getSqlType());
                Assert.assertEquals(Integer.class, column.getJavaType());
                Assert.assertTrue(matchesAny(column.getDataType(), "int", "integer"));
            }
        } finally {
            executor.execute(prepare("drop table test_table"));
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
