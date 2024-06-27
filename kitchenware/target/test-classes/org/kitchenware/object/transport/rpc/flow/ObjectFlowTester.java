package org.kitchenware.object.transport.rpc.flow;

import java.math.BigDecimal;
import java.util.Set;

import org.kitchenware.express.io.ByteBufferedInputStream;
import org.kitchenware.express.io.ByteBufferedOutputStream;
import org.kitchenware.express.util.CollectionObjects;
import org.kitchenware.express.util.StringObjects;

public class ObjectFlowTester {

	public static void main(String[] args) throws Throwable {
		TesterObject object = new TesterObject()
				.setName("name")
				.setValue(BigDecimal.ZERO)
				.setItems(CollectionObjects.doSet("1,2"))
				;
		
		ObjectSerialize serialize = new ObjectSerialize(object);
		
		ByteBufferedOutputStream buf = new ByteBufferedOutputStream();
		
		serialize.writeObject(buf);
		
		ObjectDeserialize deserialize = new ObjectDeserialize(new ByteBufferedInputStream(buf.toByteArray()));
		
		object = deserialize.readObject();
		
		System.out.println(object.toString());
	}

	static class TesterObject {
		String name;

		BigDecimal value;

		Set<String> items;

		public String getName() {
			return name;
		}

		public ObjectFlowTester.TesterObject setName(String name) {
			this.name = name;
			return ObjectFlowTester.TesterObject.this;
		}

		public BigDecimal getValue() {
			return value;
		}

		public ObjectFlowTester.TesterObject setValue(BigDecimal value) {
			this.value = value;
			return ObjectFlowTester.TesterObject.this;
		}

		public Set<String> getItems() {
			return items;
		}

		public ObjectFlowTester.TesterObject setItems(Set<String> items) {
			this.items = items;
			return ObjectFlowTester.TesterObject.this;
		}

		
		@Override
		public String toString() {
			StringBuilder buf = new StringBuilder()
					.append(String.format("name: %s", this.name))
					.append(StringObjects.LINE_SEPARATOR	).append(String.format("value: %s", this.value))
					.append(StringObjects.LINE_SEPARATOR).append(String.format("items: %s", CollectionObjects.toString(this.items)))
					;
			return buf.toString();
		}
	}
}
