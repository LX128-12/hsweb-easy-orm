package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.hswebframework.ezorm.rdb.operator.dml.query.SortOrder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseOracleSequenceTest extends AbstractKingbaseOracleJdbcTestSupport {

    @Test
    public void testSequenceForAutoIncrement() {
        String table = randomName("test_seq_");
        String sequence = table + "_seq";
        String qualified = currentSchema() + "." + table;
        String qualifiedSeq = currentSchema() + "." + sequence;

        execute("create sequence " + qualifiedSeq + " start with 1 increment by 1");
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            execute("insert into " + qualified + " values (nextval('" + qualifiedSeq + "'), 'first')");
            execute("insert into " + qualified + " values (nextval('" + qualifiedSeq + "'), 'second')");
            execute("insert into " + qualified + " values (nextval('" + qualifiedSeq + "'), 'third')");

            DatabaseOperator operator = newOperator(currentSchema());
            List<Map<String, Object>> rows = operator.dml()
                .query(table)
                .orderBy(SortOrder.asc("id"))
                .fetch(mapList())
                .sync();
            Assert.assertEquals(3, rows.size());
            Assert.assertEquals("first", rows.get(0).get("name"));
            Assert.assertEquals("second", rows.get(1).get("name"));
            Assert.assertEquals("third", rows.get(2).get("name"));
            Assert.assertEquals(1, ((Number) rows.get(0).get("id")).intValue());
            Assert.assertEquals(2, ((Number) rows.get(1).get("id")).intValue());
            Assert.assertEquals(3, ((Number) rows.get(2).get("id")).intValue());
        } finally {
            dropTableQuietly(qualified);
            try { execute("drop sequence " + qualifiedSeq); } catch (Exception ignore) {}
        }
    }

    @Test
    public void testInsertWithoutSequenceUsesExplicitId() {
        String table = randomName("test_no_seq_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified + " (id int primary key, name varchar(64))");
        try {
            DatabaseOperator operator = newOperator(currentSchema());
            operator.dml()
                .insert(table)
                .value("id", 100)
                .value("name", "explicit_id")
                .execute()
                .sync();

            Map<String, Object> row = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 100))
                .fetch(singleMap())
                .sync();
            Assert.assertEquals("explicit_id", row.get("name"));
        } finally {
            dropTableQuietly(qualified);
        }
    }
}
