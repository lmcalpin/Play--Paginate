package play.modules.paginate;

import javax.persistence.Entity;

import play.db.jpa.Model;

@Entity(name="AnotherModel")
public class AltMockModel extends Model {
	private static final long serialVersionUID = 1L;
	public String testKey;
	public String testValue;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((testKey == null) ? 0 : testKey.hashCode());
		result = prime * result
				+ ((testValue == null) ? 0 : testValue.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AltMockModel other = (AltMockModel) obj;
		if (testKey == null) {
			if (other.testKey != null)
				return false;
		} else if (!testKey.equals(other.testKey))
			return false;
		if (testValue == null) {
			if (other.testValue != null)
				return false;
		} else if (!testValue.equals(other.testValue))
			return false;
		return true;
	}
}