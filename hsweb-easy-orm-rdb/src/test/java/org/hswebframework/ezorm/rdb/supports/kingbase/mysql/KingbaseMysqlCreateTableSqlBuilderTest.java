package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

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

public class KingbaseMysqlCreateTableSqlBuilderTest {

    @Test
    public void testBuild() {
        KingbaseMysqlSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("test_create_sql");
        table.setComment("demo table");

        RDBColumnMetadata id = new RDBColumnMetadata();
        id.setName("id");
        id.setSortIndex(1);
        id.setJdbcType(JDBCType.INTEGER, Integer.class);
        id.setPrimaryKey(true);
        id.setAutoIncrement(true);
        id.setComment("pk");
        table.addColumn(id);

        RDBColumnMetadata code = new RDBColumnMetadata();
        code.setName("code");
        code.setSortIndex(2);
        code.setLength(32);
        code.setJdbcType(JDBCType.VARCHAR, String.class);
        code.setNotNull(true);
        code.setComment("code");
        table.addColumn(code);

        RDBColumnMetadata createdAt = new RDBColumnMetadata();
        createdAt.setName("created_at");
        createdAt.setSortIndex(3);
        createdAt.setType(schema.getDialect().convertDataType("timestamp without time zone"));
        createdAt.setDefaultValue(NativeSqlDefaultValue.of("now()"));
        table.addColumn(createdAt);

        RDBColumnMetadata metadata = new RDBColumnMetadata();
        metadata.setName("metadata");
        metadata.setSortIndex(4);
        metadata.setColumnDefinition("jsonb not null");
        table.addColumn(metadata);

        RDBColumnMetadata nickname = new RDBColumnMetadata();
        nickname.setName("nickname");
        nickname.setSortIndex(5);
        nickname.setLength(64);
        nickname.setType(schema.getDialect().convertDataType("character varying"));
        table.addColumn(nickname);

        RDBIndexMetadata index = new RDBIndexMetadata();
        index.setName("idx_test_create_sql_code");
        index.setUnique(true);
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("code", RDBIndexMetadata.IndexSort.asc));
        table.addIndex(index);

        SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(table);
        Assert.assertTrue(request instanceof BatchSqlRequest);

        BatchSqlRequest batch = (BatchSqlRequest) request;
        Assert.assertEquals(
            "create table `public`.test_create_sql ( `id` int not null primary key auto_increment comment 'pk' , `code` varchar(32) not null comment 'code' , `created_at` timestamp without time zone default now() , `metadata` jsonb not null , `nickname` varchar(64) ) comment= 'demo table'",
            normalizeSql(batch.getSql())
        );
        Assert.assertEquals(1, batch.getBatch().size());
        Assert.assertEquals(
            "create unique index if not exists idx_test_create_sql_code on `public`.test_create_sql ( `code` asc )",
            normalizeSql(batch.getBatch().get(0).getSql())
        );
    }

    @Test
    public void testBuildCompositePrimaryKey() {
        KingbaseMysqlSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("test_composite_pk");

        RDBColumnMetadata colA = new RDBColumnMetadata();
        colA.setName("col_a");
        colA.setSortIndex(1);
        colA.setJdbcType(JDBCType.INTEGER, Integer.class);
        colA.setPrimaryKey(true);
        colA.setNotNull(true);
        table.addColumn(colA);

        RDBColumnMetadata colB = new RDBColumnMetadata();
        colB.setName("col_b");
        colB.setSortIndex(2);
        colB.setJdbcType(JDBCType.VARCHAR, String.class);
        colB.setLength(64);
        colB.setPrimaryKey(true);
        colB.setNotNull(true);
        table.addColumn(colB);

        SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(table);
        BatchSqlRequest batch = (BatchSqlRequest) request;
        String sql = normalizeSql(batch.getSql());
        Assert.assertTrue(sql.contains("`col_a` int not null primary key"));
        Assert.assertTrue(sql.contains("`col_b` varchar(64) not null primary key"));
    }

    @Test
    public void testBuildMultipleIndexes() {
        KingbaseMysqlSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("test_multi_idx");

        RDBColumnMetadata id = new RDBColumnMetadata();
        id.setName("id");
        id.setJdbcType(JDBCType.INTEGER, Integer.class);
        id.setPrimaryKey(true);
        id.setNotNull(true);
        table.addColumn(id);

        RDBColumnMetadata name = new RDBColumnMetadata();
        name.setName("name");
        name.setJdbcType(JDBCType.VARCHAR, String.class);
        name.setLength(128);
        table.addColumn(name);

        RDBColumnMetadata code = new RDBColumnMetadata();
        code.setName("code");
        code.setJdbcType(JDBCType.VARCHAR, String.class);
        code.setLength(64);
        table.addColumn(code);

        RDBIndexMetadata idxName = new RDBIndexMetadata();
        idxName.setName("idx_name");
        idxName.getColumns().add(RDBIndexMetadata.IndexColumn.of("name", RDBIndexMetadata.IndexSort.asc));
        table.addIndex(idxName);

        RDBIndexMetadata idxCode = new RDBIndexMetadata();
        idxCode.setName("idx_code_unique");
        idxCode.setUnique(true);
        idxCode.getColumns().add(RDBIndexMetadata.IndexColumn.of("code", RDBIndexMetadata.IndexSort.asc));
        table.addIndex(idxCode);

        SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(table);
        BatchSqlRequest batch = (BatchSqlRequest) request;
        Assert.assertTrue(batch.getBatch().size() >= 2);
    }

    @Test
    public void testBuildTableWithoutAutoIncrement() {
        KingbaseMysqlSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("test_no_auto_inc");

        RDBColumnMetadata id = new RDBColumnMetadata();
        id.setName("id");
        id.setJdbcType(JDBCType.INTEGER, Integer.class);
        id.setPrimaryKey(true);
        id.setNotNull(true);
        id.setDefaultValue(NativeSqlDefaultValue.of("0"));
        table.addColumn(id);

        RDBColumnMetadata name = new RDBColumnMetadata();
        name.setName("name");
        name.setJdbcType(JDBCType.VARCHAR, String.class);
        name.setLength(128);
        table.addColumn(name);

        SqlRequest request = schema.findFeatureNow(CreateTableSqlBuilder.ID).build(table);
        BatchSqlRequest batch = (BatchSqlRequest) request;
        String sql = normalizeSql(batch.getSql());
        Assert.assertTrue(sql.contains("`id` int not null primary key default 0"));
        Assert.assertFalse(sql.contains("auto_increment"));
    }

    private KingbaseMysqlSchemaMetadata newSchema() {
        KingbaseMysqlDialect dialect = new KingbaseMysqlDialect();
        RDBDatabaseMetadata database = new RDBDatabaseMetadata(dialect);
        KingbaseMysqlSchemaMetadata schema = new KingbaseMysqlSchemaMetadata("public");
        database.addSchema(schema);
        database.setCurrentSchema(schema);
        return schema;
    }

    private String normalizeSql(String sql) {
        return sql.replaceAll("\\s+", " ").trim();
    }
}
