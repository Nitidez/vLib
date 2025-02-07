package tech.nitidez.valarlibrary.data;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import tech.nitidez.valarlibrary.data.data.DataRow;
import tech.nitidez.valarlibrary.data.data.DataTable;
import tech.nitidez.valarlibrary.data.types.MySQL;

public abstract class Database {
    private static Database database;
    private static Set<DataRow> cachedRows;
    public static void setupDatabase(String type, boolean mariadb, String dbHost, String dbPort, String DbName, String mysqlUser, String mysqlPass) {
        cachedRows = new HashSet<>();
        if (type.equals("MYSQL")) {
            database = new MySQL(dbHost, dbPort, DbName, mysqlUser, mysqlPass, mariadb);
        }

        DataTable.TABLES.forEach(t -> database.createTable(t));
    }

    public static Database getDatabase() {
        return database;
    }

    public static Set<DataRow> getCachedRows() {
        return cachedRows;
    }

    public static void cacheRow(DataRow row) {
        if (!cachedRows.contains(row)) cachedRows.add(row);
    }

    public static void uncacheRow(DataRow row) {
        if (cachedRows.contains(row)) cachedRows.remove(row);
    }

    public abstract void createTable(DataTable table);
    public abstract void save(DataTable table);
    public abstract void saveSync(DataTable table);
    public abstract Optional<DataRow> load(DataTable table, Object primaryValue);
    public abstract void close();
    public abstract boolean exists(DataTable table, String primaryValue);
}
