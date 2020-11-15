package py.com.sodep.mobileforms.web.acme;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import py.com.sodep.mobileforms.api.dtos.widgets.GroupSelectOption;
import py.com.sodep.mobileforms.api.exceptions.ApplicationException;

public class FactorySelectItem {

	private static final String ID="id";
	private static final String LABEL="label";
	
	private static Map<String,String> getMap(String id,String label){
		HashMap<String, String> map=new HashMap<String, String>();
		map.put(ID,id);
		map.put(LABEL,label);
		return map;
	}
	/**
	 * This method will return a list of object of the form {id:"xxx",label:"yyyy"} . Those object are a json representation of a select item that are suitable to be used with a select-like component
	 * 
	 * @param data
	 *            a list of any object type. The object should contain at least
	 *            two accesible fields (valueField and labelField)
	 * @param idFieldName
	 *            A value that identify this item unequivocally (actually a toString of this field)
	 * @param labelFieldName
	 *            The label to be displayed  (actually a toString of this field)
	 * @return
	 */
	
	public static List<Map<String,String>>  toJSONSelectItem(List<?> data,String idFieldName, String labelFieldName) {

		ArrayList<Map<String,String>> dataTojsonify=new ArrayList<Map<String,String>>();
		for (Iterator<?> iterator = data.iterator(); iterator.hasNext();) {
			Object t = iterator.next();
			try {
				Object id = PropertyUtils.getProperty(t, idFieldName);
				Object label = PropertyUtils.getProperty(t, labelFieldName);
				dataTojsonify.add(getMap(id.toString(), label.toString()));
			} catch (IllegalAccessException e) {
				throw new ApplicationException("Error reading property of object "+t.getClass().getCanonicalName(),e);
			} catch (InvocationTargetException e) {
				throw new ApplicationException("Error reading property of object "+t.getClass().getCanonicalName(),e);
			} catch (NoSuchMethodException e) {
				throw new ApplicationException("Error reading property of object "+t.getClass().getCanonicalName(),e);
			}
		}
		
		return dataTojsonify;

	}
	
	
	/**
	 * This method will return a list of object of the form {id:"xxx",group:"zzz",label:"yyyy"} . 
	 * Those object are a json representation of a select item with groups that are suitable to be used with a select-like component
	 * 
	 * @param data
	 *            a list of any object type. The object should contain at least
	 *            three accesible fields (groupField,valueField and labelField)
	 * @param idFieldName
	 *            A value that identify this item unequivocally (actually a toString of this field)
	 * @param groupFieldName
	 *            The Group to be ordered  (actually a toString of this field)
	 * @param labelFieldName
	 *            The label to be displayed  (actually a toString of this field)
	 *
	 * @return
	 */
	
	public static List<GroupSelectOption>  toJSONSelectGroupItem(List<?> data,String idFieldName, String groupFieldName, String labelFieldName) {

		List<GroupSelectOption> dataTojsonify = new ArrayList<GroupSelectOption>();
		for (Iterator<?> iterator = data.iterator(); iterator.hasNext();) {
			Object t = iterator.next();
			try {
				Object id = PropertyUtils.getProperty(t, idFieldName);
				Object group = PropertyUtils.getProperty(t, groupFieldName);
				Object label = PropertyUtils.getProperty(t, labelFieldName);
				dataTojsonify.add(new GroupSelectOption(id.toString(), group.toString(), label.toString()));
			} catch (IllegalAccessException e) {
				throw new ApplicationException("Error reading property of object "+t.getClass().getCanonicalName(),e);
			} catch (InvocationTargetException e) {
				throw new ApplicationException("Error reading property of object "+t.getClass().getCanonicalName(),e);
			} catch (NoSuchMethodException e) {
				throw new ApplicationException("Error reading property of object "+t.getClass().getCanonicalName(),e);
			}
		}
		
		return dataTojsonify;
	}
}
