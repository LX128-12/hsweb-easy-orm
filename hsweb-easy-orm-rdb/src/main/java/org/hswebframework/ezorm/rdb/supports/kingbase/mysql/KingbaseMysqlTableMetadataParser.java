package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.mapping.defaults.record.Record;
import org.hswebframework.ezorm.rdb.metadata.LengthSupport;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.mysql.MysqlTableMetadataParser;

public class KingbaseMysqlTableMetadataParser extends MysqlTableMetadataParser {

    public KingbaseMysqlTableMetadataParser(RDBSchemaMetadata schema) {
        super(schema);
    }

    @Override
    protected void applyColumnInfo(RDBColumnMetadata column, Record record) {
        super.applyColumnInfo(column, record);
        if (column.getType() instanceof LengthSupport) {
            LengthSupport lengthSupport = (LengthSupport) column.getType();
            if (column.getLength() < 0) {
                column.setLength(lengthSupport.getLength());
            }
            if (column.getPrecision() < 0) {
                column.setPrecision(lengthSupport.getPrecision());
            }
            if (column.getScale() < 0) {
                column.setScale(lengthSupport.getScale());
            }
        }
    }
}
