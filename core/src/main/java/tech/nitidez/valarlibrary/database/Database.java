package tech.nitidez.valarlibrary.database;

import tech.nitidez.valarlibrary.database.data.DataTable;
import tech.nitidez.valarlibrary.database.types.MySQL;

public abstract class Database {
    private static Database database;
    public static void setupDatabase(String type, boolean mariadb, String dbHost, String dbPort, String DbName, String mysqlUser, String mysqlPass) {
        if (type.equals("MYSQL")) {
            database = new MySQL(dbHost, dbPort, DbName, mysqlUser, mysqlPass, mariadb);
        }

        DataTable.TABLES.forEach(t -> database.createTable(t));
    }

    public static Database getDatabase() {
        return database;
    }

    public abstract void createTable(DataTable table);
    public abstract void close();
    public abstract boolean exists(DataTable table, String primaryValue);
}
