package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.operator.DatabaseOperator;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

public class KingbaseMssqlDefaultValueTest extends AbstractKingbaseMssqlJdbcTestSupport {

    @Test
    public void testDefaultNumericValue() {
        String table = randomName("test_def_num_");
        String qualified = currentSchema() + "." + table;
        execute("create table " + qualified
            + " (id int primary key, score int default 0, name varchar(64))");
        try {
            DatabaseOperator operator = newOperator(currentSchema());
            operator.dml()
                .insert(table)
                .value("id", 1)
                .value("name", "no_score")
                .execute()
                .sync();

            Map<String, Object> row = operator.dml()
                .query(table)
                .where(dsl -> dsl.is("id", 1))
                .fetch(singleMap())
                .sync();
            Assert.assertEquals(0, ((Number) row.get("score")).intValue());
        } finally {
            dropTableQuietly(qualified);
        }
    }
}
