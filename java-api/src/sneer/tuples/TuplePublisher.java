package sneer.tuples;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import sneer.PublicKey;

import java.util.Map;

public interface TuplePublisher extends Action1<Object>, Action0 {

	TuplePublisher audience(PublicKey audience);
	TuplePublisher type(String type);
	TuplePublisher payload(Object payload);
	TuplePublisher field(String field, Object value);
	TuplePublisher putFields(Map<String, Object> fields);
	
	/** Publishes a tuple with the given payload. Equivalent to calling {@link #payload(Object)} and {@link #pub()}.
	 * @return The published tuple. */
	Observable<Tuple> pub(Object payload);

	/** Publishes a tuple with the values set in this publisher.
	 * @return The published tuple. */
	Observable<Tuple> pub();

}
