package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseMysqlAlterOperationsTest extends AbstractKingbaseMysqlJdbcTestSupport {

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
            Map<String, Object> row = operator.dml()
                .query(newTable)
                .where(dsl -> dsl.is("id", 1))
                .fetch(singleMap())
                .sync();
            Assert.assertEquals("value", row.get("name"));
        } finally {
            dropTableQuietly(qualifiedNew);
            dropTableQuietly(qualified);
        }
    }
}
