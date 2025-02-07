package tech.nitidez.valarlibrary.data.types;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;

import tech.nitidez.valarlibrary.vLib;
import tech.nitidez.valarlibrary.data.Database;
import tech.nitidez.valarlibrary.data.data.DataRow;
import tech.nitidez.valarlibrary.data.data.DataTable;
import tech.nitidez.valarlibrary.data.data.DataTable.DataColumn;
import tech.nitidez.valarlibrary.plugin.logger.ValarLogger;

public class MySQL extends Database {
    private static final vLib instance = vLib.getInstance();
    private static final Logger LOGGER = ValarLogger.getLogger("Database");
    private String hostname;
    private String port;
    private String dbname;
    private String user;
    private String pass;
    private Connection conn;
    private boolean mariadb;
    private ExecutorService executor;

    public MySQL(String hostname, String port, String dbname, String user, String pass, boolean mariaDB) {
        this.hostname = hostname;
        this.port = port;
        this.dbname = dbname;
        this.user = user;
        this.pass = pass;
        this.mariadb = mariaDB;
        this.executor = Executors.newCachedThreadPool();
        openConnection();
    }

    private void openConnection() {
        try {
            boolean reconnected = true;
            if (this.conn == null) {
                reconnected = false;
            }
            Class.forName(this.mariadb ? "org.mariadb.jdbc.Driver" : "com.mysql.jdbc.Driver");
            this.conn = DriverManager.getConnection((this.mariadb ? "jdbc:mariadb://" : "jdbc:mysql://")+hostname+":"+port+"/"+dbname+"?verifyServerCertificate=false&useSSL=false&useUnicode=yes&characterEncoding=UTF-8", user, pass);
            if (reconnected) {
                instance.getLogger().info("Reconectado ao MySQL.");
                return;
            }
            instance.getLogger().info("Conectado ao MySQL.");
        } catch (ClassNotFoundException exception) {
            LOGGER.severe("O driver do "+(this.mariadb?"MariaDB":"MySQL")+" não foi encontrado.");
            System.exit(0);
        } catch (SQLException sqlexc) {
            LOGGER.log(Level.SEVERE, "Não foi possível se conectar ao MySQL: ", sqlexc);
        }
    }

    public void closeConnection() {
        if (isConnected()) {
            try {
                conn.close();
            } catch (SQLException ex) {
                LOGGER.log(Level.WARNING, "Não foi possível fechar a conexão com o MySQL: ", ex);
            }
        }
    }

    public Connection getConnection() throws SQLException {
        if (!isConnected()) {
            this.openConnection();
        }

        return this.conn;
    }

    public boolean isConnected() {
        try {
            return !(conn == null || conn.isClosed() || !conn.isValid(5));
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Não foi possível verificar a conexão com o MySQL: ", ex);
            return false;
        }
    }

    public void update(String sql, Object... vars) {
        try (PreparedStatement ps = prepareStatement(sql, vars)) {
            ps.executeUpdate();
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Não foi possível executar um SQL: ", ex);
        }
    }

    public void execute(String sql, Object... vars) {
        executor.execute(() -> {
            update(sql, vars);
        });
    }

    public int updateWithInsertId(String sql, Object... vars) {
        int id = -1;
        ResultSet rs = null;
        try (PreparedStatement ps = prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i = 0; i < vars.length; i++) {
                ps.setObject(i + 1, vars[i]);
            }
            ps.execute();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Não foi possível executar um SQL: ", ex);
        } finally {
            try {
                if (rs != null && !rs.isClosed()) rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return id;
    }

    public PreparedStatement prepareStatement(String query, Object... vars) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(query);
            for (int i = 0; i < vars.length; i++) {
                ps.setObject(i + 1, vars[i]);
            }
            return ps;
        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Não foi possível preparar um SQL: ", ex);
        }
        return null;
    }

    public CachedRowSet query(String query, Object... vars) {
        CachedRowSet rowSet = null;
        try {
            Future<CachedRowSet> future = executor.submit(() -> {
                CachedRowSet crs = null;
                try (PreparedStatement ps = prepareStatement(query, vars); ResultSet rs = ps.executeQuery()) {
                    CachedRowSet rs2 = RowSetProvider.newFactory().createCachedRowSet();
                    rs2.populate(rs);
                    if (rs2.next()) {
                        crs = rs2;
                    }
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Não foi possível executar uma requisição: ", ex);
                }

                return crs;
            });

            if (future.get() != null) {
                rowSet = future.get();
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Não foi possível executar uma futura tarefa: ", ex);
        }

        return rowSet;
    }

    public String classToType(DataColumn column) {
        @SuppressWarnings({ "rawtypes", "unchecked" })
        Map<Class<?>, String> typeMap = new HashMap();
        typeMap.put(Float.class, "FLOAT");
        typeMap.put(Integer.class, "INT");
        return typeMap.getOrDefault(column.columnType, column.limit.isPresent() ? "VARCHAR" : "TEXT");
    }

    @Override
    public void close() {
        this.executor.shutdownNow().forEach(Runnable::run);
        this.closeConnection();
    }

    @Override
    public void createTable(DataTable table) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS `");
        sql.append(table.getTableName()).append("` (");
        for (DataColumn column : table.getColumns()) {
            sql.append("`"+column.columnName+"` "+ classToType(column));
            if (column.limit.isPresent()) sql.append("(").append(column.limit.get()).append(")");
            if (column.autoIncrement) sql.append(" AUTO_INCREMENT");
            sql.append(", ");
        }
        sql.delete(sql.length() - 2, sql.length());
        if (table.getPrimary().isPresent()) sql.append(", PRIMARY KEY(`").append(table.getPrimary().get().columnName).append("`)");
        sql.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;");
        this.update(sql.toString());
    }

    @Override
    public void save(DataTable table) {
        save0(table, true);
    }

    @Override
    public void saveSync(DataTable table) {
        save0(table, false);
    }

    private void save0(DataTable table, boolean async) {
        Set<DataRow> rows = Database.getCachedRows();
        rows = rows.stream().filter(row -> ((!row.isUpdated()) && row.getTable().equals(table))).collect(Collectors.toSet());
        rows.forEach(row -> {
            List<Object> values = new ArrayList<>();
            table.getColumns().forEach(c -> {
                values.add(row.get(c));
            });
            StringBuilder query = new StringBuilder("INSERT INTO " + table.getTableName() + " (");
            query.append(String.join(", ", table.getColumns().stream().map(c -> c.columnName).collect(Collectors.toList())));
            query.append(") VALUES (");
            query.append(String.join(", ", Collections.nCopies(table.getColumns().size(), "?")));
            query.append(") ON DUPLICATE KEY UPDATE ");
            table.getColumns().stream().filter(c -> row.getOutdated().contains(c)).forEach(column -> {
                query.append(column.columnName + " = VALUES(" + column.columnName + "), ");
            });
            query.delete(query.length() - 2, query.length());

            row.setUpdated(true);
            if (async) {
                this.execute(query.toString(), values.toArray());
            } else {
                this.update(query.toString(), values.toArray());
            }
        });
    }

    @Override
    public Optional<DataRow> load(DataTable table, Object primaryValue) {
        StringBuilder query = new StringBuilder("SELECT ");
        query.append(String.join(", ", table.getColumns().stream().map(c -> c.columnName).collect(Collectors.toList())));
        query.append(" FROM "+ table.getTableName() + " WHERE " + table.getPrimary().get().columnName + " = ?");
        try (CachedRowSet rs = this.query(query.toString(), primaryValue)) {
            if (rs != null) {
                Object[] values = new Object[table.getColumns().size()];
                for (int i = 0; i < table.getColumns().size(); i++) {
                    values[i] = rs.getObject(i+1);
                }
                DataRow returnRow = new DataRow(table, values);
                returnRow.setUpdated(true);
                return Optional.of(returnRow);
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean exists(DataTable table, String primaryValue) {
        try {
            String primaryName = table.getPrimary().get().columnName;
            return (query("SELECT `"+primaryName+"` FROM `"+table.getTableName()+"` WHERE "+primaryName+" = ?", primaryValue).getString("uuid")) != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
