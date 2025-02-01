package tech.nitidez.valarlibrary.database.data;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DataTable {
    public static class DataColumn {
        public String columnName;
        @SuppressWarnings("rawtypes")
        public Class columnType;
        public boolean primaryKey;
        public boolean autoIncrement;
        public boolean unique;
        public Optional<Integer> limit;
        public Optional<Object> defaultValue;
        @SuppressWarnings("rawtypes")
        public DataColumn(String columnName, Class columnType, boolean primaryKey, boolean autoIncrement, boolean unique, Optional<Integer> limit, Optional<Object> defaultValue) {
            this.columnName = columnName;
            this.primaryKey = primaryKey;
            this.autoIncrement = autoIncrement;
            this.limit = limit;
            this.defaultValue = defaultValue;
            this.columnType = columnType;
            this.unique = unique;
        }
    }

    public static Set<DataTable> TABLES = new HashSet<>();

    private String tableName;
    private List<DataColumn> columns;

    public DataTable(String tableName, DataColumn... columns) {
        this.tableName = tableName;
        this.columns = Arrays.asList(columns);
    }

    public String getTableName() {
        return tableName;
    }

    public List<DataColumn> getColumns() {
        return columns;
    }

    public Optional<DataColumn> getPrimary() {
        return columns.stream().filter(c -> c.primaryKey).findFirst();
    }

    public Optional<DataColumn> getColumn(String columnName) {
        return columns.stream().filter(c -> c.columnName.equals(columnName)).findFirst();
    }

    public static Optional<DataTable> getTable(String tableName) {
        return TABLES.stream().filter(t -> t.tableName.equals(tableName)).findFirst();
    }
}
