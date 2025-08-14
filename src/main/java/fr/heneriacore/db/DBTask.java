package fr.heneriacore.db;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface DBTask<T> {
    T execute(Connection connection) throws SQLException;
}
