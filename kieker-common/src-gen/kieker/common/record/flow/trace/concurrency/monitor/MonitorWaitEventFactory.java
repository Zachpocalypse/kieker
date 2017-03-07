package kieker.common.record.flow.trace.concurrency.monitor;

import java.nio.ByteBuffer;

import kieker.common.record.factory.IRecordFactory;
import kieker.common.record.io.IValueDeserializer;
import kieker.common.util.registry.IRegistry;

/**
 * @author Jan Waller
 * 
 * @since 1.8
 */
public final class MonitorWaitEventFactory implements IRecordFactory<MonitorWaitEvent> {
	
	@Override
	public MonitorWaitEvent create(final IValueDeserializer deserializer, final ByteBuffer buffer, final IRegistry<String> stringRegistry) {
		return new MonitorWaitEvent(deserializer, buffer, stringRegistry);
	}
	
	@Override
	public MonitorWaitEvent create(final Object[] values) {
		return new MonitorWaitEvent(values);
	}
	
	@Override
	public int getRecordSizeInBytes() {
		return MonitorWaitEvent.SIZE;
	}
}
