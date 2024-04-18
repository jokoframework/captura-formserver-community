package py.com.sodep.mobileforms.impl.services.data;

import java.net.UnknownHostException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

import py.com.sodep.mobileforms.api.services.data.IMongoConnection;

public class MongoConnectionImpl implements IMongoConnection {

	private String database;

	private int port;

	private String host;

	private Mongo mongo;

	private String user;
	
	private String pwd;
	
	public MongoConnectionImpl(@Value("${mongo.database}") String database,
			@Value("${mongo.por}") int port, 
			@Value("${mongo.host}") String host, 
			@Value("${mongo.user}") String user, 
			@Value("${mongo.pwd}") String pwd) {
		super();
		this.database = database;
		this.port = port;
		this.host = host;
		this.user = user;
		this.pwd = pwd;
	}

	@Override
	public IMongoConnection newClient() throws UnknownHostException {
		MongoClientOptions options = MongoClientOptions.builder()
				.maxWaitTime(1000 * 30)
				.build();
		
		// The default authentication mechanism for Mongo 3.2 (version used in
		// production) is SCRAMSHA1.
		// To be able to authenticate we upgraded the mongo driver to the
		// minimum compatible version of the driver with Mongo 3.2, so that
		// we don't break other API Driver usages.
		// See the compatibility table here:
		// https://docs.mongodb.com/ecosystem/drivers/java/#mongodb-compatibility
		// More info here:
		// http://stackoverflow.com/questions/39546543/java-mongo-db-authentication-failed
		MongoCredential credential = MongoCredential.createScramSha1Credential(user, database, pwd.toCharArray());
		mongo = new MongoClient(new ServerAddress(host, port), Arrays.asList(credential), options);
		return this;
	}
	
	@Override
	public DB getDatabase() {
		Assert.notNull(mongo, "mongo is null. Did you call buildClient()?");
		return this.mongo.getDB(database);
	}
	
}
