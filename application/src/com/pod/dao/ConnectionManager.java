package com.pod.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

/**
 * This class provides a getConnection method to allow efficient use of connections to the database, using a pool of connections
 */
public class ConnectionManager {
	
	public static DataSource dataSource;
	
	/**
	 * Retrieves a connection from a pool
	 * @return Connection
	 * @throws SQLException
	 */
	public static Connection getConnection() throws SQLException {
		
		if ( dataSource == null ) {
			BasicDataSource basicDataSource = new BasicDataSource();
			basicDataSource.setDriverClassName("com.mysql.jdbc.Driver");
			basicDataSource.setUsername("root");
			basicDataSource.setPassword("kaerus_123");
			basicDataSource.setUrl("jdbc:mysql://localhost:3306/pod");
			
			// Optional. SQL sentence that allows BasicDataSource
			// to check if the connection is right.
			basicDataSource.setValidationQuery("select 1");
			
			dataSource = basicDataSource;
		}

		return dataSource.getConnection();
		
	}

}
