package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBIndexMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateIndexParameter;
import org.junit.Assert;
import org.junit.Test;

import java.sql.JDBCType;

public class KingbaseMysqlCreateIndexSqlBuilderTest {

    private KingbaseMysqlSchemaMetadata newSchema() {
        KingbaseMysqlDialect dialect = new KingbaseMysqlDialect();
        RDBDatabaseMetadata database = new RDBDatabaseMetadata(dialect);
        KingbaseMysqlSchemaMetadata schema = new KingbaseMysqlSchemaMetadata("public");
        database.addSchema(schema);
        database.setCurrentSchema(schema);
        return schema;
    }

    private RDBTableMetadata buildTable() {
        KingbaseMysqlSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("test_table");

        RDBColumnMetadata colId = new RDBColumnMetadata();
        colId.setName("id");
        colId.setOwner(table);
        colId.setDataType("int");
        colId.setJdbcType(JDBCType.INTEGER, Integer.class);
        table.addColumn(colId);

        RDBColumnMetadata colName = new RDBColumnMetadata();
        colName.setName("name");
        colName.setOwner(table);
        colName.setDataType("varchar(128)");
        colName.setJdbcType(JDBCType.VARCHAR, String.class);
        table.addColumn(colName);

        RDBColumnMetadata colCode = new RDBColumnMetadata();
        colCode.setName("code");
        colCode.setOwner(table);
        colCode.setDataType("varchar(64)");
        colCode.setJdbcType(JDBCType.VARCHAR, String.class);
        table.addColumn(colCode);

        return table;
    }

    @Test
    public void testBuildPlainIndex() {
        RDBTableMetadata table = buildTable();

        RDBIndexMetadata index = new RDBIndexMetadata();
        index.setName("idx_test_name");
        index.setUnique(false);
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("name", RDBIndexMetadata.IndexSort.asc));

        SqlRequest result = KingbaseMysqlCreateIndexSqlBuilder.INSTANCE.build(
            CreateIndexParameter.of(table, index));

        String sql = result.getSql();
        Assert.assertTrue(sql, sql.contains("create"));
        Assert.assertTrue(sql, sql.contains("index if not exists"));
        Assert.assertTrue(sql, sql.contains("idx_test_name"));
        Assert.assertTrue(sql, sql.contains("`public`.test_table"));
        Assert.assertFalse(sql, sql.contains("unique"));
    }

    @Test
    public void testBuildUniqueIndex() {
        RDBTableMetadata table = buildTable();

        RDBIndexMetadata index = new RDBIndexMetadata();
        index.setName("idx_code_unique");
        index.setUnique(true);
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("code", RDBIndexMetadata.IndexSort.asc));

        SqlRequest result = KingbaseMysqlCreateIndexSqlBuilder.INSTANCE.build(
            CreateIndexParameter.of(table, index));

        String sql = result.getSql();
        Assert.assertTrue(sql, sql.contains("unique"));
    }

    @Test
    public void testBuildCompositeIndex() {
        RDBTableMetadata table = buildTable();

        RDBIndexMetadata index = new RDBIndexMetadata();
        index.setName("idx_test_composite");
        index.setUnique(false);
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("name", RDBIndexMetadata.IndexSort.asc));
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("code", RDBIndexMetadata.IndexSort.desc));

        SqlRequest result = KingbaseMysqlCreateIndexSqlBuilder.INSTANCE.build(
            CreateIndexParameter.of(table, index));

        String sql = result.getSql();
        Assert.assertTrue(sql, sql.contains("`name` asc"));
        Assert.assertTrue(sql, sql.contains("`code` desc"));
        Assert.assertTrue(sql, sql.contains(","));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBuildIndexWithNonExistentColumnThrows() {
        RDBTableMetadata table = buildTable();

        RDBIndexMetadata index = new RDBIndexMetadata();
        index.setName("idx_bad");
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("nonexistent", RDBIndexMetadata.IndexSort.asc));

        KingbaseMysqlCreateIndexSqlBuilder.INSTANCE.build(
            CreateIndexParameter.of(table, index));
    }
}
