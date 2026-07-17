package org.hswebframework.ezorm.rdb.supports.kingbase.oracle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.core.CastUtil;
import org.hswebframework.ezorm.core.meta.ObjectMetadata;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.reactive.ReactiveSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ColumnWrapperContext;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;
import org.hswebframework.ezorm.rdb.metadata.RDBIndexMetadata;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.metadata.parser.IndexMetadataParser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.hswebframework.ezorm.rdb.executor.SqlRequests.*;
import static org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrappers.*;

@Slf4j
@AllArgsConstructor(staticName = "of")
public class KingbaseOracleIndexMetadataParser implements IndexMetadataParser {

    private static final String sql = String.join(" ",
        "select i.relname as index_name,",
        "t.relname as table_name,",
        "ix.indisunique as uniqueness,",
        "a.attname as column_name,",
        "array_position(ix.indkey,a.attnum) as column_position,",
        "case ix.indoption[array_position(ix.indkey,a.attnum)-1] & 1 when 1 then 'DESC' else 'ASC' end as descend",
        "from pg_class t",
        "join pg_index ix on t.oid = ix.indrelid",
        "join pg_class i on i.oid = ix.indexrelid",
        "join pg_attribute a on a.attrelid = t.oid",
        "join pg_namespace n on n.oid = t.relnamespace",
        "where a.attnum = any(ix.indkey)",
        "and n.nspname = ?",
        "and t.relname like ?");

    private static final String primaryKeyIndexSql = String.join(" ",
        "select c.relname as index_name",
        "from pg_index ix",
        "join pg_class c on c.oid = ix.indexrelid",
        "where ix.indisprimary",
        "and ix.indrelid in (select oid from pg_class where relnamespace = (select oid from pg_namespace where nspname = ?) and relname like ?)");

    @Getter
    private final RDBSchemaMetadata schema;

    @Override
    public List<RDBIndexMetadata> parseTableIndex(String tableName) {
        String schemaName = schema.getName();

        return schema
            .<SyncSqlExecutor>findFeature(SyncSqlExecutor.ID)
            .map(sqlExecutor -> sqlExecutor
                .select(prepare(sql, schemaName, tableName),
                    new KingbaseOracleIndexWrapper(
                        sqlExecutor
                            .select(prepare(primaryKeyIndexSql, schemaName, tableName),
                                stream(column("index_name", String::valueOf)))
                            .collect(Collectors.toSet())
                    )))
            .orElseGet(() -> {
                log.warn("unsupported SyncSqlExecutor");
                return Collections.emptyList();
            });
    }

    @Override
    public Optional<RDBIndexMetadata> parseByName(String name) {
        return Optional.empty();
    }

    @Override
    public List<RDBIndexMetadata> parseAll() {
        return parseTableIndex("%%");
    }

    @Override
    public Flux<RDBIndexMetadata> parseAllReactive() {
        return parseTableIndexReactive("%%");
    }

    @Override
    public Mono<RDBIndexMetadata> parseByNameReactive(String name) {
        return Mono.empty();
    }

    @Override
    public Flux<RDBIndexMetadata> parseTableIndexReactive(String tableName) {
        ReactiveSqlExecutor sqlExecutor = schema.findFeatureNow(ReactiveSqlExecutor.ID);
        String schemaName = schema.getName();

        return sqlExecutor
            .select(prepare(primaryKeyIndexSql, schemaName, tableName),
                column("index_name", String::valueOf))
            .collect(Collectors.toSet())
            .flatMap(pkSet -> {
                KingbaseOracleIndexWrapper wrapper = new KingbaseOracleIndexWrapper(pkSet);
                return sqlExecutor
                    .select(prepare(sql, schemaName, tableName), wrapper)
                    .then(Mono.fromSupplier(wrapper::getResult));
            })
            .flatMapIterable(Function.identity());
    }

    class KingbaseOracleIndexWrapper implements ResultWrapper<Map<String, String>, List<RDBIndexMetadata>> {
        private final Map<Tuple2<String, String>, RDBIndexMetadata> mappingByName = new HashMap<>();
        private final Set<String> pkNameSet;

        public KingbaseOracleIndexWrapper(Set<String> pkNameSet) {
            this.pkNameSet = pkNameSet;
        }

        @Override
        public Map<String, String> newRowInstance() {
            return new HashMap<>();
        }

        @Override
        public void wrapColumn(ColumnWrapperContext<Map<String, String>> context) {
            if (context.getResult() != null) {
                context.getRowInstance().put(context.getColumnLabel().toLowerCase(), String.valueOf(context.getResult()));
            }
        }

        @Override
        public boolean completedWrapRow(Map<String, String> result) {
            String name = result.get("index_name");
            String tableName = result.get("table_name");

            RDBIndexMetadata metadata = mappingByName.computeIfAbsent(
                Tuples.of(tableName, name), tp2 -> new RDBIndexMetadata(tp2.getT2()));
            metadata.setTableName(tableName);
            metadata.setUnique("1".equals(result.get("uniqueness")) || "t".equals(result.get("uniqueness")));
            metadata.setPrimaryKey(pkNameSet.contains(metadata.getName()));

            RDBIndexMetadata.IndexColumn column = new RDBIndexMetadata.IndexColumn();
            column.setSort("DESC".equalsIgnoreCase(result.get("descend")) ?
                RDBIndexMetadata.IndexSort.desc : RDBIndexMetadata.IndexSort.asc);
            column.setSortIndex(Integer.parseInt(result.get("column_position")));
            column.setColumn(schema.getDialect()
                .clearQuote(result.get("column_name"))
                .toLowerCase());

            metadata.getColumns().add(column);
            return true;
        }

        @Override
        public List<RDBIndexMetadata> getResult() {
            return new ArrayList<>(mappingByName.values());
        }
    }
}
