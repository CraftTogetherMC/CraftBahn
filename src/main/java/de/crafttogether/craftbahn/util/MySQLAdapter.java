package de.crafttogether.craftbahn.util;

import com.zaxxer.hikari.HikariDataSource;
import de.crafttogether.craftbahn.CraftBahn;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import java.sql.*;

public class MySQLAdapter {
    private static MySQLAdapter instance;

    private static MySQLConfig config;
    private HikariDataSource dataSource;

    public MySQLAdapter(MySQLConfig _config) {
        instance = this;
        config = _config;
        setupHikari();
    }

    public MySQLAdapter(String host, int port, String database, String username, String password, String tablePrefix) {
        instance = this;
        config = new MySQLConfig(host, port, database, username, password, tablePrefix);
        setupHikari();
    }

    private void setupHikari() {
        this.dataSource = new HikariDataSource();
        this.dataSource.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        this.dataSource.addDataSourceProperty("serverName", config.getHost());
        this.dataSource.addDataSourceProperty("port", config.getPort());

        if (config.getDatabase() != null)
            this.dataSource.addDataSourceProperty("databaseName", config.getDatabase());

        this.dataSource.addDataSourceProperty("user", config.getUsername());
        this.dataSource.addDataSourceProperty("password", config.getPassword());
        this.dataSource.setAutoCommit(true);
    }

    public static MySQLAdapter getAdapter() {
        return instance;
    }

    public MySQLConnection getConnection() {
        return new MySQLConnection();
    }

    public void disconnect() {
        // TODO: Should we call .close() on all instantiated MySQLConnection-Objects here?
        dataSource.close();
    }

    public class MySQLConnection {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        private void executeAsync(Runnable task) {
            Bukkit.getServer().getScheduler().runTaskAsynchronously(CraftBahn.getInstance(), task);
        }

        public ResultSet query(String statement, final Object... args) throws SQLException {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            try {
                connection = dataSource.getConnection();
                preparedStatement = connection.prepareStatement(finalStatement);
                resultSet = preparedStatement.executeQuery();
            }
            catch (SQLException e) {
                if (e.getMessage().contains("link failure"))
                    CraftBahn.getInstance().getLogger().warning("[MySQL]: Couldn't connect to MySQL-Server...");
                else
                    throw e;
            }

            return resultSet;
        }

        public int insert(String statement, final Object... args) throws SQLException {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            int lastInsertedId = 0;
            try {
                connection = dataSource.getConnection();
                preparedStatement = connection.prepareStatement(finalStatement, Statement.RETURN_GENERATED_KEYS);
                preparedStatement.executeUpdate();

                resultSet = preparedStatement.getGeneratedKeys();
                if (resultSet.next())
                    lastInsertedId = resultSet.getInt(1);
            }
            catch (SQLException e) {
                if (e.getMessage().contains("link failure"))
                    CraftBahn.getInstance().getLogger().warning("[MySQL]: Couldn't connect to MySQL-Server...");
                else
                    throw e;
            }

            return lastInsertedId;
        }

        public int update(String statement, final Object... args) throws SQLException {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            int rows = 0;
            try {
                connection = dataSource.getConnection();
                preparedStatement = connection.prepareStatement(finalStatement);
                rows = preparedStatement.executeUpdate();
            }
            catch (SQLException e) {
                if (e.getMessage().contains("link failure"))
                    CraftBahn.getInstance().getLogger().warning("[MySQL]: Couldn't connect to MySQL-Server...");
                else
                    throw e;
            }

            return rows;
        }

        public Boolean execute(String statement, final Object... args) throws SQLException {
            if (args.length > 0) statement = String.format(statement, args);
            String finalStatement = statement;

            boolean result = false;
            try {
                connection = dataSource.getConnection();
                preparedStatement = connection.prepareStatement(finalStatement);
                result = preparedStatement.execute();
            }
            catch (SQLException e) {
                if (e.getMessage().contains("link failure"))
                    CraftBahn.getInstance().getLogger().warning("[MySQL]: Couldn't connect to MySQL-Server...");
                else
                    throw e;
            }

            return result;
        }

        public MySQLConnection queryAsync(String statement, final @Nullable Callback<SQLException, ResultSet> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    ResultSet resultSet = query(finalStatement);
                    assert callback != null;
                    callback.call(null, resultSet);
                } catch (SQLException e) {
                    if (e.getMessage().contains("link failure"))
                        CraftBahn.getInstance().getLogger().warning("[MySQL]: Couldn't connect to MySQL-Server...");
                    else {
                        assert callback != null;
                        callback.call(e, null);
                    }
                }
            });

            return this;
        }

        public MySQLConnection insertAsync(String statement, final @Nullable Callback<SQLException, Integer> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    int lastInsertedId = insert(finalStatement);
                    assert callback != null;
                    callback.call(null, lastInsertedId);
                } catch (SQLException e) {
                    if (e.getMessage().contains("link failure"))
                        CraftBahn.getInstance().getLogger().warning("[MySQL]: Couldn't connect to MySQL-Server...");
                    else {
                        assert callback != null;
                        callback.call(e, 0);
                    }
                }
            });

            return this;
        }

        public MySQLConnection updateAsync(String statement, final @Nullable Callback<SQLException, Integer> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    int rows = update(finalStatement);
                    assert callback != null;
                    callback.call(null, rows);
                } catch (SQLException e) {
                    if (e.getMessage().contains("link failure"))
                        CraftBahn.getInstance().getLogger().warning("[MySQL]: Couldn't connect to MySQL-Server...");
                    else {
                        assert callback != null;
                        callback.call(e, 0);
                    }
                }
            });

            return this;
        }

        public MySQLConnection executeAsync(String statement, final @Nullable Callback<SQLException, Boolean> callback, final Object... args) {
            if (args.length > 0) statement = String.format(statement, args);
            final String finalStatement = statement;

            executeAsync(() -> {
                try {
                    boolean result = execute(finalStatement);
                    assert callback != null;
                    callback.call(null, result);
                } catch (SQLException e) {
                    if (e.getMessage().contains("link failure"))
                        CraftBahn.getInstance().getLogger().warning("[MySQL]: Couldn't connect to MySQL-Server...");
                    else {
                        assert callback != null;
                        callback.call(e, false);
                    }
                }
            });

            return this;
        }

        public MySQLConnection close() {
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.out.println(e.getMessage());
                }
            }

            return this;
        }

        public String getTablePrefix() {
            return config.getTablePrefix();
        }
    }

    public static class MySQLConfig {
        String host;
        Integer port;
        String username;
        String password;
        String database;
        String tablePrefix;

        public MySQLConfig() { }

        public MySQLConfig(String host, int port, String username, String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        public MySQLConfig(String host, int port, String username, String password, String database, String tablePrefix) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.database = database;
            this.tablePrefix = tablePrefix;
        }

        public boolean checkInputs() {
            if (tablePrefix == null) tablePrefix = "";
            return (this.host != null && port != null && port > 1 && username != null && password != null);
        }

        public void setHost(String host) { this.host = host; }

        public void setPort(int port) { this.port = port; }

        public void setUsername(String username) { this.username = username; }

        public void setPassword(String password) { this.password = password; }

        public void setDatabase(String database) { this.database = database; }

        public void setTablePrefix(String tablePrefix) { this.tablePrefix = tablePrefix; }

        public String getHost() { return host; }

        public int getPort() { return port; }

        public String getUsername() { return username; }

        public String getPassword() { return password; }

        public String getDatabase() { return database; }

        public String getTablePrefix() { return tablePrefix; }
    }
}