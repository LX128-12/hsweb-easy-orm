package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.supports.postgres.PostgresqlIndexMetadataParser;

public class KingbaseMssqlIndexMetadataParser extends PostgresqlIndexMetadataParser {

    public KingbaseMssqlIndexMetadataParser(RDBSchemaMetadata schema) {
        super(schema);
    }
}
