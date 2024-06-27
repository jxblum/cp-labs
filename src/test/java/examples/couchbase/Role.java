package examples.couchbase;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cp.elements.util.stream.StreamUtils;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Joh Blum
 */
@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
@Setter(AccessLevel.PROTECTED)
@SuppressWarnings("unused")
public class Role {

	public static Role as(Role.Type roleType) {
		return new Role(roleType);
	}

	public static Role asExecuteUser() {
		return as(Type.USER).with(Permission.EXECUTE);
	}

	public static Role asReadUser() {
		return as(Type.USER).with(Permission.READ);
	}

	public static Role asReadWriteUser() {
		return as(Type.USER).with(Permission.READ, Permission.WRITE);
	}

	public static Role asReadWriteDeveloper() {
		return as(Type.DEVELOPER).with(Permission.READ, Permission.WRITE);
	}

	private Set<Permission> permissions = new HashSet<>();

	private final Type type;

	public Role with(Permission... permissions) {
		return with(List.of(permissions));
	}

	public Role with(Iterable<Permission> permissions) {
		StreamUtils.stream(permissions).forEach(this::add);
		return this;
	}

	public Role add(Permission permission) {

		if (permission != null) {
			this.permissions.add(permission);
		}

		return this;
	}

	public Role remove(Permission permission) {
		this.permissions.remove(permission);
		return this;
	}

	public enum Type {
		ADMIN, USER, DEVELOPER, QA, DBA, GUEST
	}

	public enum Permission {
		READ, WRITE, EXECUTE
	}
}
