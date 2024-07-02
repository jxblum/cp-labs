package examples.couchbase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cp.elements.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Joh Blum
 */
@Getter
@Document
@ToString(of = "name")
@EqualsAndHashCode(of = { "name" })
@SuppressWarnings("unused")
public class User {

	public static User named(String name) {
		User user = new User();
		user.name = StringUtils.requireText(name, "Name [%s] is required");
		return user;
	}

	@Id
	@Setter
	private UUID id;

	@Field
	private String name;

	//private List<Role> roles = new ArrayList<>();
	private Object roles = new ArrayList<>();

	public List<Role> getRoles() {
		return asList(this.roles);
	}

	public User add(Role role) {

		if (role != null) {
			asList(this.roles).add(role);
		}

		return this;
	}

	public User remove(Role role) {
		asList(this.roles).remove(role);
		return this;
	}

	@SuppressWarnings("unchecked")
	private <T> List<T> asList(Object target) {
		return ((List<T>) target);
	}
}
