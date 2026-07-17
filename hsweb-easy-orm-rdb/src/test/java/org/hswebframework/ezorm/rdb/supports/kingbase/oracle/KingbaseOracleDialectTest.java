package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.metadata.DataType;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.junit.Assert;
import org.junit.Test;

import java.sql.JDBCType;

public class KingbaseOracleDialectTest {

    @Test
    public void testBasicProperties() {
        KingbaseOracleDialect dialect = new KingbaseOracleDialect();

        Assert.assertEquals("kingbase-oracle", dialect.getId());
        Assert.assertEquals("KingbaseOracle", dialect.getName());
        Assert.assertTrue(dialect.isColumnToUpperCase());

        Assert.assertEquals("\"TEST\"", dialect.quote("test"));
        Assert.assertEquals("\"", dialect.getQuoteStart());
        Assert.assertEquals("\"", dialect.getQuoteEnd());
    }

    @Test
    public void testOracleCompatibleTypes() {
        KingbaseOracleDialect dialect = new KingbaseOracleDialect();

        DataType varchar2 = dialect.convertDataType("varchar2");
        Assert.assertEquals(JDBCType.VARCHAR, varchar2.getSqlType());
        Assert.assertEquals(String.class, varchar2.getJavaType());

        RDBColumnMetadata varcharCol = new RDBColumnMetadata();
        varcharCol.setLength(64);
        varcharCol.setType(varchar2);
        String ddl = dialect.buildColumnDataType(varcharCol);
        Assert.assertTrue(ddl, ddl.startsWith("varchar2(64"));

        DataType number = dialect.convertDataType("number");
        Assert.assertEquals(JDBCType.NUMERIC, number.getSqlType());

        DataType clob = dialect.convertDataType("clob");
        Assert.assertEquals(JDBCType.LONGVARCHAR, clob.getSqlType());

        DataType date = dialect.convertDataType("date");
        Assert.assertEquals(JDBCType.TIMESTAMP, date.getSqlType());
    }
}