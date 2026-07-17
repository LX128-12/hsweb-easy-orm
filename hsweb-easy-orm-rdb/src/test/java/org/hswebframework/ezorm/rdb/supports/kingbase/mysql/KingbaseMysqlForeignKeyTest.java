package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.dml.query.Selects;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseMysqlForeignKeyTest extends AbstractKingbaseMysqlJdbcTestSupport {

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
                .orderBy(SortOrder.asc("name"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(2, result.size());
        } finally {
            dropTableQuietly(qualifiedChild);
            dropTableQuietly(qualifiedParent);
        }
    }
}
