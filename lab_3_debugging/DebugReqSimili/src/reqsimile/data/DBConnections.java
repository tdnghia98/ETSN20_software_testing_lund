package reqsimile.data;

import java.net.*;
import java.sql.*;
import java.util.*;

import reqsimile.*;

/**
 * DBConnections creates and saves all database connections. This is done so
 * that database connections may be reused.
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class DBConnections {

	private class DBConnection {
		Connection dbConn = null; // The connection

		URL driverURL; // The URL to an external driver (empty if internal)

		String driverClassName; // The driver class name

		String dbURL; // The database name

		String dbUser; // The database user name

		String dbPassword; // The database password

		int usage = 0;

		/**
		 * Constructor Creates and stores a new connection If a connection
		 * cannot be established the connection will be null
		 * 
		 * @param dbName
		 * @param dbUser
		 * @param dbPassword
		 */
		public DBConnection(String dURL, String dClassName, String dbURL,
				String dbUser, String dbPassword) {
			try {
				this.driverURL = new URL(dURL);
			} catch (Exception e) {
				this.driverURL = null;
			}
			this.driverClassName = dClassName;
			this.dbURL = dbURL;
			this.dbUser = dbUser;
			this.dbPassword = dbPassword;
			try {
				if (dURL.toString().length() == 0) {
					Driver d = (Driver) Class.forName(driverClassName)
							.newInstance(); // "sun.jdbc.odbc.JdbcOdbcDriver"
				} else {
					URLClassLoader ucl = new URLClassLoader(
							new URL[] { driverURL });
					Driver d = (Driver) Class.forName(driverClassName, true,
							ucl).newInstance();
					DriverManager.registerDriver(new DriverShim(d));
				}
				dbConn = DriverManager.getConnection(dbURL, dbUser, dbPassword); // "jdbc:odbc:"
																				 // +
			} catch (Exception e) {
				dbConn = null;
			}
		}

		/**
		 * Returns the connection
		 * 
		 * @return
		 */
		public Connection getConnection() {
			return dbConn;
		}

		/**
		 * Returns true if this is a valid connection, otherwise false.
		 * 
		 * @return
		 */
		public boolean isValid() {
			return (dbConn != null);
		}

		/**
		 * Returns true if this connection corresponds to the specified
		 * connection parameters, otherwise false
		 * 
		 * @param dbName
		 * @param dbUser
		 * @param dbPassword
		 * @return
		 */
		public boolean isConnection(String dURL, String dClassName,
				String dbName, String dbUser, String dbPassword) {
			if (this.driverURL == null) {
				if (dURL.length() == 0) {
					return ((this.driverClassName.equalsIgnoreCase(dClassName))
							&& (this.dbURL.equalsIgnoreCase(dbName))
							&& (this.dbUser.equals(dbUser)) && (this.dbPassword
							.equals(dbPassword)));
				} else {
					return false;
				}
			} else {
				return ((this.driverURL.toString().equals(dURL))
						&& (this.driverClassName.equalsIgnoreCase(dClassName))
						&& (this.dbURL.equalsIgnoreCase(dbName))
						&& (this.dbUser.equals(dbUser)) && (this.dbPassword
						.equals(dbPassword)));
			}
		}

		public boolean isConnection(Connection dbConn) {
			return (this.dbConn == dbConn);
		}

		public void incrementUsage() {
			usage++;
		}

		public void decrementUsage() {
			usage--;
		}

		public boolean inUse() {
			return (usage != 0);
		}

	}

	Vector dbConnections; // Holds all connections

	/**
	 * Constructor
	 *  
	 */
	public DBConnections() {
		dbConnections = new Vector();
	}

	/**
	 * Returns an existing connection or creates and returns a new connection
	 * 
	 * @param dbName
	 * @param dbUser
	 * @param dbPassword
	 * @return The Connection object
	 */
	public Connection newConnection(String dURL, String dClassName,
			String dbName, String dbUser, String dbPassword) {
		// Check if connection exists
		if (!dbConnections.isEmpty()) {
			for (int i = 0; i < dbConnections.size(); i++) {
				if (((DBConnection) (dbConnections.get(i))).isConnection(dURL,
						dClassName, dbName, dbUser, dbPassword)) {
					System.out.println("o A Connection is found");
					((DBConnection) (dbConnections.get(i))).incrementUsage();
					return ((DBConnection) (dbConnections.get(i)))
							.getConnection();
				}
			}
		}

		// Create a new connection
		DBConnection newConn = new DBConnection(dURL, dClassName, dbName,
				dbUser, dbPassword);
		if (newConn.isValid()) {
			dbConnections.add(newConn);
			newConn.incrementUsage();
			System.out.println("+ A Connection is added");
			return newConn.getConnection();
		}

		return null; // No connection found or established

	}

	/**
	 * Fetches the tables of a specified connection Connection must be
	 * established or an error will occur.
	 * 
	 * @param dbConn
	 *            Connection object for which the tables shall be fetched
	 */
	public Object[] getTables(Connection dbConn) {
		if (dbConn == null)
			return null;
		// Fetch list of tables
		Application.getGUI().logPrintln("Fetching list of tables.");
		try {
			DatabaseMetaData dmd = dbConn.getMetaData();
			ResultSet rsTables = dmd.getTables(dbConn.getCatalog(), null, "%",
					new String[] { "TABLE", "VIEW" });

			// Produce list
			Vector items = new Vector();
			while (rsTables.next())
				items.add(rsTables.getString("TABLE_NAME"));
			rsTables.close();
			return items.toArray();
		} catch (Exception e) {
			Application.getGUI().showError("Unable to fetch list: ", e);
			return null;
		}
	}

	/**
	 * Closes a specific connection
	 * 
	 * @param dbConn
	 */
	public void close(Connection dbConn) {
		DBConnection currentConn;
		for (int i = 0; i < dbConnections.size(); i++) {
			currentConn = (DBConnection) (dbConnections.get(i));
			if (currentConn.isConnection(dbConn)) {
				currentConn.decrementUsage();
				try {
					if (!currentConn.inUse()) {
						currentConn.getConnection().close();
						dbConnections.removeElementAt(i);
						System.out.println("- A Connection is released");
					}
					return;
				} catch (SQLException e) {
					// Don't care.
				}
			}
		}
	}

	/**
	 * Close all connections
	 *  
	 */
	public void closeAll() {
		for (int i = 0; i < dbConnections.size(); i++) {
			try {
				((DBConnection) (dbConnections.get(i))).getConnection().close();
				dbConnections.removeAllElements();
			} catch (SQLException e) {
				// Don't care.
			}
		}
	}

}