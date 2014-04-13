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

package kieker.panalysis.stage;

import kieker.panalysis.base.AbstractFilter;
import kieker.panalysis.base.IInputPort;
import kieker.panalysis.base.IOutputPort;

/**
 * @author Christian Wulf
 * 
 * @since 1.10
 */
public class Distributor<T> extends AbstractFilter<Distributor<T>, T, T> {

	public final IInputPort<Distributor<T>, T> OBJECT = this.createInputPort();

	private int index = 0;

	// TODO add parameter: MergeStrategy
	/**
	 * @since 1.10
	 */
	public Distributor(int numOutputPorts) {
		while (numOutputPorts-- > 0) {
			this.createOutputPort();
		}
	}

	/**
	 * @since 1.10
	 */
	public boolean execute() {
		final T object = this.tryTake(this.OBJECT);
		if (object == null) {
			return false;
		}
		final IOutputPort<Distributor<T>, T> port = this.getNextPortInRoundRobinOrder();
		this.put(port, object);
		return true;
	}

	/**
	 * @since 1.10
	 * @return
	 */
	private IOutputPort<Distributor<T>, T> getNextPortInRoundRobinOrder() {
		final IOutputPort<Distributor<T>, T> port = this.getOutputPort(this.index);
		this.index = (this.index + 1) % this.getOutputPorts().size();
		return port;
	}

	/**
	 * @since 1.10
	 * @param portIndex
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public IOutputPort<Distributor<T>, T> getOutputPort(final int portIndex) {
		final IOutputPort<Distributor<T>, ? extends T> port = this.getOutputPorts().get(portIndex);
		return (IOutputPort<Distributor<T>, T>) port;
	}
}
