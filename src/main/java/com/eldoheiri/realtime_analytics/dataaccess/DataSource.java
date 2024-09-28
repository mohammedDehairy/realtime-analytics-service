package com.eldoheiri.realtime_analytics.dataaccess;

import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class DataSource {
    private static final HikariDataSource DATA_SOURCE = createDataSource();

    private static HikariDataSource createDataSource() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/analytics");
        config.setUsername("analytics");
        config.setPassword("analytics");
        config.addDataSourceProperty("ssl", "false");
        config.setAutoCommit(false);
        config.setMaximumPoolSize(40);
        config.setConnectionTimeout(30000);
        config.setMaxLifetime(300000);
        config.setKeepaliveTime(150000);
        HikariDataSource ds = new HikariDataSource(config);

        return ds;
    }

    public static java.sql.Connection getConnection() throws SQLException {
        return DATA_SOURCE.getConnection();
    }
}
