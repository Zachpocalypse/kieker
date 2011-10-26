/***************************************************************************
 * Copyright 2011 by
 *  + Christian-Albrechts-University of Kiel
 *    + Department of Computer Science
 *      + Software Engineering Group 
 *  and others.
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

package kieker.monitoring.core.registry;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;

/**
 * @author Andre van Hoorn, Jan Waller
 */
public final class ControlFlowRegistry {
	private static final Log LOG = LogFactory.getLog(ControlFlowRegistry.class);

	private final AtomicLong lastThreadId;
	private final ThreadLocal<Long> threadLocalTraceId = new ThreadLocal<Long>();
	private final ThreadLocal<Integer> threadLocalEoi = new ThreadLocal<Integer>();
	private final ThreadLocal<Integer> threadLocalEss = new ThreadLocal<Integer>();

	private ControlFlowRegistry() {
		/*
		 * In order to (probabilistically!) avoid that other instances in our
		 * system (on another node, in another vm, ...) generate the same thread
		 * ids, we fill the left-most 16 bits of the thread id with a uniquely
		 * distributed random number (0,0000152587890625 = 0,00152587890625 %).
		 * As a consequence, this constitutes a uniquely distributed offset of
		 * size 2^(64-1-16) = 2^47 = 140737488355328L in the worst case. Note
		 * that we restrict ourselves to the positive long values so far. Of
		 * course, negative values may occur (as a result of an overflow) --
		 * this does not hurt!
		 */
		final Random r = new Random();
		final long base = ((long) r.nextInt(65536) << (Long.SIZE - 16 - 1)); // NOCS

		this.lastThreadId = new AtomicLong(base);
		ControlFlowRegistry.LOG.info("First threadId will be " + (base + 1));
	}

	/**
	 * @return the singleton instance of ControlFlowRegistry
	 */
	public static final ControlFlowRegistry getInstance() {
		return LazyHolder.INSTANCE;
	}

	/**
	 * This methods returns a globally unique trace id.
	 * 
	 * @return a globally unique trace id.
	 */
	public final long getUniqueTraceId() {

		final long id = this.lastThreadId.incrementAndGet();
		// Since we use -1 as a marker for an invalid traceId,
		// it must not be returned!

		if (id == -1) {
			/*
			 * in this case, choose a valid threadId. Note, that this is not
			 * necessarily 0 due to concurrent executions of this method.
			 * 
			 * Example: like the following one, but it seems to fine:
			 * 
			 * (this.lastThreadId = -2) Thread A: id = -1 (inc&get -2)
			 * (this.lastThreadId = -1) Thread B: id = 0 (inc&get -1)
			 * (this.lastThreadId = 0) Thread A: returns 1 (because id == -1,
			 * and this.lastThreadId=0 in the meantime) (this.lastThreadId = 1)
			 * Thread B: returns 0 (because id != -1)
			 */
			return this.lastThreadId.incrementAndGet();
		} else {
			/* i.e., id <> 1 */
			return id;
		}
	}

	/**
	 * This method returns a thread-local traceid which is globally unique and
	 * stored it local for the thread. The thread is responsible for
	 * invalidating the stored curTraceId using the method
	 * unsetThreadLocalTraceId()!
	 */
	public final long getAndStoreUniqueThreadLocalTraceId() {
		final long id = this.getUniqueTraceId();
		this.threadLocalTraceId.set(id);
		return id;
	}

	/**
	 * This method stores a thread-local curTraceId. The thread is responsible
	 * for invalidating the stored curTraceId using the method
	 * unsetThreadLocalTraceId()!
	 */
	public final void storeThreadLocalTraceId(final long traceId) {
		this.threadLocalTraceId.set(traceId);
	}

	/**
	 * This method returns the thread-local traceid previously registered using
	 * the method registerTraceId(curTraceId).
	 * 
	 * @return the traceid. -1 if no curTraceId has been registered for this
	 *         thread.
	 */
	public final long recallThreadLocalTraceId() {
		final Long traceIdObj = this.threadLocalTraceId.get();
		if (traceIdObj == null) {
			return -1;
		}
		return traceIdObj;
	}

	/**
	 * This method unsets a previously registered traceid.
	 */
	public final void unsetThreadLocalTraceId() {
		this.threadLocalTraceId.remove();
	}

	/**
	 * Used to explicitly register an curEoi. The thread is responsible for
	 * invalidating the stored curTraceId using the method
	 * unsetThreadLocalEOI()!
	 */

	public final void storeThreadLocalEOI(final int eoi) {
		this.threadLocalEoi.set(eoi);
	}

	/**
	 * Since this method accesses a ThreadLocal variable, it is not (necessary
	 * to be) thread-safe.
	 */

	public final int incrementAndRecallThreadLocalEOI() {
		final Integer curEoi = this.threadLocalEoi.get();
		if (curEoi == null) {
			ControlFlowRegistry.LOG.error("eoi has not been registered before"); // NOCS (MultipleStringLiteralsCheck)
			return -1;
		}
		final int newEoi = curEoi + 1;
		this.threadLocalEoi.set(newEoi);
		return newEoi;
	}

	/**
	 * This method returns the thread-local curEoi previously registered using
	 * the method registerTraceId(curTraceId).
	 * 
	 * @return the sessionid. -1 if no curEoi registered.
	 */

	public final int recallThreadLocalEOI() {
		final Integer curEoi = this.threadLocalEoi.get();
		if (curEoi == null) {
			ControlFlowRegistry.LOG.error("eoi has not been registered before"); // NOCS (MultipleStringLiteralsCheck)
			return -1;
		}
		return curEoi;
	}

	/**
	 * This method unsets a previously registered traceid.
	 */

	public final void unsetThreadLocalEOI() {
		this.threadLocalEoi.remove();
	}

	/**
	 * Used to explicitly register a sessionid that is to be collected within a
	 * servlet method (that knows the request object). The thread is responsible
	 * for invalidating the stored curTraceId using the method
	 * unsetThreadLocalSessionId()!
	 */

	public final void storeThreadLocalESS(final int ess) {
		this.threadLocalEss.set(ess);
	}

	/**
	 * Since this method accesses a ThreadLocal variable, it is not (necessary
	 * to be) thread-safe.
	 */

	public final int recallAndIncrementThreadLocalESS() {
		final Integer curEss = this.threadLocalEss.get();
		if (curEss == null) {
			ControlFlowRegistry.LOG.error("ess has not been registered before"); // NOCS (MultipleStringLiteralsCheck)
			return -1;
		}
		this.threadLocalEss.set(curEss + 1);
		return curEss;
	}

	/**
	 * This method returns the thread-local curEss previously registered using
	 * the method registerTraceId(curTraceId).
	 * 
	 * @return the sessionid. -1 if no curEss registered.
	 */

	public final int recallThreadLocalESS() {
		final Integer ess = this.threadLocalEss.get();
		if (ess == null) {
			ControlFlowRegistry.LOG.error("ess has not been registered before"); // NOCS (MultipleStringLiteralsCheck)
			return -1;
		}
		return ess;
	}

	/**
	 * This method unsets a previously registered curEss.
	 */

	public final void unsetThreadLocalESS() {
		this.threadLocalEss.remove();
	}

	/**
	 * SINGLETON
	 */
	private static final class LazyHolder { // NOCS (MissingCtorCheck)
		private static final ControlFlowRegistry INSTANCE = new ControlFlowRegistry(); // NOPMD (AccessorClassGeneration)
	}
}
