package py.com.sodep.mobileforms.api.entities.i18n;

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
import javax.persistence.MapKeyColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import py.com.sodep.mobileforms.api.entities.SodepEntity;
import py.com.sodep.mobileforms.api.services.i18n.I18nBundle;

/**
 * A given language has a set of labels that are arranged by key. A language is
 * identified by its name which should be one of the of ISO 639-1 codes.
 * (http://en.wikipedia.org/wiki/List_of_ISO_639-1_codes)
 * 
 * @see {@link I18nBundle}
 * @author Miguel
 * 
 */
@Entity
@Table(schema = "i18n", name = "languages")
@SequenceGenerator(name = "seq_languages", sequenceName = "i18n.seq_languages")
//FIXME This should be internationalized. The name of the language differs with user's selected language
public class I18nLanguage extends SodepEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String isoLanguage;

	private String name;

	private Map<String, String> labels;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = "seq_languages")
	@Column(unique = true, nullable = false)
	public Long getId() {
		return this.id;
	}

	@Column(name = "iso_language", unique = true, nullable = false)
	public String getIsoLanguage() {
		return isoLanguage;
	}

	public void setIsoLanguage(String isoLanguage) {
		this.isoLanguage = isoLanguage;
	}

	@ElementCollection(fetch = FetchType.LAZY)
	@Column(name = "value", length = 1024)
	@MapKeyColumn(name = "key", length = 512)
	@CollectionTable(schema = "i18n", name = "labels", joinColumns = @JoinColumn(name = "language_id"))
	public Map<String, String> getLabels() {
		return labels;
	}

	public void setLabels(Map<String, String> labels) {
		this.labels = labels;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
