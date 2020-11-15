package py.com.sodep.mobileforms.api.services.data;

import java.net.UnknownHostException;

import com.mongodb.DB;

public interface IMongoConnection {

	DB getDatabase();

	IMongoConnection newClient() throws UnknownHostException;

}
