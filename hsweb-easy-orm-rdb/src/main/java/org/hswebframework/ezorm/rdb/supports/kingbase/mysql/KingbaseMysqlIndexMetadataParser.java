package org.hswebframework.ezorm.rdb.supports.kingbase.mysql;

import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.postgres.PostgresqlIndexMetadataParser;

public class KingbaseMysqlIndexMetadataParser extends PostgresqlIndexMetadataParser {

    public KingbaseMysqlIndexMetadataParser(RDBSchemaMetadata schema) {
        super(schema);
    }
}
