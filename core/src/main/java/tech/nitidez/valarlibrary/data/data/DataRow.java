package tech.nitidez.valarlibrary.data.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import tech.nitidez.valarlibrary.data.data.DataTable.DataColumn;

public class DataRow {
    private DataTable table;
    private Map<DataColumn, Object> columnValues;
    private Set<DataColumn> outdated;

    public DataRow(DataTable table, Object... values) {
        this.table = table;
        this.outdated = new HashSet<>();
        this.columnValues = new HashMap<>();
        for (int i = 0; i < table.getColumns().size(); i++) {
            Object val;
            DataColumn column = table.getColumns().get(i);
            if (i < values.length) {
                val = values[i];
                if (val.equals(null)) val = column.getDefaultValue();
            } else {
                val = column.getDefaultValue();
            }
            this.columnValues.put(column, val);
        }
        this.setUpdated(false);
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

    public Map<DataColumn, Object> getValues() {
        return this.columnValues;
    }

    public boolean set(DataColumn column, Object value) {
        try {
            if (!this.columnValues.keySet().contains(column)) return false;
            this.columnValues.put(column, value);
            this.outdated.add(column);
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
        return !(this.outdated.size() > 0);
    }

    public void setUpdated(boolean updated) {
        if (updated) {
            this.outdated.clear();
        } else {
            this.outdated.addAll(columnValues.keySet());
        }
    }

    public Set<DataColumn> getOutdated() {
        return this.outdated;
    }

    public DataTable getTable() {
        return this.table;
    }
}
