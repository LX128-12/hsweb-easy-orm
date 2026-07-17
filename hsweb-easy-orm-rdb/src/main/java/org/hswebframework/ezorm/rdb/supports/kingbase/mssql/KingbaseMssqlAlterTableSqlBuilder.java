package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import lombok.Getter;
import lombok.Setter;
import org.hswebframework.ezorm.rdb.executor.DefaultBatchSqlRequest;
import org.hswebframework.ezorm.rdb.metadata.RDBColumnMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBTableMetadata;
import org.hswebframework.ezorm.rdb.supports.mssql.SqlServerAlterTableSqlBuilder;

import static org.hswebframework.ezorm.rdb.executor.SqlRequests.of;

@Getter
@Setter
@SuppressWarnings("all")
public class KingbaseMssqlAlterTableSqlBuilder extends SqlServerAlterTableSqlBuilder {

    @Override
    protected void appendDropColumnSql(DefaultBatchSqlRequest batch, RDBColumnMetadata drop) {
        RDBTableMetadata table = (RDBTableMetadata) drop.getOwner();
        if (drop.getComment() != null) {
            batch.addBatch(of(String.format("comment on column %s is ''",
                drop.getFullTableName())));
        }
        super.appendDropColumnSql(batch, drop);
    }

    @Override
    protected void appendAddColumnCommentSql(DefaultBatchSqlRequest batch, RDBColumnMetadata column) {
        if (column.getComment() == null || column.getComment().isEmpty()) {
            return;
        }
        batch.addBatch(of(String.format("comment on column %s is '%s'",
            column.getFullTableName(), column.getComment().replace("'", "''"))));
    }
}
