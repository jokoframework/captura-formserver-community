package py.com.sodep.mobileforms.api.entities.forms;

import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import py.com.sodep.mobileforms.api.entities.SodepEntity;

/**
 * This entity should be used to avoid repetitive generation of the XML
 * definition of a form.
 * 
 * The generated XML text should be indexed by language.
 * 
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "forms", name = "form_xml_cache", uniqueConstraints = { @UniqueConstraint(columnNames = "form_id") })
@SequenceGenerator(name = "seq_forms_xml_cache", sequenceName = "forms.seq_forms_xml_cache")
public class FormXmlCache extends SodepEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Form form;

	private Map<String, String> xml;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_forms_xml_cache")
	@Column(unique = true, nullable = false)
	//@Column(insertable = false, updatable = false)
	public Long getId() {
		return this.id;
	}

	@ManyToOne
	@JoinColumn(name = "form_id", nullable = false)
	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	/**
	 * XML text by language
	 * 
	 * @return XML text by language
	 */
	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "value", columnDefinition = "TEXT")
	@MapKeyColumn(name = "language", length = 16)
	@CollectionTable(schema = "forms", name = "form_xml_cache_xml", joinColumns = @JoinColumn(name = "form_xml_cache_id"))
	public Map<String, String> getXml() {
		return xml;
	}

	public void setXml(Map<String, String> xml) {
		this.xml = xml;
	}
}
