package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBIndexMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateIndexParameter;
import org.junit.Assert;
import org.junit.Test;

import java.sql.JDBCType;

public class KingbaseOracleCreateIndexSqlBuilderTest {

    private KingbaseOracleSchemaMetadata newSchema() {
        KingbaseOracleDialect dialect = new KingbaseOracleDialect();
        RDBDatabaseMetadata database = new RDBDatabaseMetadata(dialect);
        KingbaseOracleSchemaMetadata schema = new KingbaseOracleSchemaMetadata("TEST");
        database.addSchema(schema);
        database.setCurrentSchema(schema);
        return schema;
    }

    private RDBTableMetadata buildTable() {
        KingbaseOracleSchemaMetadata schema = newSchema();
        RDBTableMetadata table = schema.newTable("test_table");

        RDBColumnMetadata colId = new RDBColumnMetadata();
        colId.setName("ID");
        colId.setOwner(table);
        colId.setDataType("number(20,0)");
        colId.setJdbcType(JDBCType.NUMERIC, Long.class);
        table.addColumn(colId);

        RDBColumnMetadata colName = new RDBColumnMetadata();
        colName.setName("NAME");
        colName.setOwner(table);
        colName.setDataType("varchar2(128)");
        colName.setJdbcType(JDBCType.VARCHAR, String.class);
        table.addColumn(colName);

        RDBColumnMetadata colCode = new RDBColumnMetadata();
        colCode.setName("CODE");
        colCode.setOwner(table);
        colCode.setDataType("varchar2(64)");
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
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("NAME", RDBIndexMetadata.IndexSort.asc));

        SqlRequest result = KingbaseOracleCreateIndexSqlBuilder.INSTANCE.build(
            CreateIndexParameter.of(table, index));

        String sql = result.getSql();
        Assert.assertTrue(sql, sql.contains("create"));
        Assert.assertTrue(sql, sql.contains("index if not exists"));
        Assert.assertTrue(sql, sql.contains("idx_test_name"));
        Assert.assertTrue(sql, sql.contains("\"TEST\".test_table"));
        Assert.assertFalse(sql, sql.contains("unique"));
    }

    @Test
    public void testBuildUniqueIndex() {
        RDBTableMetadata table = buildTable();

        RDBIndexMetadata index = new RDBIndexMetadata();
        index.setName("idx_test_unique_code");
        index.setUnique(true);
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("CODE", RDBIndexMetadata.IndexSort.asc));

        SqlRequest result = KingbaseOracleCreateIndexSqlBuilder.INSTANCE.build(
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
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("NAME", RDBIndexMetadata.IndexSort.asc));
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("CODE", RDBIndexMetadata.IndexSort.desc));

        SqlRequest result = KingbaseOracleCreateIndexSqlBuilder.INSTANCE.build(
            CreateIndexParameter.of(table, index));

        String sql = result.getSql();
        Assert.assertTrue(sql, sql.contains("\"NAME\" asc"));
        Assert.assertTrue(sql, sql.contains("\"CODE\" desc"));
        Assert.assertTrue(sql, sql.contains(","));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBuildIndexWithNonExistentColumnThrows() {
        RDBTableMetadata table = buildTable();

        RDBIndexMetadata index = new RDBIndexMetadata();
        index.setName("idx_bad");
        index.getColumns().add(RDBIndexMetadata.IndexColumn.of("NONEXISTENT", RDBIndexMetadata.IndexSort.asc));

        KingbaseOracleCreateIndexSqlBuilder.INSTANCE.build(
            CreateIndexParameter.of(table, index));
    }
}
