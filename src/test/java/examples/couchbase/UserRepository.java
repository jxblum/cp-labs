package examples.couchbase;

import java.util.UUID;

import org.springframework.data.couchbase.repository.CouchbaseRepository;

/**
 * @author Joh Blum
 */
@SuppressWarnings("unused")
public interface UserRepository extends CouchbaseRepository<User, UUID> {

	User findByName(String name);

}
