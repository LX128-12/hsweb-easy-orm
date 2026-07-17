package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.executor.BatchSqlRequest;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.NativeSqlDefaultValue;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBIndexMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateTableSqlBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.sql.JDBCType;

public class KingbaseOracleCreateTableSqlBuilderTest {

    @Test
    public void testBuild() {
        KingbaseOracleSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("TEST_CREATE_SQL");
        table.setComment("demo table");

        RDBColumnMetadata id = new RDBColumnMetadata();
        id.setName("ID");
        id.setSortIndex(1);
        id.setJdbcType(JDBCType.NUMERIC, Long.class);
        id.setPrimaryKey(true);
        id.setNotNull(true);
        id.setComment("primary key");
        id.setPrecision(20);
        table.addColumn(id);

        RDBColumnMetadata code = new RDBColumnMetadata();
        code.setName("CODE");
        code.setSortIndex(2);
        code.setLength(32);
        code.setJdbcType(JDBCType.VARCHAR, String.class);
        code.setNotNull(true);
        code.setComment("code column");
        table.addColumn(code);

        RDBColumnMetadata createdAt = new RDBColumnMetadata();
        createdAt.setName("CREATED_AT");
        createdAt.setSortIndex(3);
        createdAt.setJdbcType(JDBCType.TIMESTAMP, java.sql.Timestamp.class);
        createdAt.setDefaultValue(NativeSqlDefaultValue.of("sysdate"));
        table.addColumn(createdAt);

        RDBColumnMetadata description = new RDBColumnMetadata();
        description.setName("DESCRIPTION");
        description.setSortIndex(4);
        description.setLength(200);
        description.setJdbcType(JDBCType.VARCHAR, String.class);
        table.addColumn(description);

        RDBIndexMetadata index = new RDBIndexMetadata();
        index.setName("idx_test_create_sql_code");
        index.setUnique(true);
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("CODE", RDBIndexMetadata.IndexSort.asc));
        table.addIndex(index);

        SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(table);
        Assert.assertTrue(request instanceof BatchSqlRequest);

        BatchSqlRequest batch = (BatchSqlRequest) request;
        Assert.assertEquals(
            "create table \"public\".TEST_CREATE_SQL ( \"ID\" number(20,0) not null primary key , \"CODE\" varchar2(32 char) not null , \"CREATED_AT\" timestamp default sysdate , \"DESCRIPTION\" varchar2(200 char) )",
            normalizeSql(batch.getSql())
        );

        Assert.assertTrue(batch.getBatch().size() >= 1);
    }

    @Test
    public void testBuildWithClobAndNumericPrecision() {
        KingbaseOracleSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("TEST_TYPES");
        table.setComment("type test");

        RDBColumnMetadata id = new RDBColumnMetadata();
        id.setName("ID");
        id.setSortIndex(1);
        id.setJdbcType(JDBCType.NUMERIC, Long.class);
        id.setPrimaryKey(true);
        id.setNotNull(true);
        id.setPrecision(20);
        table.addColumn(id);

        RDBColumnMetadata body = new RDBColumnMetadata();
        body.setName("BODY");
        body.setSortIndex(2);
        body.setJdbcType(JDBCType.CLOB, String.class);
        body.setLength(1024);
        body.setNotNull(true);
        table.addColumn(body);

        RDBColumnMetadata amount = new RDBColumnMetadata();
        amount.setName("AMOUNT");
        amount.setSortIndex(3);
        amount.setJdbcType(JDBCType.NUMERIC, java.math.BigDecimal.class);
        amount.setPrecision(18);
        amount.setScale(4);
        amount.setNotNull(true);
        table.addColumn(amount);

        SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(table);
        Assert.assertTrue(request instanceof BatchSqlRequest);

        BatchSqlRequest batch = (BatchSqlRequest) request;
        String sql = normalizeSql(batch.getSql());
        Assert.assertTrue(sql.contains("\"ID\" number(20,0) not null primary key"));
        Assert.assertTrue(sql.contains("\"BODY\" clob not null"));
        Assert.assertTrue(sql.contains("\"AMOUNT\" number(18,4) not null"));
    }

    @Test
    public void testBuildCompositePrimaryKey() {
        KingbaseOracleSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("TEST_COMPOSITE_PK");

        RDBColumnMetadata colA = new RDBColumnMetadata();
        colA.setName("COL_A");
        colA.setSortIndex(1);
        colA.setJdbcType(JDBCType.NUMERIC, Long.class);
        colA.setPrimaryKey(true);
        colA.setNotNull(true);
        colA.setPrecision(20);
        table.addColumn(colA);

        RDBColumnMetadata colB = new RDBColumnMetadata();
        colB.setName("COL_B");
        colB.setSortIndex(2);
        colB.setJdbcType(JDBCType.VARCHAR, String.class);
        colB.setLength(64);
        colB.setPrimaryKey(true);
        colB.setNotNull(true);
        table.addColumn(colB);

        SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(table);
        BatchSqlRequest batch = (BatchSqlRequest) request;
        String sql = normalizeSql(batch.getSql());
        Assert.assertTrue(sql.contains("\"COL_A\" number(20,0) not null primary key"));
        Assert.assertTrue(sql.contains("\"COL_B\" varchar2(64 char) not null primary key"));
    }

    @Test
    public void testBuildMultipleIndexes() {
        KingbaseOracleSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("TEST_MULTI_IDX");

        RDBColumnMetadata id = new RDBColumnMetadata();
        id.setName("ID");
        id.setJdbcType(JDBCType.NUMERIC, Long.class);
        id.setPrimaryKey(true);
        id.setNotNull(true);
        id.setPrecision(20);
        table.addColumn(id);

        RDBColumnMetadata name = new RDBColumnMetadata();
        name.setName("NAME");
        name.setJdbcType(JDBCType.VARCHAR, String.class);
        name.setLength(128);
        table.addColumn(name);

        RDBColumnMetadata code = new RDBColumnMetadata();
        code.setName("CODE");
        code.setJdbcType(JDBCType.VARCHAR, String.class);
        code.setLength(64);
        table.addColumn(code);

        RDBIndexMetadata idxName = new RDBIndexMetadata();
        idxName.setName("idx_name");
        idxName.getColumns().add(RDBIndexMetadata.IndexColumn.of("NAME", RDBIndexMetadata.IndexSort.asc));
        table.addIndex(idxName);

        RDBIndexMetadata idxCode = new RDBIndexMetadata();
        idxCode.setName("idx_code_unique");
        idxCode.setUnique(true);
        idxCode.getColumns().add(RDBIndexMetadata.IndexColumn.of("CODE", RDBIndexMetadata.IndexSort.asc));
        table.addIndex(idxCode);

        SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(table);
        BatchSqlRequest batch = (BatchSqlRequest) request;
        Assert.assertTrue(batch.getBatch().size() >= 2);
    }

    @Test
    public void testBuildTableWithoutComment() {
        KingbaseOracleSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("TEST_NO_COMMENT");

        RDBColumnMetadata id = new RDBColumnMetadata();
        id.setName("ID");
        id.setJdbcType(JDBCType.NUMERIC, Long.class);
        id.setPrimaryKey(true);
        id.setNotNull(true);
        id.setPrecision(20);
        table.addColumn(id);

        SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(table);
        BatchSqlRequest batch = (BatchSqlRequest) request;
        String sql = normalizeSql(batch.getSql());
        Assert.assertTrue(sql.startsWith("create table \"public\".TEST_NO_COMMENT"));
    }

    private KingbaseOracleSchemaMetadata newSchema() {
        KingbaseOracleDialect dialect = new KingbaseOracleDialect();
        RDBDatabaseMetadata database = new RDBDatabaseMetadata(dialect);
        KingbaseOracleSchemaMetadata schema = new KingbaseOracleSchemaMetadata("public");
        database.addSchema(schema);
        database.setCurrentSchema(schema);
        return schema;
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
