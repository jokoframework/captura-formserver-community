package py.com.sodep.mobileforms.impl.services.data;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import py.com.sodep.mobileforms.api.services.data.MFFileStream;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * This is wrapper over {@link GridFSDBFile} to avoid exposing mongo concepts out of {@link DataAccessService}
 * @author danicricco
 *
 */
public class MFFileStreamMongo implements MFFileStream {
	private GridFSDBFile gridFSDBFile;
	public MFFileStreamMongo(GridFSDBFile gridFSDBFile){
		this.gridFSDBFile=gridFSDBFile;
		
	}
	
	public void write(OutputStream stream) throws IOException{
		gridFSDBFile.writeTo(stream);
	}
	
	public InputStream getInputStream(){
		return gridFSDBFile.getInputStream();
	}
	
}
