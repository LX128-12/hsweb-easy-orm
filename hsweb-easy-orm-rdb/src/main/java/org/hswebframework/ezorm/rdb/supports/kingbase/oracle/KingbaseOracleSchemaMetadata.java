package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import org.hswebframework.ezorm.rdb.codec.EnumValueCodec;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.CompositeExceptionTranslation;
import org.hswebframework.ezorm.rdb.supports.oracle.*;

public class KingbaseOracleSchemaMetadata extends RDBSchemaMetadata {

    public KingbaseOracleSchemaMetadata(String name) {
        super(name);

        addFeature(new KingbaseOracleCreateTableSqlBuilder());
        addFeature(new KingbaseOracleAlterTableSqlBuilder());
        addFeature(KingbaseOracleCreateIndexSqlBuilder.INSTANCE);
        addFeature(new OraclePaginator());

        addFeature(new KingbaseOracleTableMetadataParser(this));
        addFeature(KingbaseOracleIndexMetadataParser.of(this));
        addFeature(new KingbaseOracleDialect());

        addFeature(new CompositeExceptionTranslation()
            .add(KingbaseOracleJDBCExceptionTranslation.of(this)));
    }

    @Override
    public void addTable(RDBTableMetadata metadata) {
        metadata.addFeature(OracleInsertSqlBuilder.of(metadata));
        super.addTable(metadata);
    }

    @Override
    public RDBTableMetadata newTable(String name) {
        RDBTableMetadata metadata = super.newTable(name);
        metadata.addFeature(OracleInsertSqlBuilder.of(metadata));
        metadata.addFeature(new OracleBatchUpsertOperator(metadata));
        metadata.setOnColumnAdded(column -> {
            if (column.getValueCodec() instanceof EnumValueCodec
                && ((EnumValueCodec) column.getValueCodec()).isToMask()) {
                column.addFeature(OracleEnumInFragmentBuilder.in);
                column.addFeature(OracleEnumInFragmentBuilder.notIn);
            }
        });
        return metadata;
    }
}