package py.com.sodep.mobileforms.web.listener;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneralServletContextListener implements ServletContextListener {

	private static final Logger logger = LoggerFactory.getLogger(GeneralServletContextListener.class);

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		logger.info("Context destroyed");
		unregisterJDBCDriver();
	}

	private void unregisterJDBCDriver() {
		// TODO : Move this method to a "utility" class regarding app lifecycle

		// This manually deregisters JDBC driver, which prevents Tomcat 7 from
		// complaining about memory leaks wrto this class
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			try {
				DriverManager.deregisterDriver(driver);
				logger.info(String.format("Deregistering jdbc driver: %s", driver));
			} catch (SQLException e) {
				logger.info(String.format("Error deregistering driver %s", driver), e);
			}

		}
	}

	@Override
	public void contextInitialized(ServletContextEvent ctx) {
		logger.info("Initializing context");
		logEnvironmentVariables();
	}

	private void logEnvironmentVariables() {
		logger.debug("Environment variables");
		Map<String, String> env = System.getenv();
		for (String envName : env.keySet()) {
			logger.debug("EV: " + envName + "=" + env.get(envName));
		}

		Properties properties = System.getProperties();
		Set<Entry<Object, Object>> entrySet = properties.entrySet();
		for (Entry<Object, Object> entry : entrySet) {
			logger.debug("SP: " + entry.getKey() + "=" + entry.getValue());
		}
	}

}
