package tech.nitidez.valarlibrary.database.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import tech.nitidez.valarlibrary.database.data.DataTable.DataColumn;

public class DataRow {
    private DataTable table;
    private Map<DataColumn, Object> columnValues;
    private boolean updated;

    @SuppressWarnings("rawtypes")
    public DataRow(DataTable table, Object... values) {
        this.table = table;
        this.updated = true;
        this.columnValues = new HashMap<>();
        for (int i = 0; i < table.getColumns().size(); i++) {
            Object val;
            DataColumn column = table.getColumns().get(i);
            if (i < values.length) {
                val = values[i];
            } else {
                val = column.defaultValue.orElse(null);
                if (val instanceof Supplier) {
                    val = ((Supplier) val).get();
                }
            }
            this.columnValues.put(column, val);
        }
    }

    public Object get(DataColumn column) {
        return this.columnValues.get(column);
    }

    public Optional<Object> get(String columnName) {
        Optional<DataColumn> column = this.columnValues.keySet().stream().filter(c -> c.columnName.equals(columnName)).findFirst();
        if (column.isPresent()) {
            return Optional.of(get(column.get()));
        } else {
            return Optional.empty();
        }
    }

    public boolean set(DataColumn column, Object value) {
        try {
            if (!this.columnValues.keySet().contains(column)) return false;
            this.columnValues.put(column, value);
            this.updated = false;
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean set(String columnName, Object value) {
        Optional<DataColumn> column = this.columnValues.keySet().stream().filter(c -> c.columnName.equals(columnName)).findFirst();
        if (column.isPresent()) {
            return set(column.get(), value);
        } else {
            return false;
        }
    }

    public boolean isUpdated() {
        return this.updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }

    public DataTable getTable() {
        return this.table;
    }
}
