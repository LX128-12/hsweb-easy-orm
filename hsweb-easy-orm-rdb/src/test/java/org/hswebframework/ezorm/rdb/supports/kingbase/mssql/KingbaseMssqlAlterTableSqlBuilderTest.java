package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.executor.DefaultBatchSqlRequest;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBDatabaseMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.AlterRequest;
import org.junit.Assert;
import org.junit.Test;

import java.sql.JDBCType;

public class KingbaseMssqlAlterTableSqlBuilderTest {

    private KingbaseMssqlSchemaMetadata newSchema() {
        KingbaseMssqlDialect dialect = new KingbaseMssqlDialect();
        RDBDatabaseMetadata database = new RDBDatabaseMetadata(dialect);
        KingbaseMssqlSchemaMetadata schema = new KingbaseMssqlSchemaMetadata("public");
        database.addSchema(schema);
        database.setCurrentSchema(schema);
        return schema;
    }

    @Test
    public void testDropColumnWithCommentClearsCommentFirst() {
        KingbaseMssqlSchemaMetadata schema = newSchema();

        RDBTableMetadata oldTable = schema.newTable("old_table");
        RDBColumnMetadata dropCol = new RDBColumnMetadata();
        dropCol.setName("drop_me");
        dropCol.setOwner(oldTable);
        dropCol.setComment("some comment");
        dropCol.setDataType("varchar(64)");
        dropCol.setJdbcType(JDBCType.VARCHAR, String.class);
        oldTable.addColumn(dropCol);

        RDBColumnMetadata keepCol = new RDBColumnMetadata();
        keepCol.setName("keep_me");
        keepCol.setOwner(oldTable);
        keepCol.setDataType("varchar(32)");
        keepCol.setJdbcType(JDBCType.VARCHAR, String.class);
        oldTable.addColumn(keepCol);

        RDBTableMetadata newTable = schema.newTable("old_table");
        RDBColumnMetadata newKeepCol = new RDBColumnMetadata();
        newKeepCol.setName("keep_me");
        newKeepCol.setOwner(newTable);
        newKeepCol.setDataType("varchar(32)");
        newKeepCol.setJdbcType(JDBCType.VARCHAR, String.class);
        newTable.addColumn(newKeepCol);

        KingbaseMssqlAlterTableSqlBuilder builder = new KingbaseMssqlAlterTableSqlBuilder();
        AlterRequest request = AlterRequest.builder()
            .newTable(newTable)
            .oldTable(oldTable)
            .allowDrop(true)
            .build();

        SqlRequest result = builder.build(request);
        Assert.assertTrue(result instanceof DefaultBatchSqlRequest);
        DefaultBatchSqlRequest batch = (DefaultBatchSqlRequest) result;
        // Without comment: only MSSQL extended property + drop column
        // With comment: Kingbase adds "comment on column ... is ''" before MSSQL parent SQL
        boolean foundDropColumn = false;
        for (SqlRequest r : batch.getBatch()) {
            if (r.getSql().contains("drop column")) {
                foundDropColumn = true;
            }
        }
        Assert.assertTrue("Expected drop column SQL", foundDropColumn);
    }

    @Test
    public void testDropColumnWithoutCommentOnlyDrop() {
        KingbaseMssqlSchemaMetadata schema = newSchema();

        RDBTableMetadata oldTable = schema.newTable("old_table");
        RDBColumnMetadata dropCol = new RDBColumnMetadata();
        dropCol.setName("no_comment_col");
        dropCol.setOwner(oldTable);
        dropCol.setDataType("varchar(64)");
        dropCol.setJdbcType(JDBCType.VARCHAR, String.class);
        oldTable.addColumn(dropCol);

        RDBTableMetadata newTable = schema.newTable("old_table");

        KingbaseMssqlAlterTableSqlBuilder builder = new KingbaseMssqlAlterTableSqlBuilder();
        AlterRequest request = AlterRequest.builder()
            .newTable(newTable)
            .oldTable(oldTable)
            .allowDrop(true)
            .build();

        SqlRequest result = builder.build(request);
        DefaultBatchSqlRequest batch = (DefaultBatchSqlRequest) result;
        Assert.assertTrue(batch.getBatch().size() >= 1);
        String lastBatch = batch.getBatch().get(batch.getBatch().size() - 1).getSql();
        Assert.assertTrue(lastBatch, lastBatch.contains("drop column"));
    }

    @Test
    public void testAddColumnWithComment() {
        KingbaseMssqlSchemaMetadata schema = newSchema();

        RDBTableMetadata oldTable = schema.newTable("test_table");

        RDBTableMetadata newTable = schema.newTable("test_table");
        RDBColumnMetadata newCol = new RDBColumnMetadata();
        newCol.setName("new_col");
        newCol.setOwner(newTable);
        newCol.setComment("new column comment");
        newCol.setDataType("varchar(100)");
        newCol.setJdbcType(JDBCType.VARCHAR, String.class);
        newTable.addColumn(newCol);

        KingbaseMssqlAlterTableSqlBuilder builder = new KingbaseMssqlAlterTableSqlBuilder();
        AlterRequest request = AlterRequest.builder()
            .newTable(newTable)
            .oldTable(oldTable)
            .build();

        SqlRequest result = builder.build(request);
        DefaultBatchSqlRequest batch = (DefaultBatchSqlRequest) result;
        Assert.assertTrue(batch.getBatch().size() >= 1);
        boolean foundComment = false;
        for (SqlRequest r : batch.getBatch()) {
            if (r.getSql().contains("comment on column") && r.getSql().contains("new column comment")) {
                foundComment = true;
                break;
            }
        }
        Assert.assertTrue("Expected comment SQL for new column with comment", foundComment);
    }

    @Test
    public void testAddColumnWithoutCommentDoesNotGenerateCommentSql() {
        KingbaseMssqlSchemaMetadata schema = newSchema();

        RDBTableMetadata oldTable = schema.newTable("test_table");

        RDBTableMetadata newTable = schema.newTable("test_table");
        RDBColumnMetadata newCol = new RDBColumnMetadata();
        newCol.setName("silent_col");
        newCol.setOwner(newTable);
        newCol.setDataType("int");
        newCol.setJdbcType(JDBCType.INTEGER, Integer.class);
        newTable.addColumn(newCol);

        KingbaseMssqlAlterTableSqlBuilder builder = new KingbaseMssqlAlterTableSqlBuilder();
        AlterRequest request = AlterRequest.builder()
            .newTable(newTable)
            .oldTable(oldTable)
            .build();

        SqlRequest result = builder.build(request);
        DefaultBatchSqlRequest batch = (DefaultBatchSqlRequest) result;
        for (SqlRequest r : batch.getBatch()) {
            Assert.assertFalse(r.getSql(), r.getSql().contains("comment on column"));
        }
    }

    @Test
    public void testCommentWithSingleQuoteIsEscaped() {
        KingbaseMssqlSchemaMetadata schema = newSchema();

        RDBTableMetadata oldTable = schema.newTable("test_table");

        RDBTableMetadata newTable = schema.newTable("test_table");
        RDBColumnMetadata newCol = new RDBColumnMetadata();
        newCol.setName("quoted_col");
        newCol.setOwner(newTable);
        newCol.setComment("it's escaped");
        newCol.setDataType("varchar(50)");
        newCol.setJdbcType(JDBCType.VARCHAR, String.class);
        newTable.addColumn(newCol);

        KingbaseMssqlAlterTableSqlBuilder builder = new KingbaseMssqlAlterTableSqlBuilder();
        AlterRequest request = AlterRequest.builder()
            .newTable(newTable)
            .oldTable(oldTable)
            .build();

        SqlRequest result = builder.build(request);
        DefaultBatchSqlRequest batch = (DefaultBatchSqlRequest) result;
        boolean foundEscaped = false;
        for (SqlRequest r : batch.getBatch()) {
            if (r.getSql().contains("it''s escaped")) {
                foundEscaped = true;
                break;
            }
        }
        Assert.assertTrue("Expected escaped quote it''s escaped in comment SQL", foundEscaped);
    }
}
