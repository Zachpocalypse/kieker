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

package kieker.toolsteetime.logReplayer.filter;

import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.controller.IMonitoringController;

import teetime.stage.basic.AbstractFilter;

/**
 * Passes {@link IMonitoringRecord}s received via its input port to its own {@link IMonitoringController} instance,
 * which is passed in the constructor. Additionally, incoming records are relayed via the output port.
 *
 * @author Andre van Hoorn, Lars Bluemke
 *
 * @since 1.6
 */
public class MonitoringRecordLoggerFilter extends AbstractFilter<IMonitoringRecord> {

	/**
	 * The {@link IMonitoringController} the received records are passed to.
	 */
	private final IMonitoringController monitoringController;

	/**
	 * Creates a new instance of this class using the given parameters.
	 *
	 * @param monitoringController
	 *            The monitoring controller from the monitoring component.
	 */
	public MonitoringRecordLoggerFilter(final IMonitoringController monitoringController) {
		this.monitoringController = monitoringController;
	}

	/**
	 * This method represents the input port. The new records are send to the monitoring controller before they are delivered via the output port.
	 *
	 * @param record
	 *            The next record.
	 */
	@Override
	protected void execute(final IMonitoringRecord record) {
		this.monitoringController.newMonitoringRecord(record);
		this.getOutputPort().send(record);
	}
}