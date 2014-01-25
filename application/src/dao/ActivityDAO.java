package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import model.Activity;

/**
 * Class to interact with the table of activities in the database
 */
public class ActivityDAO {
	
	private String error;
	
	public ActivityDAO(){
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
	 * Inserts a activity object in the database
	 * @param activity
	 * @return the id of the activity. The given object by reference is also modified to include it
	 */
	public int insert ( Activity activity ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id = -1;
		
		try {
			con = ConnectionManager.getConnection();
			
			String searchQuery = "INSERT INTO activities ( name , installationScriptLocation, status ) VALUES ( ? , ? , ? )";
			
			statement = con.prepareStatement(searchQuery, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, activity.getName() );
			statement.setString(2, activity.getInstallationScriptLocation() );
			statement.setString(3, activity.getStatus() );
			
			statement.executeUpdate();
			
			resultSet = statement.getGeneratedKeys();
			while (resultSet.next()) {
				id = resultSet.getInt(1);
				activity.setId(id);
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
	 * Updates the activity record in the database with the information of the given activity object
	 * @param activity activity with a valid id
	 * @return true if updated, false otherwise
	 */
	public boolean update ( Activity activity ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE activities SET name=?, installationScriptLocation=?, status=? WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, activity.getName() );
			statement.setString(2, activity.getInstallationScriptLocation() );
			statement.setString(3, activity.getStatus() );
			statement.setInt(4, activity.getId() );

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
	 * Updates only the status field of the given activity by its id
	 * @param id of the activity to update
	 * @param status
	 * @return true if updated, false otherwise
	 */
	public boolean updateStatus ( int activityId , String status ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE activities SET status=? WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, status );
			statement.setInt(2, activityId );

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
	 * Looks in the database for a activity with the given id
	 * @param id
	 * @return the activity object or null if it didn't exist
	 */
	public Activity select ( int id ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Activity activity = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM activities WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, id );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				activity = new Activity();
				activity.setId( id );
				activity.setName( rs.getString("name") );
				activity.setInstallationScriptLocation( rs.getString("installationScriptLocation") );
				activity.setStatus( rs.getString("status") );
				break;
				
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
		return activity;
	}
	
	/**
	 * Looks in the database for a activity with the given name
	 * @param name
	 * @return the activity object or null if it didn't exist
	 */
	public Activity select ( String name ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Activity activity = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM activities WHERE name = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, name );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				activity = new Activity();
				activity.setId( rs.getInt("id") );
				activity.setName( name );
				activity.setInstallationScriptLocation( rs.getString("installationScriptLocation") );
				activity.setStatus( rs.getString("status") );
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
		return activity;
	}
	
	/**
	 * Selects all the activities and puts them in an array
	 * @return
	 */
	public Activity[] list () {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Activity> activities = new ArrayList<Activity>();
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM activities";
			
			statement = con.prepareStatement(searchQuery);
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				Activity activity = new Activity();
				activity.setId( rs.getInt("id") );
				activity.setName( rs.getString("name") );
				activity.setInstallationScriptLocation( rs.getString("installationScriptLocation") );
				activity.setStatus( rs.getString("status") );
				
				activities.add(activity);
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
		return activities.toArray( new Activity [activities.size()] );
	}
	
	/**
	 * Deletes the given activity by its id from the database
	 * @param int activityId
	 * @return true if successful, false otherwise
	 */
	public boolean delete ( int id ){
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean deleted = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "DELETE FROM activities WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, id );
			
			int rows = statement.executeUpdate();
			
			deleted = rows > 0 ? true : false;
			
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

		return deleted;
	}
	
	/**
	 * Deletes the given activity by its id from the database
	 * @param activity
	 * @return true if successful, false otherwise
	 */
	public boolean delete ( Activity activity ){
		return delete (activity.getId());
	}
	
	/**
	 * Deletes the given activity by its name from the database
	 * @param String activityName
	 * @return true if successful, false otherwise
	 */
	public boolean delete ( String activityName ){
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean deleted = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "DELETE FROM activities WHERE name = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, activityName );
			
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
	 * Retrieves the activities that are installed in the given worker by its id
	 * @param workerId
	 * @return activity array
	 */
	public Activity[] selectByWorker ( int workerId ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Activity> activities = new ArrayList<Activity>();
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT activities.id, activities.name, activities.installationScriptLocation "
					+ "FROM activities, installations WHERE activities.id = installations.activityId AND installations.workerId = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, workerId );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				Activity activity = new Activity();
				activity.setId( rs.getInt("id") );
				activity.setName( rs.getString("name") );
				activity.setInstallationScriptLocation( rs.getString("installationScriptLocation") );
				activity.setStatus( rs.getString("status") );
				
				activities.add(activity);
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
		return activities.toArray( new Activity [activities.size()] );
	}
	
	
	/**
	 * Deletes all activities in the database, as well as the information about their installations
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
			
			searchQuery = "DELETE FROM activities WHERE id > 0";
			
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

}
