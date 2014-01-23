package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Installation;

/**
 * InstallationDAO provides methods to add new records of activity installations in workers or delete them
 */
public class InstallationDAO {
	
	private String error;
	
	public InstallationDAO(){
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
	 * Inserts a new installation record with the given activityId and workerId
	 * @param activityId
	 * @param workerId
	 * @param status of the installation
	 * @param errorDescription String containing the error message for the user
	 * @return index of the new record
	 */
	public int insert ( int activityId, int workerId, String status , String errorDescription ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id = -1;
		
		try {
			con = ConnectionManager.getConnection();
			
			String searchQuery = "INSERT INTO installations ( activityId , workerId , status , errorDescription ) VALUES ( ? , ? , ? , ? )";
			
			statement = con.prepareStatement(searchQuery, Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, activityId );
			statement.setInt(2, workerId );
			statement.setString(3, status );
			statement.setString(4, errorDescription);
			
			statement.executeUpdate();
			
			resultSet = statement.getGeneratedKeys();
			while (resultSet.next()) {
				id = resultSet.getInt(1);
				break;
			}
			
		} 
		catch (com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException icve) {
			error = icve.toString();
		}
		catch (SQLException e) {
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
		return id;
		
	}
	
	
	/**
	 * Inserts a new installation record with the given activityId and workerId. The errorDescription field will be set to null
	 * @param activityId
	 * @param workerId
	 * @param status of the installation
	 * @return index of the new record
	 */
	public int insert ( int activityId, int workerId, String status ) {
		return insert(activityId, workerId, status, null);
	}
	
	/**
	 * Updates the installation record with a new status
	 * @param activityId
	 * @param workerId
	 * @param status
	 * @param errorDescription String containing the error message for the user
	 * @return true if success, false otherwise
	 */
	public boolean update ( int activityId, int workerId, String status , String errorDescription ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE installations SET status=?, errorDescription=? WHERE activityId = ? AND workerId = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, status );
			statement.setString(2, errorDescription );
			statement.setInt(3, activityId );
			statement.setInt(4, workerId );

			int rows = statement.executeUpdate();
			
			updated = rows > 0 ? true : false;
			
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
	 * Updates the installation record with a new status. The errorDescription field will be set to null
	 * @param activityId
	 * @param workerId
	 * @param status
	 * @return true if success, false otherwise
	 */
	public boolean update ( int activityId, int workerId, String status ) {
		return update (activityId, workerId, status, null);
	}
	
	/**
	 * Deletes the installation record given by its activityId and workerId
	 * @param activityId
	 * @param workerId
	 * @return true if one or more rows were deleted, false otherwise
	 */
	public boolean delete ( int activityId, int workerId ){
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean deleted = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "DELETE FROM installations WHERE activityId = ? AND workerId = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, activityId );
			statement.setInt(2, workerId );
			
			int rows = statement.executeUpdate();
			
			deleted = rows > 0 ? true : false;
			
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
	 * Deletes all installation records that match the given activityId
	 * @param activityId
	 * @return true if success, false if some error happened
	 */
	public boolean deleteAll ( int activityId ){
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean deleted = true;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "DELETE FROM installations WHERE activityId = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, activityId );
			
			statement.executeUpdate();
			
		} catch (SQLException e) {
			error = e.toString();
			e.printStackTrace();
			deleted = false;
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
	 * Allows to get the worker ids that have anything to do with the given activity by its id
	 * @param activityId
	 * @return an array of Installation objects
	 */
	public Installation[] selectByActivity ( int activityId ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Installation> installations = new ArrayList<Installation>();
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM installations WHERE activityId = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, activityId );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				Installation installation = new Installation();
				installation.setActivityId(activityId);
				installation.setWorkerId( rs.getInt("workerId") );
				installation.setStatus( rs.getString("status") );
				installation.setErrorDescription( rs.getString("errorDescription") );
				
				installations.add(installation);
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
		return installations.toArray( new Installation [installations.size()] );
	}
}
