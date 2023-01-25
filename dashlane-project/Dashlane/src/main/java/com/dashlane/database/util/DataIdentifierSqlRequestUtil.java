package com.dashlane.database.util;

import com.dashlane.database.sql.DataIdentifierSql;
import com.dashlane.vault.model.SyncState;



public class DataIdentifierSqlRequestUtil {

    private DataIdentifierSqlRequestUtil() {
        
    }

    public static StringBuilder appendGetDefaultShowList(StringBuilder stringBuilder, String tablename) {
        stringBuilder
                .append(" (")
                .append(tablename).append(".").append(DataIdentifierSql.FIELD_ITEM_STATE)
                .append("='")
                .append(SyncState.IN_SYNC_MODIFIED.getCode())
                .append("' or ")
                .append(tablename).append(".").append(DataIdentifierSql.FIELD_ITEM_STATE)
                .append(" ='")
                .append(SyncState.MODIFIED.getCode())
                .append("' or ")
                .append(tablename).append(".").append(DataIdentifierSql.FIELD_ITEM_STATE)
                .append(" ='")
                .append(SyncState.SYNCED.getCode())
                .append("') ");
        return stringBuilder;
    }
}
