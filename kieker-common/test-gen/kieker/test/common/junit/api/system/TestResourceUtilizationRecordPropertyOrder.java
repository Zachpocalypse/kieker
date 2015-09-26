/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
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

import kieker.common.record.system.ResourceUtilizationRecord;
import kieker.common.util.registry.IRegistry;
import kieker.common.util.registry.Registry;

import kieker.test.common.junit.AbstractKiekerTest;
import kieker.test.common.junit.util.APIEvaluationFunctions;
			
/**
 * Test API of {@link kieker.common.record.system.ResourceUtilizationRecord}.
 * 
 * @author API Checker
 * 
 * @since 1.11
 */
public class TestResourceUtilizationRecordPropertyOrder extends AbstractKiekerTest {

	/**
	 * All numbers and values must be pairwise unequal. As the string registry also uses integers,
	 * we must guarantee this criteria by starting with 1000 instead of 0.
	 */
	/** Constant value parameter for timestamp. */
	private static final long PROPERTY_TIMESTAMP = 2L;
	/** Constant value parameter for hostname. */
	private static final String PROPERTY_HOSTNAME = "<hostname>";
	/** Constant value parameter for resourceName. */
	private static final String PROPERTY_RESOURCE_NAME = "<resourceName>";
	/** Constant value parameter for utilization. */
	private static final double PROPERTY_UTILIZATION = 2.0;
							
	/**
	 * Empty constructor.
	 */
	public TestResourceUtilizationRecordPropertyOrder() {
		// Empty constructor for test class.
	}

	/**
	 * Test property order processing of {@link kieker.common.record.system.ResourceUtilizationRecord} constructors and
	 * different serialization routines.
	 */
	@Test
	public void testResourceUtilizationRecordPropertyOrder() { // NOPMD
		final IRegistry<String> stringRegistry = this.makeStringRegistry();
		final Object[] values = {
			PROPERTY_TIMESTAMP,
			PROPERTY_HOSTNAME,
			PROPERTY_RESOURCE_NAME,
			PROPERTY_UTILIZATION,
		};
		final ByteBuffer inputBuffer = APIEvaluationFunctions.createByteBuffer(ResourceUtilizationRecord.SIZE, 
			this.makeStringRegistry(), values);
					
		final ResourceUtilizationRecord recordInitParameter = new ResourceUtilizationRecord(
			PROPERTY_TIMESTAMP,
			PROPERTY_HOSTNAME,
			PROPERTY_RESOURCE_NAME,
			PROPERTY_UTILIZATION
		);
		final ResourceUtilizationRecord recordInitBuffer = new ResourceUtilizationRecord(inputBuffer, this.makeStringRegistry());
		final ResourceUtilizationRecord recordInitArray = new ResourceUtilizationRecord(values);
		
		this.assertResourceUtilizationRecord(recordInitParameter);
		this.assertResourceUtilizationRecord(recordInitBuffer);
		this.assertResourceUtilizationRecord(recordInitArray);

		// test to array
		final Object[] valuesParameter = recordInitParameter.toArray();
		Assert.assertArrayEquals("Result array of record initialized by parameter constructor differs from predefined array.", values, valuesParameter);
		final Object[] valuesBuffer = recordInitBuffer.toArray();
		Assert.assertArrayEquals("Result array of record initialized by buffer constructor differs from predefined array.", values, valuesBuffer);
		final Object[] valuesArray = recordInitArray.toArray();
		Assert.assertArrayEquals("Result array of record initialized by parameter constructor differs from predefined array.", values, valuesArray);

		// test write to buffer
		final ByteBuffer outputBufferParameter = ByteBuffer.allocate(ResourceUtilizationRecord.SIZE);
		recordInitParameter.writeBytes(outputBufferParameter, stringRegistry);
		Assert.assertArrayEquals("Byte buffer do not match (parameter).", inputBuffer.array(), outputBufferParameter.array());

		final ByteBuffer outputBufferBuffer = ByteBuffer.allocate(ResourceUtilizationRecord.SIZE);
		recordInitParameter.writeBytes(outputBufferBuffer, stringRegistry);
		Assert.assertArrayEquals("Byte buffer do not match (buffer).", inputBuffer.array(), outputBufferBuffer.array());

		final ByteBuffer outputBufferArray = ByteBuffer.allocate(ResourceUtilizationRecord.SIZE);
		recordInitParameter.writeBytes(outputBufferArray, stringRegistry);
		Assert.assertArrayEquals("Byte buffer do not match (array).", inputBuffer.array(), outputBufferArray.array());
	}

	/**
	 * Assertions for ResourceUtilizationRecord.
	 */
	private void assertResourceUtilizationRecord(final ResourceUtilizationRecord record) {
		Assert.assertEquals("'timestamp' value assertion failed.", record.getTimestamp(), PROPERTY_TIMESTAMP);
		Assert.assertEquals("'hostname' value assertion failed.", record.getHostname(), PROPERTY_HOSTNAME);
		Assert.assertEquals("'resourceName' value assertion failed.", record.getResourceName(), PROPERTY_RESOURCE_NAME);
		Assert.assertEquals("'utilization' value assertion failed.", record.getUtilization(), PROPERTY_UTILIZATION, 0.1);
	}
			
	/**
	 * Build a populated string registry for all tests.
	 */
	private IRegistry<String> makeStringRegistry() {
		final IRegistry<String> stringRegistry = new Registry<String>();
		// get registers string and returns their ID
		stringRegistry.get(PROPERTY_HOSTNAME);
		stringRegistry.get(PROPERTY_RESOURCE_NAME);

		return stringRegistry;
	}
}
