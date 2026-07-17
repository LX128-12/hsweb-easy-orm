package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.core.DefaultValue;
import org.hswebframework.ezorm.rdb.executor.DefaultBatchSqlRequest;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBIndexMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateIndexParameter;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateIndexSqlBuilder;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateTableSqlBuilder;

import static org.hswebframework.ezorm.rdb.executor.SqlRequests.*;

@SuppressWarnings("all")
@Getter
@Setter
public class KingbaseOracleCreateTableSqlBuilder implements CreateTableSqlBuilder {

    @Override
    public SqlRequest build(RDBTableMetadata table) {
        DefaultBatchSqlRequest sql = new DefaultBatchSqlRequest();

        PrepareSqlFragments createTable = PrepareSqlFragments.of();
        createTable.addSql("create table", table.getFullName(), "(");

        int index = 0;
        for (RDBColumnMetadata column : table.getColumns()) {
            if (index++ != 0) {
                createTable.addSql(",");
            }
            createTable.addSql(column.getQuoteName());
            if (column.getColumnDefinition() != null) {
                createTable.addSql(column.getColumnDefinition());
            } else {
                createTable.addSql(column.getDialect().buildColumnDataType(column));
                DefaultValue defaultValue = column.getDefaultValue();
                if (defaultValue instanceof NativeSql) {
                    createTable.addSql("default", ((NativeSql) defaultValue).getSql());
                }
                if (column.isNotNull() || column.isPrimaryKey()) {
                    createTable.addSql("not null");
                }
                if (column.isPrimaryKey()) {
                    createTable.addSql("primary key");
                }
            }
            if (column.getComment() != null) {
                sql.addBatch(of(String.format("comment on column %s is '%s'",
                    column.getFullTableName(), column.getComment().replace("'", "''"))));
            }
        }
        createTable.addSql(")");

        if (table.getComment() != null) {
            sql.addBatch(of(String.format("comment on table %s is '%s'",
                table.getFullName(), table.getComment().replace("'", "''"))));
        }

        sql.setSql(createTable.toRequest().getSql());

        table.findFeature(CreateIndexSqlBuilder.ID)
            .ifPresent(builder -> {
                for (RDBIndexMetadata tableIndex : table.getIndexes()) {
                    sql.addBatch(builder.build(CreateIndexParameter.of(table, tableIndex)));
                }
            });

        return sql;
    }
}