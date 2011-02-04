package kieker.monitoring.core;

/*
 * ==================LICENCE=========================
 * Copyright 2006-2011 Kieker Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==================================================
 */
/**
 * @author Jan Waller
 */
interface IController {

	/**
	 * Permanently terminates monitoring
	 * 
	 * @see #isMonitoringTerminated()
	 * @return true if now terminated; false if already terminated
	 */
	public abstract boolean terminateMonitoring();

	/**
	 * Returns whether monitoring is permanently terminated.
	 * 
	 * @see #terminateMonitoring()
	 * @return true if monitoring is permanently terminated, false if monitoring is enabled or disabled.
	 */
	public abstract boolean isMonitoringTerminated();
	
	/**
	 * Returns the name of this controller.
	 * 
	 * @return String
	 */
	public abstract String getName();

	/**
	 * a String representation of the current state
	 * 
	 * @return String
	 */
	public abstract String getState();
}
