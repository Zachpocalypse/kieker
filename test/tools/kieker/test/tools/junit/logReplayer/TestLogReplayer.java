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

package kieker.test.tools.junit.logReplayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import kieker.analysis.IAnalysisController;
import kieker.analysis.plugin.reader.AbstractReaderPlugin;
import kieker.analysis.plugin.reader.list.ListReader;
import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.misc.EmptyRecord;
import kieker.common.record.system.MemSwapUsageRecord;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.tools.logReplayer.AbstractLogReplayer;

import kieker.test.common.junit.AbstractKiekerTest;
import kieker.test.monitoring.util.NamedListWriter;

/**
 * Tests the {@link AbstractLogReplayer}.
 * 
 * @author Andre van Hoorn
 * 
 * @since 1.6
 */
public class TestLogReplayer extends AbstractKiekerTest {

	@Rule
	public final TemporaryFolder tmpFolder = new TemporaryFolder(); // NOCS (@Rule must be public)

	private volatile File monitoringConfigurationFile;
	private volatile List<IMonitoringRecord> recordListFilledByListWriter;
	private final List<IMonitoringRecord> replayList = new ArrayList<IMonitoringRecord>();

	public TestLogReplayer() {
		/* Adding arbitrary records */
		this.replayList.add(new EmptyRecord());
		this.replayList.add(
				new MemSwapUsageRecord(1, "myHost", /* memTotal: */17, /* memUsed */3, /* memFree: */14, /* swapTotal: */100, /* swapUsed: */0, /* swapFree: */100));
		this.replayList.add(new EmptyRecord());
	}

	@Before
	public void init() throws IOException {
		this.tmpFolder.create();
		final Configuration config = ConfigurationFactory.createDefaultConfiguration();
		config.setProperty(ConfigurationFactory.METADATA, "false");
		final String listName = NamedListWriter.FALLBACK_LIST_NAME;
		this.recordListFilledByListWriter = NamedListWriter.createNamedList(listName);
		config.setProperty(ConfigurationFactory.WRITER_CLASSNAME, NamedListWriter.class.getName());
		// Doesn't work because property not known to Kieker: System.setProperty(NamedListWriter.CONFIG_PROPERTY_NAME_LIST_NAME, this.listName);
		this.monitoringConfigurationFile = this.tmpFolder.newFile("moitoring.properties");
		final FileOutputStream fos = new FileOutputStream(this.monitoringConfigurationFile);
		try {
			config.store(fos, "Generated by " + TestLogReplayer.class.getName());
		} finally {
			fos.close();
		}

	}

	// TODO: we should add test variants with different initialization values for the AbstractLogReplayer options (realtimeMode, ....).
	@Test
	public void testIt() {

		final ListReplayer replayer = new ListReplayer(this.monitoringConfigurationFile.getAbsolutePath(),
				/* realtimeMode: */false,
				/* keepOriginalLoggingTimestamps: */true,
				/* numRealtimeWorkerThreads: */1,
				/* ignoreRecordsBeforeTimestamp */AbstractLogReplayer.MIN_TIMESTAMP,
				/* ignoreRecordsAfterTimestamp */AbstractLogReplayer.MAX_TIMESTAMP,
				this.replayList);
		Assert.assertTrue(replayer.replay());

		Assert.assertEquals("Unexpected list replayed", this.replayList, this.recordListFilledByListWriter);
	}

	@After
	public void cleanup() {
		this.tmpFolder.delete();
	}
}

/**
 * @author Andre van Hoorn
 */
class ListReplayer extends AbstractLogReplayer { // NOPMD
	private final List<IMonitoringRecord> replayList = new ArrayList<IMonitoringRecord>();

	public ListReplayer(final String monitoringConfigurationFile, final boolean realtimeMode, final boolean keepOriginalLoggingTimestamps,
			final int numRealtimeWorkerThreads, final long ignoreRecordsBeforeTimestamp, final long ignoreRecordsAfterTimestamp,
			final List<IMonitoringRecord> replayList) {
		super(monitoringConfigurationFile, realtimeMode, keepOriginalLoggingTimestamps, numRealtimeWorkerThreads, ignoreRecordsBeforeTimestamp,
				ignoreRecordsAfterTimestamp);
		this.replayList.addAll(replayList);
	}

	@Override
	protected String readerOutputPortName() {
		return ListReader.OUTPUT_PORT_NAME;
	}

	@Override
	protected AbstractReaderPlugin createReader(final IAnalysisController analysisController) {
		final Configuration listReaderConfig = new Configuration();
		listReaderConfig.setProperty(ListReader.CONFIG_PROPERTY_NAME_AWAIT_TERMINATION, Boolean.toString(Boolean.FALSE));
		final ListReader<IMonitoringRecord> listReader = new ListReader<IMonitoringRecord>(listReaderConfig, analysisController);
		listReader.addAllObjects(this.replayList);
		return listReader;
	}
}
