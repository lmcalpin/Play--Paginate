package play.modules.paginate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.jpa.GenericModel;

@Entity
public class MockGenericModel extends GenericModel {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(length=25)
	public String testKey;
    @Column(length=55)
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
		MockGenericModel other = (MockGenericModel) obj;
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