package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.codec.EnumValueCodec;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.CompositeExceptionTranslation;
import org.hswebframework.ezorm.rdb.supports.mssql.*;

public class KingbaseMssqlSchemaMetadata extends RDBSchemaMetadata {

    public KingbaseMssqlSchemaMetadata(String name) {
        super(name);

        addFeature(new KingbaseMssqlCreateTableSqlBuilder());
        addFeature(new KingbaseMssqlAlterTableSqlBuilder());
        addFeature(KingbaseMssqlCreateIndexSqlBuilder.INSTANCE);
        addFeature(new SqlServer2012Paginator());
        addFeature(new KingbaseMssqlTableMetadataParser(this));
        addFeature(new KingbaseMssqlIndexMetadataParser(this));
        addFeature(new KingbaseMssqlDialect());

        addFeature(new CompositeExceptionTranslation()
            .add(KingbaseMssqlJDBCExceptionTranslation.of(this)));
    }

    @Override
    public RDBTableMetadata newTable(String name) {
        RDBTableMetadata metadata = super.newTable(name);
        metadata.addFeature(new SqlServerBatchUpsertOperator(metadata));
        metadata.setOnColumnAdded(column -> {
            if (column.getValueCodec() instanceof EnumValueCodec && ((EnumValueCodec) column.getValueCodec()).isToMask()) {
                column.addFeature(SqlServerEnumInFragmentBuilder.in);
                column.addFeature(SqlServerEnumInFragmentBuilder.notIn);
            }
        });
        return metadata;
    }

    @Override
    public void addTable(RDBTableMetadata metadata) {
        metadata.addFeature(new SqlServerBatchUpsertOperator(metadata));
        super.addTable(metadata);
    }
}
