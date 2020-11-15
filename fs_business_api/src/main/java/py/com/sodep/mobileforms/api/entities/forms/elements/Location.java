package py.com.sodep.mobileforms.api.entities.forms.elements;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(schema = "forms", name = "elements_locations")
public class Location extends ElementPrototype {

	private Double defaultLatitude;
	private Double defaultLongitude;

	private static final long serialVersionUID = 1L;

	@Column(name = "default_latitude")
	public Double getDefaultLatitude() {
		return defaultLatitude;
	}

	public void setDefaultLatitude(Double defaultLatitude) {
		this.defaultLatitude = defaultLatitude;
	}

	@Column(name = "default_longitude")
	public Double getDefaultLongitude() {
		return defaultLongitude;
	}

	public void setDefaultLongitude(Double defaultLongitude) {
		this.defaultLongitude = defaultLongitude;
	}
}
