/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
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

package kieker.test.common.junit.api.system;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import kieker.common.record.io.DefaultValueDeserializer;
import kieker.common.record.io.DefaultValueSerializer;
import kieker.common.record.system.LoadAverageRecord;
import kieker.common.util.registry.IRegistry;
import kieker.common.util.registry.Registry;
import kieker.test.common.junit.AbstractKiekerTest;
import kieker.test.common.junit.util.APIEvaluationFunctions;
			
/**
 * Test API of {@link kieker.common.record.system.LoadAverageRecord}.
 * 
 * @author API Checker
 * 
 * @since 1.12
 */
public class TestLoadAverageRecordPropertyOrder extends AbstractKiekerTest {

	/**
	 * All numbers and values must be pairwise unequal. As the string registry also uses integers,
	 * we must guarantee this criteria by starting with 1000 instead of 0.
	 */
	/** Constant value parameter for timestamp. */
	private static final long PROPERTY_TIMESTAMP = 2L;
	/** Constant value parameter for hostname. */
	private static final String PROPERTY_HOSTNAME = "<hostname>";
	/** Constant value parameter for oneMinLoadAverage. */
	private static final double PROPERTY_ONE_MIN_LOAD_AVERAGE = 2.0;
	/** Constant value parameter for fiveMinLoadAverage. */
	private static final double PROPERTY_FIVE_MIN_LOAD_AVERAGE = 3.0;
	/** Constant value parameter for fifteenMinLoadAverage. */
	private static final double PROPERTY_FIFTEEN_MIN_LOAD_AVERAGE = 4.0;
							
	/**
	 * Empty constructor.
	 */
	public TestLoadAverageRecordPropertyOrder() {
		// Empty constructor for test class.
	}

	/**
	 * Test property order processing of {@link kieker.common.record.system.LoadAverageRecord} constructors and
	 * different serialization routines.
	 */
	@Test
	public void testLoadAverageRecordPropertyOrder() { // NOPMD
		final IRegistry<String> stringRegistry = this.makeStringRegistry();
		final Object[] values = {
			PROPERTY_TIMESTAMP,
			PROPERTY_HOSTNAME,
			PROPERTY_ONE_MIN_LOAD_AVERAGE,
			PROPERTY_FIVE_MIN_LOAD_AVERAGE,
			PROPERTY_FIFTEEN_MIN_LOAD_AVERAGE,
		};
		final ByteBuffer inputBuffer = APIEvaluationFunctions.createByteBuffer(LoadAverageRecord.SIZE, 
			this.makeStringRegistry(), values);
					
		final LoadAverageRecord recordInitParameter = new LoadAverageRecord(
			PROPERTY_TIMESTAMP,
			PROPERTY_HOSTNAME,
			PROPERTY_ONE_MIN_LOAD_AVERAGE,
			PROPERTY_FIVE_MIN_LOAD_AVERAGE,
			PROPERTY_FIFTEEN_MIN_LOAD_AVERAGE
		);
		final LoadAverageRecord recordInitBuffer = new LoadAverageRecord(DefaultValueDeserializer.instance(), inputBuffer, this.makeStringRegistry());
		final LoadAverageRecord recordInitArray = new LoadAverageRecord(values);
		
		this.assertLoadAverageRecord(recordInitParameter);
		this.assertLoadAverageRecord(recordInitBuffer);
		this.assertLoadAverageRecord(recordInitArray);

		// test to array
		final Object[] valuesParameter = recordInitParameter.toArray();
		Assert.assertArrayEquals("Result array of record initialized by parameter constructor differs from predefined array.", values, valuesParameter);
		final Object[] valuesBuffer = recordInitBuffer.toArray();
		Assert.assertArrayEquals("Result array of record initialized by buffer constructor differs from predefined array.", values, valuesBuffer);
		final Object[] valuesArray = recordInitArray.toArray();
		Assert.assertArrayEquals("Result array of record initialized by parameter constructor differs from predefined array.", values, valuesArray);

		// test write to buffer
		final ByteBuffer outputBufferParameter = ByteBuffer.allocate(LoadAverageRecord.SIZE);
		recordInitParameter.writeBytes(DefaultValueSerializer.instance(), outputBufferParameter, stringRegistry);
		Assert.assertArrayEquals("Byte buffer do not match (parameter).", inputBuffer.array(), outputBufferParameter.array());

		final ByteBuffer outputBufferBuffer = ByteBuffer.allocate(LoadAverageRecord.SIZE);
		recordInitParameter.writeBytes(DefaultValueSerializer.instance(), outputBufferBuffer, stringRegistry);
		Assert.assertArrayEquals("Byte buffer do not match (buffer).", inputBuffer.array(), outputBufferBuffer.array());

		final ByteBuffer outputBufferArray = ByteBuffer.allocate(LoadAverageRecord.SIZE);
		recordInitParameter.writeBytes(DefaultValueSerializer.instance(), outputBufferArray, stringRegistry);
		Assert.assertArrayEquals("Byte buffer do not match (array).", inputBuffer.array(), outputBufferArray.array());
	}

	/**
	 * Assertions for LoadAverageRecord.
	 */
	private void assertLoadAverageRecord(final LoadAverageRecord record) {
		Assert.assertEquals("'timestamp' value assertion failed.", record.getTimestamp(), PROPERTY_TIMESTAMP);
		Assert.assertEquals("'hostname' value assertion failed.", record.getHostname(), PROPERTY_HOSTNAME);
		Assert.assertEquals("'oneMinLoadAverage' value assertion failed.", record.getOneMinLoadAverage(), PROPERTY_ONE_MIN_LOAD_AVERAGE, 0.1);
		Assert.assertEquals("'fiveMinLoadAverage' value assertion failed.", record.getFiveMinLoadAverage(), PROPERTY_FIVE_MIN_LOAD_AVERAGE, 0.1);
		Assert.assertEquals("'fifteenMinLoadAverage' value assertion failed.", record.getFifteenMinLoadAverage(), PROPERTY_FIFTEEN_MIN_LOAD_AVERAGE, 0.1);
	}
			
	/**
	 * Build a populated string registry for all tests.
	 */
	private IRegistry<String> makeStringRegistry() {
		final IRegistry<String> stringRegistry = new Registry<String>();
		// get registers string and returns their ID
		stringRegistry.get(PROPERTY_HOSTNAME);

		return stringRegistry;
	}
}
