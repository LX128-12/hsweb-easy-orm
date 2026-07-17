package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.mapping.defaults.record.Record;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.junit.Assert;
import org.junit.Test;

import java.sql.JDBCType;

public class KingbaseOracleTableMetadataParserUnitTest {

    @Test
    public void testShouldApplyLengthPrecisionAndScaleFromLengthSupportWhenMissing() {
        TestParser parser = new TestParser();
        RDBColumnMetadata column = new RDBColumnMetadata();
        column.setLength(-1);
        column.setPrecision(-1);
        column.setScale(-1);

        Record record = Record.newRecord()
                              .putValue("name", "title")
                              .putValue("data_type", "varchar2")
                              .putValue("data_length", 64)
                              .putValue("not_null", "1");

        parser.apply(column, record);

        Assert.assertEquals("title", column.getName());
        Assert.assertEquals(64, column.getLength());
        Assert.assertTrue(column.isNotNull());
    }

    @Test
    public void testShouldKeepExistingParsedLengthPrecisionAndScale() {
        TestParser parser = new TestParser();
        RDBColumnMetadata column = new RDBColumnMetadata();
        column.setLength(24);
        column.setPrecision(18);
        column.setScale(4);

        Record record = Record.newRecord()
                              .putValue("name", "amount")
                              .putValue("data_type", "number")
                              .putValue("data_precision", 18)
                              .putValue("data_scale", 4);

        parser.apply(column, record);

        Assert.assertEquals(24, column.getLength());
        Assert.assertEquals(18, column.getPrecision());
        Assert.assertEquals(4, column.getScale());
    }

    private static class TestParser extends KingbaseOracleTableMetadataParser {

        private TestParser() {
            super(new KingbaseOracleSchemaMetadata("public"));
        }

        private void apply(RDBColumnMetadata column, Record record) {
            super.applyColumnInfo(column, record);
        }
    }
}