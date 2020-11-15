package py.com.sodep.mobileforms.api.services.data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/***
 * This is a stream over a saved file, to avoid loading it completely on memory.
 * For example, the method {@link #write(OutputStream)} can be used directly to
 * the outputstream of a Servlet
 * 
 * @author danicricco
 * 
 */
public interface MFFileStream {

	public void write(OutputStream stream) throws IOException;

	public InputStream getInputStream();
}
