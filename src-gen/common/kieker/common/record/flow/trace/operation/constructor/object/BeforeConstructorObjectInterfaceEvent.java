/***************************************************************************
 * Copyright 2013 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.common.record.flow.trace.operation.constructor.object;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import kieker.common.record.IMonitoringRecord;
import kieker.common.util.registry.IRegistry;

import kieker.common.record.flow.trace.operation.constructor.object.BeforeConstructorObjectEvent;
import kieker.common.record.flow.IInterfaceRecord;

/**
 * @author Generic Kieker
 * 
 * @since 1.10
 */
public class BeforeConstructorObjectInterfaceEvent extends BeforeConstructorObjectEvent implements IMonitoringRecord.Factory, IMonitoringRecord.BinaryFactory, IInterfaceRecord {
	/** Descriptive definition of the serialization size of the record. */
	public static final int SIZE = TYPE_SIZE_LONG // IEventRecord.timestamp
			 + TYPE_SIZE_LONG // ITraceRecord.traceId
			 + TYPE_SIZE_INT // ITraceRecord.orderIndex
			 + TYPE_SIZE_STRING // IClassSignature.classSignature
			 + TYPE_SIZE_STRING // IOperationRecord.operationSignature
			 + TYPE_SIZE_INT // IObjectRecord.objectId
			 + TYPE_SIZE_STRING // IInterfaceRecord.interface
	;
	private static final long serialVersionUID = -1522482789252285454L;
	
	private static final Class<?>[] TYPES = {
		Long.class, // IEventRecord.timestamp
		Long.class, // ITraceRecord.traceId
		Integer.class, // ITraceRecord.orderIndex
		String.class, // IClassSignature.classSignature
		String.class, // IOperationRecord.operationSignature
		Integer.class, // IObjectRecord.objectId
		String.class, // IInterfaceRecord.interface
	};
	
	
	private final String _interface;

	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param timestamp
	 *            timestamp
	 * @param traceId
	 *            traceId
	 * @param orderIndex
	 *            orderIndex
	 * @param classSignature
	 *            classSignature
	 * @param operationSignature
	 *            operationSignature
	 * @param objectId
	 *            objectId
	 * @param _interface
	 *            _interface
	 */
	public BeforeConstructorObjectInterfaceEvent(final long timestamp, final long traceId, final int orderIndex, final String classSignature, final String operationSignature, final int objectId, final String _interface) {
		super(timestamp, traceId, orderIndex, classSignature, operationSignature, objectId);
		this._interface = _interface;
	}

	/**
	 * This constructor converts the given array into a record. It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The values for the record.
	 */
	public BeforeConstructorObjectInterfaceEvent(final Object[] values) { // NOPMD (direct store of values)
		super(values, TYPES);
		this._interface = (String) values[6];
	}
	
	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 */
	protected BeforeConstructorObjectInterfaceEvent(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		super(values, valueTypes);
		this._interface = (String) values[6];
	}

	/**
	 * This constructor converts the given array into a record.
	 * 
	 * @param buffer
	 *            The bytes for the record.
	 * 
	 * @throws BufferUnderflowException
	 *             if buffer not sufficient
	 */
	public BeforeConstructorObjectInterfaceEvent(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		super(buffer, stringRegistry);
		this._interface = stringRegistry.get(buffer.getInt());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object[] toArray() {
		return new Object[] {
			this.getTimestamp(),
			this.getTraceId(),
			this.getOrderIndex(),
			this.getClassSignature(),
			this.getOperationSignature(),
			this.getObjectId(),
			this.getInterface()
		};
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void writeBytes(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferOverflowException {
		buffer.putLong(this.getTimestamp());
		buffer.putLong(this.getTraceId());
		buffer.putInt(this.getOrderIndex());
		buffer.putInt(stringRegistry.get(this.getClassSignature()));
		buffer.putInt(stringRegistry.get(this.getOperationSignature()));
		buffer.putInt(this.getObjectId());
		buffer.putInt(stringRegistry.get(this.getInterface()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getValueTypes() {
		return TYPES; // NOPMD
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSize() {
		return SIZE;
	}
	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.Factory} mechanism. Hence, this method is not implemented.
	 */
	@Override
	@Deprecated
	public void initFromArray(final Object[] values) {
		throw new UnsupportedOperationException();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated This record uses the {@link kieker.common.record.IMonitoringRecord.BinaryFactory} mechanism. Hence, this method is not implemented.
	 */
	@Override
	@Deprecated
	public void initFromBytes(final ByteBuffer buffer, final IRegistry<String> stringRegistry) throws BufferUnderflowException {
		throw new UnsupportedOperationException();
	}

	public final String getInterface() {
		return this._interface;
	}
	
}
