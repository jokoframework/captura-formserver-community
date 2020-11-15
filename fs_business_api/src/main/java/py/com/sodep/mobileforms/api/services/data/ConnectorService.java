package py.com.sodep.mobileforms.api.services.data;

import java.util.List;

import py.com.sodep.mobileforms.api.entities.application.Application;
import py.com.sodep.mobileforms.api.entities.data.Connector;
import py.com.sodep.mobileforms.api.entities.data.Connector.CONNECTOR_DIRECTION;
import py.com.sodep.mobileforms.api.entities.data.Connector.CONNECTOR_TYPE;
import py.com.sodep.mobileforms.api.services.metadata.PagedData;

public interface ConnectorService {

	public void addConnector(Application app, Connector connector);

	public PagedData<List<Connector>> findConnectors(Application app, String name, CONNECTOR_DIRECTION direction,
			CONNECTOR_TYPE type, String orderBy, boolean asc);

	public List<Connector> listAllConnector(Application app, String name, CONNECTOR_DIRECTION direction,
			CONNECTOR_TYPE type, String orderBy, boolean asc);

	public Connector remove(Long id);

}
