package org.hswebframework.ezorm.rdb.supports.kingbase.mssql;

import lombok.AllArgsConstructor;
import org.hswebframework.ezorm.rdb.exception.DuplicateKeyException;
import org.hswebframework.ezorm.rdb.metadata.RDBSchemaMetadata;
import org.hswebframework.ezorm.rdb.operator.ExceptionTranslation;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Locale;

@AllArgsConstructor(staticName = "of")
public class KingbaseMssqlJDBCExceptionTranslation implements ExceptionTranslation {

    @SuppressWarnings("unused")
    private final RDBSchemaMetadata schema;

    @Override
    public Throwable translate(Throwable e) {
        if (e instanceof SQLException) {
            SQLException exception = (SQLException) e;
            if ("23505".equals(exception.getSQLState())
                    || "23000".equals(exception.getSQLState())
                    || exception.getErrorCode() == 1062
                    || exception.getErrorCode() == 1022
                    || isDuplicateKeyMessage(exception.getMessage())) {
                return new DuplicateKeyException(false, Collections.emptyList(), e);
            }
        }
        return e;
    }

    private boolean isDuplicateKeyMessage(String message) {
        if (message == null) {
            return false;
        }
        String lowerCase = message.toLowerCase(Locale.ROOT);
        return lowerCase.contains("duplicate key")
                || lowerCase.contains("duplicated key")
                || lowerCase.contains("unique constraint");
    }
}
