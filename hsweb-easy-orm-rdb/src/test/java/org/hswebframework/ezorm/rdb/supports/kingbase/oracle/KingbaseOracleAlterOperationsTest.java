package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseOracleAlterOperationsTest extends AbstractKingbaseOracleJdbcTestSupport {

    @Test
    public void testRenameColumn() {
        String table = randomName("test_rename_col_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, old_name varchar(64))");
        try {
            execute("insert into " + qualified + " values (1, 'value')");
            execute("alter table " + qualified + " rename column old_name to new_name");

            DatabaseOperator operator = newOperator(currentSchema());
            Map<String, Object> row = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 1))
                .fetch(singleMap())
                .sync();
            Assert.assertEquals("value", row.get("new_name"));
        } finally {
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testRenameTable() {
        String table = randomName("test_rename_tbl_");
        String qualified = currentSchema() + "." + table;
        String newTable = table + "_renamed";
        String qualifiedNew = currentSchema() + "." + newTable;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            execute("insert into " + qualified + " values (1, 'value')");
            execute("alter table " + qualified + " rename to " + newTable);

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> rows = operator.dml()
                .query(newTable)
                .fetch(mapList())
                .sync();
            Assert.assertEquals(1, rows.size());
            Assert.assertEquals("value", rows.get(0).get("name"));
        } finally {
            dropTableQuietly(qualifiedNew);
            dropTableQuietly(qualified);
        }
    }

    @Test
    public void testAddAndDropUniqueConstraint() {
        String table = randomName("test_alter_unique_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, code varchar(64))");
        try {
            execute("alter table " + qualified + " add constraint uq_test_code unique (code)");
            execute("insert into " + qualified + " values (1, 'code_a')");
            execute("insert into " + qualified + " values (2, 'code_b')");

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> rows = operator.dml()
                .query(table)
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, rows.size());

            execute("alter table " + qualified + " drop constraint uq_test_code");
        } finally {
            dropTableQuietly(qualified);
        }
    }
}
