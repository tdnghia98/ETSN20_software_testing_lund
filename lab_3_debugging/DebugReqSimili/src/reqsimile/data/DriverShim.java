/*
 * Created on 2004-jul-31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package reqsimile.data;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

/**
 * This is a shim class used as a workaround to enable dynamic loading of JDBC
 * driver at runtime The workaround makes it possible to use a driver that is
 * not in the classpath, nor in the jar.
 * 
 * @author Johan Natt och Dag
 * @since 1.4.2
 */
public class DriverShim implements Driver {
	private Driver driver;

	/**
	 * Constructor
	 *  
	 */
	public DriverShim(Driver d) {
		this.driver = d;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Driver#getMajorVersion()
	 */
	public int getMajorVersion() {
		return this.driver.getMajorVersion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Driver#getMinorVersion()
	 */
	public int getMinorVersion() {
		return this.driver.getMinorVersion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Driver#jdbcCompliant()
	 */
	public boolean jdbcCompliant() {
		return this.driver.jdbcCompliant();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Driver#acceptsURL(java.lang.String)
	 */
	public boolean acceptsURL(String arg0) throws SQLException {
		return this.driver.acceptsURL(arg0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Driver#connect(java.lang.String, java.util.Properties)
	 */
	public Connection connect(String arg0, Properties arg1) throws SQLException {
		return this.driver.connect(arg0, arg1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Driver#getPropertyInfo(java.lang.String,
	 *      java.util.Properties)
	 */
	public DriverPropertyInfo[] getPropertyInfo(String arg0, Properties arg1)
			throws SQLException {
		return this.driver.getPropertyInfo(arg0, arg1);
	}

}