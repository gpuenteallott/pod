package com.pod.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pod.model.Policy;

/**
 * Policy Data Access Object
 * Class to interact with the table of policies in the database
 */
public class PolicyDAO {
	
	// Variables to have the active policy cached. Timeout in milliseconds
	private static final int CACHE_TIMEOUT = 3000;
	private static Policy cachedPolicy;
	private static long lastAccessTime;
	
	private String error;
	
	public PolicyDAO(){
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
	 * Inserts a policy object in the database
	 * Doesn't update the active flag
	 * @param policy
	 * @return the id of the policy. The given object by reference is also modified to include it
	 */
	public int insert ( Policy policy ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id = -1;
		
		try {
			con = ConnectionManager.getConnection();
			
			String searchQuery = "INSERT INTO policies ( name , rules ) VALUES ( ? , ? )";
			
			statement = con.prepareStatement(searchQuery, Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, policy.getName() );
			//statement.setInt(2, policy.isActive() ? 1 : 0 );
			statement.setString(2, policy.getRules().toString() );
			
			statement.executeUpdate();
			
			resultSet = statement.getGeneratedKeys();
			while (resultSet.next()) {
				id = resultSet.getInt(1);
				policy.setId(id);
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
	 * Updates the policy record in the database with the information of the given policy object
	 * Doesn't update the active flag
	 * @param policy policy with a valid id
	 * @return true if updated, false otherwise
	 */
	public boolean update ( Policy policy ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE policies SET name=?, rules=? WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, policy.getName() );
			statement.setString(2, policy.getRules().toString() );
			statement.setInt(3, policy.getId() );

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
	 * Sets the active flag for the given policy by its id
	 * @param policy
	 * @return true if a modification was made to the database
	 */
	public boolean setActive ( Policy policy ) {
		
		// Null cache
		cachedPolicy = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE policies SET active=1 WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, policy.getId() );

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
	 * Sets the active flag for the given policy by its name
	 * @param policy
	 * @return true if a modification was made to the database
	 */
	public boolean setActive ( String policyName ) {
		
		// Null cache
		cachedPolicy = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE policies SET active=1 WHERE name = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, policyName );

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
	 * Sets the active flag to false for the given policy by its name
	 * @param policy
	 * @return true if a modification was made to the database
	 */
	public boolean setInactive ( String policyName ) {
		
		// Null cache
		cachedPolicy = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE policies SET active=0 WHERE name = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, policyName );

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
	 * Sets the active flag to false for the given policy by its id
	 * @param policy
	 * @return true if a modification was made to the database
	 */
	public boolean setInactive ( Policy policy ) {
		
		// Null cache
		cachedPolicy = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE policies SET active=0 WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, policy.getId() );

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
	 * Looks in the database for a policy with the given id
	 * @param id
	 * @return the policy object or null if it didn't exist
	 */
	public Policy select ( int id ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Policy policy = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM policies WHERE id = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setInt(1, id );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				policy = new Policy();
				policy.setId( id );
				policy.setName( rs.getString("name") );
				policy.setActive( rs.getInt("active") );
				policy.setRules( rs.getString("rules") );
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
		return policy;
	}
	
	/**
	 * Looks in the database for a policy with the given name
	 * @param name
	 * @return the policy object or null if it didn't exist
	 */
	public Policy select ( String name ) {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Policy policy = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM policies WHERE name = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, name );
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				policy = new Policy();
				policy.setId( rs.getInt("id") );
				policy.setName( name );
				policy.setActive( rs.getInt("active") );
				policy.setRules( rs.getString("rules") );
				
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
		return policy;
	}
	
	/**
	 * Retrieves the active policy from the database (first occurrence) 
	 * @return
	 */
	public Policy getActive () {
		
		// Check with cache first
		if ( cachedPolicy != null && new Date().getTime() - lastAccessTime < CACHE_TIMEOUT )
			return cachedPolicy;
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		Policy policy = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM policies WHERE active = 1 LIMIT 1";
			
			statement = con.prepareStatement(searchQuery);
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				policy = new Policy();
				policy.setId( rs.getInt("id") );
				policy.setName( rs.getString("name") );
				policy.setActive( rs.getInt("active") );
				policy.setRules( rs.getString("rules") );
				
				cachedPolicy = policy;
				lastAccessTime = new Date().getTime();
				
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
		return policy;
	}
	
	/**
	 * Selects all the policies and puts them in an array
	 * @return
	 */
	public Policy[] list () {
		
		Connection con = null;
		PreparedStatement statement = null;
		ResultSet rs = null;
		List<Policy> policies = new ArrayList<Policy>();
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "SELECT * FROM policies";
			
			statement = con.prepareStatement(searchQuery);
			
			rs = statement.executeQuery();
			
			while (rs.next()) {
				
				Policy policy = new Policy();
				policy.setId( rs.getInt("id") );
				policy.setName( rs.getString("name") );
				policy.setActive( rs.getInt("active") );
				policy.setRules( rs.getString("rules") );
				
				policies.add(policy);
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
		return policies.toArray( new Policy [policies.size()] );
	}
	
	/**
	 * Deletes the given policy by its id from the database
	 * @param int policyId
	 * @return true if successful, false otherwise
	 */
	public boolean delete ( int id ){
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean deleted = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "DELETE FROM policies WHERE id = ?";
			
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
	 * Deletes the given policy by its id from the database
	 * @param policy
	 * @return true if successful, false otherwise
	 */
	public boolean delete ( Policy policy ){
		return delete (policy.getId());
	}
	
	/**
	 * Deletes the given policy by its name from the database
	 * @param String policyName
	 * @return true if successful, false otherwise
	 */
	public boolean delete ( String policyName ){
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean deleted = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "DELETE FROM policies WHERE name = ?";
			
			statement = con.prepareStatement(searchQuery);
			statement.setString(1, policyName );
			
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
	 * Deletes all policies in the database
	 */
	public void deleteAll (){
		
		Connection con = null;
		PreparedStatement statement = null;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "DELETE FROM policies WHERE id > 0";
			
			statement = con.prepareStatement(searchQuery);
			
			statement.executeUpdate();
			
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
	}

	/**
	 * Removes the active flag from the active one. No active policy after this method
	 * @param policy
	 * @return
	 */
	public boolean reset () {
		
		// Null cache
		cachedPolicy = null;
		
		Connection con = null;
		PreparedStatement statement = null;
		boolean updated = false;
		
		try {
			
			con = ConnectionManager.getConnection();
			
			String searchQuery = "UPDATE policies SET active=0 WHERE active = 1";
			
			statement = con.prepareStatement(searchQuery);

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
