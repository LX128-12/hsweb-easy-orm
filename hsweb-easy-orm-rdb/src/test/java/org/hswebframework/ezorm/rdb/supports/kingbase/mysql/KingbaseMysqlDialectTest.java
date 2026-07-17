package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.metadata.DataType;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.junit.Assert;
import org.junit.Test;

import java.sql.JDBCType;
import java.sql.Timestamp;

public class KingbaseMysqlDialectTest {

    @Test
    public void testCustomMysqlCompatibleTypes() {
        KingbaseMysqlDialect dialect = new KingbaseMysqlDialect();

        Assert.assertEquals("kingbase-mysql", dialect.getId());
        Assert.assertEquals("KingbaseMysql", dialect.getName());
        Assert.assertEquals("`test_table`", dialect.quote("test_table"));
        Assert.assertFalse(dialect.isColumnToUpperCase());

        RDBColumnMetadata varying = new RDBColumnMetadata();
        varying.setLength(64);
        varying.setType(dialect.convertDataType("character varying"));
        Assert.assertEquals("varchar(64)", dialect.buildColumnDataType(varying));
        Assert.assertEquals(JDBCType.VARCHAR, varying.getSqlType());
        Assert.assertEquals(String.class, varying.getJavaType());

        RDBColumnMetadata character = new RDBColumnMetadata();
        character.setLength(12);
        character.setType(dialect.convertDataType("character"));
        Assert.assertEquals("char(12)", dialect.buildColumnDataType(character));
        Assert.assertEquals(JDBCType.CHAR, character.getSqlType());
        Assert.assertEquals(String.class, character.getJavaType());

        DataType integer = dialect.convertDataType("integer");
        Assert.assertEquals(JDBCType.INTEGER, integer.getSqlType());
        Assert.assertEquals(Integer.class, integer.getJavaType());

        DataType timestamp = dialect.convertDataType("timestamp without time zone");
        Assert.assertEquals(JDBCType.TIMESTAMP, timestamp.getSqlType());
        Assert.assertEquals(Timestamp.class, timestamp.getJavaType());

        DataType time = dialect.convertDataType("time without time zone");
        Assert.assertEquals(JDBCType.TIME, time.getSqlType());
        Assert.assertEquals(java.sql.Time.class, time.getJavaType());
    }
}
