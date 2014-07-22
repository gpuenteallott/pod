package com.pod.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pod.model.Worker;

/**
 * Worker Data Access Object
 * Class to interact with the table of workers in the database
 */
public class WorkerDAO {
	
	private String error;
	
	public WorkerDAO(){
		error = "";
	}
	
	/**
	 * This method returns error information in case there was one
	 * @return String with the error message
	 */
	public String getError(){
		return this.error;
	}
	
	/**
	 * Inserts a worker object in the database
	 * @param worker
	 * @return the id of the worker. The given object by reference is also modified to include it
	 */
	public int insert ( Worker worker ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id = -1;
		
		try {
			con = ConnectionManager.getConnection();
			
			String searchQuery = "INSERT INTO workers ( status , local_ip, public_ip, instance_id, is_manager, last_time_worked ) VALUES ( ?,?,?,?,?,? )";
			
			statement = con.prepareStatement(searchQuery, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, worker.getStatus() );
			statement.setString(2, worker.getLocalIp() );
			statement.setString(3, worker.getPublicIp() );
			statement.setString(4, worker.getInstanceId() );
			statement.setBoolean(5, worker.isManager() );
			statement.setDate(6, new java.sql.Date( new Date().getTime() ) );
			
			statement.executeUpdate();
			
			resultSet = statement.getGeneratedKeys();
			while (resultSet.next()) {
				id = resultSet.getInt(1);
				worker.setId(id);
				break;
			}
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return id;
	}
	
	/**
	 * Updates the worker record in the database with the information of the given worker object
	 * @param worker worker with a valid id
	 * @return true if updated, false otherwise
	 */
	public boolean update ( Worker worker ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE workers SET status=?, local_ip=?, public_ip=?, instance_id=?, is_manager=? WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, worker.getStatus() );
			statement.setString(2, worker.getLocalIp() );
			statement.setString(3, worker.getPublicIp() );
			statement.setString(4, worker.getInstanceId() );
			statement.setBoolean(5, worker.isManager() );
			statement.setInt(6, worker.getId() );

			int rows = statement.executeUpdate();
			
			updated = rows > 0 ? true : false;
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return updated;
	}
	
	/**
	 * Updates the worker's status
	 * @param workerId
	 * @param status
	 * @return true if success, false otherwise
	 */
	public boolean updateStatus ( int workerId , String status ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE workers SET status=? WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, status );
			statement.setInt(2, workerId );

			int rows = statement.executeUpdate();
			
			updated = rows > 0 ? true : false;
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return updated;
	}
	
	
	/**
	 * Looks in the database for a worker with the given id
	 * @param id
	 * @return the worker object or null if it didn't exist
	 */
	public Worker select ( int id ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Worker worker = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM workers WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, id );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				worker = new Worker();
				worker.setId( id );
				worker.setStatus( rs.getString("status") );
				worker.setLocalIp( rs.getString("local_ip") );
				worker.setPublicIp( rs.getString("public_ip") );
				worker.setInstanceId( rs.getString("instance_id") );
				worker.setManager( rs.getBoolean("is_manager") );
				worker.setLastTimeWorked( rs.getDate("last_time_worked"));
				
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
			if (rs != null)	{
			    try {
			     	rs.close();
			    } catch (Exception e) { System.err.println(e); }
			        rs = null;
			    }
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return worker;
	}
	
	/**
	 * Looks in the database for a worker with the given name
	 * @param name
	 * @return the worker object or null if it didn't exist
	 */
	public Worker select ( String name ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Worker worker = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM workers WHERE name = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, name );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				worker = new Worker();
				worker.setId( rs.getInt("id") );
				worker.setStatus( rs.getString("status") );
				worker.setLocalIp( rs.getString("local_ip") );
				worker.setPublicIp( rs.getString("public_ip") );
				worker.setInstanceId( rs.getString("instance_id") );
				worker.setManager( rs.getBoolean("is_manager") );
				worker.setLastTimeWorked( rs.getDate("last_time_worked"));
				break;
				
			}
			
		} catch (SQLException e) {
			error = e.toString();
			e.printStackTrace();
		}
		
		finally {
			if (rs != null)	{
			    try {
			     	rs.close();
			    } catch (Exception e) { System.err.println(e); }
			        rs = null;
			    }
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return worker;
	}
	
	/**
	 * Selects all the workers and puts them in an array
	 * Doesn't retrieve workers with status terminated
	 * @return
	 */
	public Worker[] list () {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Worker> workers = new ArrayList<Worker>();
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM workers WHERE status != 'terminated'";
			
			statement = con.prepareStatement(searchQuery);
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				Worker worker = new Worker();
				worker.setId( rs.getInt("id") );
				worker.setStatus( rs.getString("status") );
				worker.setLocalIp( rs.getString("local_ip") );
				worker.setPublicIp( rs.getString("public_ip") );
				worker.setInstanceId( rs.getString("instance_id") );
				worker.setManager( rs.getBoolean("is_manager") );
				worker.setLastTimeWorked( rs.getDate("last_time_worked"));
				workers.add(worker);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
			if (rs != null)	{
			    try {
			     	rs.close();
			    } catch (Exception e) { System.err.println(e); }
			        rs = null;
			    }
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return workers.toArray( new Worker [workers.size()] );
	}
	
	/**
	 * Deletes the given worker by its id from the database
	 * @param int workerId
	 * @return true if successful, false otherwise
	 */
	public boolean delete ( int id ){
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean deleted = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "DELETE FROM workers WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, id );
			
			int rows = statement.executeUpdate();
			
			deleted = rows > 0 ? true : false;
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		finally {
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}

		return deleted;
	}
	
	/**
	 * Deletes the given worker by its id from the database
	 * @param workers
	 * @return true if successful, false otherwise
	 */
	public boolean delete ( Worker worker ){
		return delete (worker.getId());
	}

	/**
	 * Retrieves the workers that have installed the given activity by its id
	 * @param activityId
	 * @return worker array
	 */
	public Worker[] selectByActivity ( int activityId ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Worker> workers = new ArrayList<Worker>();
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT workers.id, workers.status, workers.local_ip, workers.public_ip, workers.instance_id, workers.is_manager FROM workers, installations WHERE workers.id = installations.workerId AND installations.activityId = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, activityId );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				Worker worker = new Worker();
				worker.setId( rs.getInt("id") );
				worker.setStatus( rs.getString("status") );
				worker.setLocalIp( rs.getString("local_ip") );
				worker.setPublicIp( rs.getString("public_ip") );
				worker.setInstanceId( rs.getString("instance_id") );
				worker.setManager( rs.getBoolean("is_manager") );
				worker.setLastTimeWorked( rs.getDate("last_time_worked"));
				workers.add(worker);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
			if (rs != null)	{
			    try {
			     	rs.close();
			    } catch (Exception e) { System.err.println(e); }
			        rs = null;
			    }
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return workers.toArray( new Worker [workers.size()] );
	}
	
	
	/**
	 * Retrieves the workers that have installed the given activity by its name
	 * @param activityName
	 * @return worker array
	 */
	public Worker[] selectByActivity ( String activityName ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Worker> workers = new ArrayList<Worker>();
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT workers.id, workers.status, workers.local_ip, workers.public_ip, workers.instance_id, workers.is_manager FROM workers, installations, activities WHERE workers.id = installations.workerId AND installations.activityId = activities.id AND activities.name = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, activityName );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				Worker worker = new Worker();
				worker.setId( rs.getInt("id") );
				worker.setStatus( rs.getString("status") );
				worker.setLocalIp( rs.getString("local_ip") );
				worker.setPublicIp( rs.getString("public_ip") );
				worker.setInstanceId( rs.getString("instance_id") );
				worker.setManager( rs.getBoolean("is_manager") );
				worker.setLastTimeWorked( rs.getDate("last_time_worked"));
				workers.add(worker);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
			if (rs != null)	{
			    try {
			     	rs.close();
			    } catch (Exception e) { System.err.println(e); }
			        rs = null;
			    }
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return workers.toArray( new Worker [workers.size()] );
	}
	
	
	public Worker[] selectByStatus ( String [] statuses ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Worker> workers = new ArrayList<Worker>();
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM workers WHERE workers.status = ?";
			for (int i = 1; i < statuses.length; i++)
				searchQuery += " AND workers.status = ?";
			
			statement = con.prepareStatement(searchQuery);
			for (int i = 0; i < statuses.length; i++)
				statement.setString(i+1, statuses[i] );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				Worker worker = new Worker();
				worker.setId( rs.getInt("id") );
				worker.setStatus( rs.getString("status") );
				worker.setLocalIp( rs.getString("local_ip") );
				worker.setPublicIp( rs.getString("public_ip") );
				worker.setInstanceId( rs.getString("instance_id") );
				worker.setManager( rs.getBoolean("is_manager") );
				worker.setLastTimeWorked( rs.getDate("last_time_worked"));
				workers.add(worker);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
			if (rs != null)	{
			    try {
			     	rs.close();
			    } catch (Exception e) { System.err.println(e); }
			        rs = null;
			    }
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return workers.toArray( new Worker [workers.size()] );
	}
	
	/**
	 * Retrieves the first available worker for work found in the database that has the given activity installed (installation status = installed)
	 * @param activityId
	 * @param status Status of the worker
	 * @return Worker object
	 */
	public Worker getAvailableByActivityAndStatus ( int activityId , String status ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Worker worker = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT workers.id, workers.status, workers.local_ip, workers.public_ip, workers.instance_id, workers.is_manager FROM workers, installations "
					+ "WHERE workers.id = installations.workerId AND installations.activityId = ? "
					+ "AND installations.status = 'installed'"
					+ "AND workers.status = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, activityId );
			statement.setString(2, status );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				worker = new Worker();
				worker.setId( rs.getInt("id") );
				worker.setStatus( rs.getString("status") );
				worker.setLocalIp( rs.getString("local_ip") );
				worker.setPublicIp( rs.getString("public_ip") );
				worker.setInstanceId( rs.getString("instance_id") );
				worker.setManager( rs.getBoolean("is_manager") );
				worker.setLastTimeWorked( rs.getDate("last_time_worked"));
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
			if (rs != null)	{
			    try {
			     	rs.close();
			    } catch (Exception e) { System.err.println(e); }
			        rs = null;
			    }
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return worker;
	}
	
	/**
	 * Deletes all workers in the database, as well as the information about their installations
	 */
	public void deleteAll (){
		
		Connection con = null;
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "DELETE FROM installations WHERE id > 0";
			
			statement = con.prepareStatement(searchQuery);
			
			statement.executeUpdate();
			
			searchQuery = "DELETE FROM workers WHERE id > 0";
			
			statement2 = con.prepareStatement(searchQuery);
			
			statement2.executeUpdate();
			
		} catch (SQLException e) {
			error = e.toString();
			e.printStackTrace();
		}
		
		finally {
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		     
		     if (statement2 != null) {
		        try {
		        	statement2.close();
		        } catch (Exception e) { System.err.println(e); }
		        statement2 = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
	}
	
	
	/**
	 * Updates the last time worked field for the given worker by its id
	 * @param worker worker with a valid id
	 * @return true if updated, false otherwise
	 */
	public boolean update ( int workerId ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE workers SET last_time_worked=? WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setDate(1, new java.sql.Date( new Date().getTime() ) );
			statement.setInt(2, workerId );

			int rows = statement.executeUpdate();
			
			updated = rows > 0 ? true : false;
			
		} catch (SQLException e) {
			e.printStackTrace();
			error = e.toString();
		}
		
		finally {
		     
		     if (statement != null) {
		        try {
		        	statement.close();
		        } catch (Exception e) { System.err.println(e); }
		        	statement = null;
		        }
		
		     if (con != null) {
		        try {
		        	con.close();
		        } catch (Exception e) { System.err.println(e); }
		
		        con = null;
		     }
		}
		return updated;
	}
	
}
