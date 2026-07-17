package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.dml.query.Selects;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseOracleForeignKeyTest extends AbstractKingbaseOracleJdbcTestSupport {

    @Test
    public void testForeignKeyConstraint() {
        String parentTable = randomName("test_fk_parent_");
        String childTable = randomName("test_fk_child_");
        String qualifiedParent = currentSchema() + "." + parentTable;
        String qualifiedChild = currentSchema() + "." + childTable;

        execute("create table " + qualifiedParent + " (id int primary key, name varchar(64))");
        execute("create table " + qualifiedChild
            + " (id int primary key, parent_id int, description varchar(64),"
            + " foreign key (parent_id) references " + qualifiedParent + "(id))");
        try {
            execute("insert into " + qualifiedParent + " values (1, 'Parent A')");
            execute("insert into " + qualifiedParent + " values (2, 'Parent B')");
            execute("insert into " + qualifiedChild + " values (10, 1, 'Child of A')");
            execute("insert into " + qualifiedChild + " values (20, 2, 'Child of B')");

            DatabaseOperator operator = newOperator(currentSchema());

            List<Map<String, Object>> result = operator.dml()
                .query(childTable)
                .select(Selects.column("description"), Selects.column("name"))
                .leftJoin(parentTable, join -> join.on(cdt -> cdt.is("parent_id", "id")))
                .orderBy(SortOrder.asc("description"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, result.size());
            Assert.assertEquals("Child of A", result.get(0).get("description"));
        } finally {
            dropTableQuietly(qualifiedChild);
            dropTableQuietly(qualifiedParent);
        }
    }

    @Test
    public void testForeignKeyCascade() {
        String parentTable = randomName("test_fkc_parent_");
        String childTable = randomName("test_fkc_child_");
        String qualifiedParent = currentSchema() + "." + parentTable;
        String qualifiedChild = currentSchema() + "." + childTable;

        execute("create table " + qualifiedParent + " (id int primary key, name varchar(64))");
        execute("create table " + qualifiedChild
            + " (id int primary key, parent_id int, description varchar(64),"
            + " foreign key (parent_id) references " + qualifiedParent + "(id) on delete cascade)");
        try {
            execute("insert into " + qualifiedParent + " values (1, 'Parent')");
            execute("insert into " + qualifiedChild + " values (10, 1, 'Child')");

            execute("delete from " + qualifiedParent + " where id = 1");

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> children = operator.dml()
                .query(childTable)
                .fetch(mapList())
                .sync();
            Assert.assertEquals(0, children.size());
        } finally {
            dropTableQuietly(qualifiedChild);
            dropTableQuietly(qualifiedParent);
        }
    }
}
