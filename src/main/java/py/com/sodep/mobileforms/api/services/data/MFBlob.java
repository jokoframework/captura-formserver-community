package py.com.sodep.mobileforms.api.services.data;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.bson.types.ObjectId;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

/**
 * This is analog to a blob object in a traditional SQL database. This kind of
 * object kind be used to store binary data. If the object has a non null array
 * of bytes, then this array will be used. Otherwise, the InputStream will be
 * used. If neither the array nor the inputStream are set, then an exception
 * will be thrown.
 * 
 * @author danicricco
 * 
 */
public class MFBlob implements MFStorable {

	public static final String FIELD_FILEID = "fileId";
	public static final String FIELD_FILENAME = "fileName";
	public static final String FIELD_CONTENTTYPE="contentType";

	private String fileId;
	private String fileName;

	private byte data[];
	private InputStream stream;
	private String contentType;

	public MFBlob() {

	}

	public MFBlob(String fileName, String filePath) throws FileNotFoundException {
		this(fileName, new FileInputStream(filePath));
	}

	public MFBlob(String fileName, InputStream stream) {
		this.fileName = fileName;
		this.stream = stream;
	}

	public MFBlob(String fileName, byte data[]) {
		this.fileName = fileName;
		this.data = data;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public InputStream getStream() {
		return stream;
	}

	public void setStream(InputStream stream) {
		this.stream = stream;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	@Override
	public DBObject toMongo() {
		BasicDBObject obj = new BasicDBObject();
		obj.put(FIELD_FILEID, new ObjectId(fileId));
		obj.put(FIELD_FILENAME, fileName);
		obj.put(FIELD_CONTENTTYPE, contentType);
		return obj;
	}

	@Override
	public void fromMongo(DBObject o) {
		fileId = ((ObjectId) o.get(FIELD_FILEID)).toString();
		fileName = (String) o.get(FIELD_FILENAME);
		contentType=(String)o.get(FIELD_CONTENTTYPE);
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

}
