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

package kieker.monitoring.queue.takestrategy;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

/**
 * This take strategy blocks if the queue is empty.
 * <br>
 * <i>IMPORTANT: This take strategy only works correctly if at most one consumer accesses the queue.</i>
 *
 * @author Christian Wulf
 *
 * @since 1.13
 */
public final class SCBlockingTakeStrategy<E> implements TakeStrategy<E> {

	private final AtomicReference<Thread> t = new AtomicReference<Thread>(null);

	public volatile int storeFence = 0; // NOCS

	@Override
	public void signal() {
		// Make sure the offer is visible before unpark
		this.storeFence = 1; // store barrier

		LockSupport.unpark(this.t.get()); // t.get() load barrier
	}

	@Override
	public E waitPoll(final Queue<E> q) throws InterruptedException {
		E e = q.poll();
		if (e != null) {
			return e;
		}

		this.t.set(Thread.currentThread());

		try {
			while ((e = q.poll()) == null) {
				LockSupport.park();
				if (Thread.currentThread().isInterrupted()) {
					throw new InterruptedException("Interrupted while waiting for the queue to become non-empty.");
				}
			}
		} finally {
			this.t.lazySet(null);
		}

		return e;
	}
}
