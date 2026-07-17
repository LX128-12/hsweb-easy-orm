package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.core.DefaultValue;
import org.hswebframework.ezorm.rdb.executor.DefaultBatchSqlRequest;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBIndexMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.metadata.parser.IndexMetadataParser;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.NativeSql;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.PrepareSqlFragments;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateIndexParameter;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateIndexSqlBuilder;
import org.hswebframework.ezorm.rdb.operator.builder.fragments.ddl.CreateTableSqlBuilder;

import java.util.ArrayList;
import java.util.List;

import static org.hswebframework.ezorm.rdb.executor.SqlRequests.of;

@SuppressWarnings("all")
@Getter
@Setter
public class KingbaseMssqlCreateTableSqlBuilder implements CreateTableSqlBuilder {

    private boolean createComment = true;

    @Override
    public SqlRequest build(RDBTableMetadata table) {
        DefaultBatchSqlRequest sql = new DefaultBatchSqlRequest();

        PrepareSqlFragments createTable = PrepareSqlFragments.of();

        createTable.addSql("create table", table.getFullName(), "(");

        List<SqlRequest> batch = new ArrayList<>();

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
                if (column.isNotNull() || column.isPrimaryKey()) {
                    createTable.addSql("not null");
                }
                if (column.isPrimaryKey()) {
                    createTable.addSql("primary key");
                }
                DefaultValue defaultValue = column.getDefaultValue();
                if (defaultValue instanceof NativeSql) {
                    createTable.addSql("default", ((NativeSql) defaultValue).getSql());
                }
            }
            if (createComment && column.getComment() != null) {
                batch.add(of(String.format("comment on column %s is '%s'",
                    column.getFullTableName(), column.getComment().replace("'", "''"))));
            }
        }
        createTable.addSql(")");
        if (createComment && table.getComment() != null) {
            batch.add(of(String.format("comment on table %s is '%s'",
                table.getFullName(), table.getComment().replace("'", "''"))));
        }
        if (table.findFeature(IndexMetadataParser.ID).isPresent()) {
            table.findFeature(CreateIndexSqlBuilder.ID)
                 .ifPresent(builder -> {
                     for (RDBIndexMetadata tableIndex : table.getIndexes()) {
                         batch.add(builder.build(CreateIndexParameter.of(table, tableIndex)));
                     }
                 });
        }
        sql.setSql(createTable.toRequest().getSql());
        batch.forEach(sql::addBatch);
        return sql;
    }
}
