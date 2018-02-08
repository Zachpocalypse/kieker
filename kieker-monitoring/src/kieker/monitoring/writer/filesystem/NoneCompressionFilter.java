/***************************************************************************
 * Copyright 2018 Kieker Project (http://kieker-monitoring.net)
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
package kieker.monitoring.writer.filesystem;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import kieker.common.util.filesystem.FSUtil;

/**
 * This class does not provide any compressing filter. It exists only to minimize implementation complexity.
 *
 * @author Reiner Jung
 *
 * @since 1.14
 */
public class NoneCompressionFilter implements ICompressionFilter {

	public NoneCompressionFilter() {
		// Empty constructor. No initialization necessary.
	}

	@Override
	public OutputStream chainOutputStream(final OutputStream outputStream, final Path fileName) throws IOException {
		return outputStream;

	}

	@Override
	public String getExtension() {
		return FSUtil.BINARY_FILE_EXTENSION;
	}

}
