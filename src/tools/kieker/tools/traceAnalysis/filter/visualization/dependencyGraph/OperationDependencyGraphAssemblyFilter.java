/***************************************************************************
 * Copyright 2012 Kieker Project (http://kieker-monitoring.net)
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

package kieker.tools.traceAnalysis.filter.visualization.dependencyGraph;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import kieker.analysis.plugin.annotation.InputPort;
import kieker.analysis.plugin.annotation.Plugin;
import kieker.analysis.plugin.annotation.Property;
import kieker.analysis.plugin.annotation.RepositoryPort;
import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;
import kieker.common.util.Signature;
import kieker.tools.traceAnalysis.filter.AbstractMessageTraceProcessingFilter;
import kieker.tools.traceAnalysis.filter.AbstractTraceAnalysisFilter;
import kieker.tools.traceAnalysis.filter.visualization.util.dot.DotFactory;
import kieker.tools.traceAnalysis.systemModel.AbstractMessage;
import kieker.tools.traceAnalysis.systemModel.AssemblyComponent;
import kieker.tools.traceAnalysis.systemModel.MessageTrace;
import kieker.tools.traceAnalysis.systemModel.Operation;
import kieker.tools.traceAnalysis.systemModel.SynchronousReplyMessage;
import kieker.tools.traceAnalysis.systemModel.repository.AbstractSystemSubRepository;
import kieker.tools.traceAnalysis.systemModel.repository.AssemblyComponentOperationPairFactory;
import kieker.tools.traceAnalysis.systemModel.repository.AssemblyRepository;
import kieker.tools.traceAnalysis.systemModel.repository.OperationRepository;
import kieker.tools.traceAnalysis.systemModel.repository.SystemModelRepository;
import kieker.tools.traceAnalysis.systemModel.util.AssemblyComponentOperationPair;

/**
 * Refactored copy from LogAnalysis-legacy tool<br>
 * 
 * This class has exactly one input port named "in". The data which is send to this plugin is not delegated in any way.
 * 
 * @author Andre van Hoorn, Lena St&ouml;ver, Matthias Rohr,
 */
@Plugin(description = "Uses the incoming data to enrich the connected repository with data for the operation assembly dependency graph",
		repositoryPorts = @RepositoryPort(name = AbstractTraceAnalysisFilter.REPOSITORY_PORT_NAME_SYSTEM_MODEL, repositoryType = SystemModelRepository.class),
		configuration = {
			@Property(name = OperationDependencyGraphAssemblyFilter.CONFIG_PROPERTY_NAME_DOT_OUTPUT_FILE, defaultValue = OperationDependencyGraphAssemblyFilter.DEFAULT_DOT_OUTPUT_FILE),
			@Property(name = OperationDependencyGraphAssemblyFilter.CONFIG_PROPERTY_NAME_INCLUDE_WEIGHTS, defaultValue = "true"),
			@Property(name = OperationDependencyGraphAssemblyFilter.CONFIG_PROPERTY_NAME_SHORT_LABELS, defaultValue = "true"),
			@Property(name = OperationDependencyGraphAssemblyFilter.CONFIG_PROPERTY_NAME_INCLUDE_SELF_LOOPS, defaultValue = "true")
		})
public class OperationDependencyGraphAssemblyFilter extends AbstractDependencyGraphFilter<AssemblyComponentOperationPair> {

	public static final String CONFIG_PROPERTY_NAME_DOT_OUTPUT_FILE = "dotOutputFn";
	public static final String CONFIG_PROPERTY_NAME_INCLUDE_WEIGHTS = "includeWeights";
	public static final String CONFIG_PROPERTY_NAME_SHORT_LABELS = "shortLabels";
	public static final String CONFIG_PROPERTY_NAME_INCLUDE_SELF_LOOPS = "includeSelfLoops";

	/**
	 * This is the default dot output name used for the default configuration of this instance.
	 */
	protected static final String DEFAULT_DOT_OUTPUT_FILE = "output.dot";

	private static final String COMPONENT_NODE_ID_PREFIX = "component_";

	private static final Log LOG = LogFactory.getLog(OperationDependencyGraphAssemblyFilter.class);

	private final File dotOutputFile;
	private final boolean includeWeights;
	private final boolean shortLabels;
	private final boolean includeSelfLoops;

	/**
	 * Creates a new instance of this class using the given configuration.
	 * 
	 * @param configuration
	 *            The configuration used to initialize this instance.
	 */
	public OperationDependencyGraphAssemblyFilter(final Configuration configuration) {
		/* Call the mandatory "default" constructor. */
		super(configuration, new DependencyGraph<AssemblyComponentOperationPair>(AbstractSystemSubRepository.ROOT_ELEMENT_ID,
				new AssemblyComponentOperationPair(AbstractSystemSubRepository.ROOT_ELEMENT_ID, OperationRepository.ROOT_OPERATION,
						AssemblyRepository.ROOT_ASSEMBLY_COMPONENT)));

		/* Initialize from the given configuration. */
		this.dotOutputFile = new File(this.configuration.getStringProperty(CONFIG_PROPERTY_NAME_DOT_OUTPUT_FILE));
		this.includeWeights = this.configuration.getBooleanProperty(CONFIG_PROPERTY_NAME_INCLUDE_WEIGHTS);
		this.shortLabels = this.configuration.getBooleanProperty(CONFIG_PROPERTY_NAME_SHORT_LABELS);
		this.includeSelfLoops = this.configuration.getBooleanProperty(CONFIG_PROPERTY_NAME_INCLUDE_SELF_LOOPS);
	}

	private String componentNodeLabel(final AssemblyComponent component) {
		final String assemblyComponentName = component.getName();
		final String componentTypePackagePrefx = component.getType().getPackageName();
		final String componentTypeIdentifier = component.getType().getTypeName();

		final StringBuilder strBuild = new StringBuilder(AbstractDependencyGraphFilter.STEREOTYPE_ASSEMBLY_COMPONENT);
		strBuild.append("\\n");
		strBuild.append(assemblyComponentName).append(":");
		if (!this.shortLabels) {
			strBuild.append(componentTypePackagePrefx).append(".");
		} else {
			strBuild.append("..");
		}
		strBuild.append(componentTypeIdentifier);
		return strBuild.toString();
	}

	private String nodeLabel(final DependencyGraphNode<?> node, final StringBuilder labelBuilder) {
		this.addDecorationText(labelBuilder, node);
		return labelBuilder.toString();
	}

	@Override
	protected void dotEdges(final Collection<DependencyGraphNode<AssemblyComponentOperationPair>> nodes, final PrintStream ps, final boolean shortLabelsUNUSED) {

		/* Component ID x contained operations */
		final Map<Integer, Collection<DependencyGraphNode<AssemblyComponentOperationPair>>> componentId2pairMapping = new Hashtable<Integer, Collection<DependencyGraphNode<AssemblyComponentOperationPair>>>(); // NOPMD

		// Derive component / operation hierarchy
		for (final DependencyGraphNode<AssemblyComponentOperationPair> pairNode : nodes) {
			final AssemblyComponent curComponent = pairNode.getEntity().getAssemblyComponent();
			final int componentId = curComponent.getId();

			Collection<DependencyGraphNode<AssemblyComponentOperationPair>> containedPairs = componentId2pairMapping.get(componentId);
			if (containedPairs == null) {
				// component not yet registered
				containedPairs = new ArrayList<DependencyGraphNode<AssemblyComponentOperationPair>>();
				componentId2pairMapping.put(componentId, containedPairs);
			}
			containedPairs.add(pairNode);
		}

		final AssemblyComponent rootComponent = AssemblyRepository.ROOT_ASSEMBLY_COMPONENT;
		final int rootComponentId = rootComponent.getId();
		final StringBuilder strBuild = new StringBuilder();
		for (final Entry<Integer, Collection<DependencyGraphNode<AssemblyComponentOperationPair>>> componentOperationEntry : componentId2pairMapping.entrySet()) {
			final int curComponentId = componentOperationEntry.getKey();
			final AssemblyComponent curComponent = this.getSystemEntityFactory().getAssemblyFactory().lookupAssemblyComponentById(curComponentId);

			if (curComponentId == rootComponentId) {
				strBuild.append(DotFactory.createNode("", this.getNodeId(this.dependencyGraph.getRootNode()), "$", DotFactory.DOT_SHAPE_NONE, null, // style
						null, // framecolor
						null, // fillcolor
						null, // fontcolor
						DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
						null, // imagefilename
						null // misc
						));
			} else {
				strBuild.append(DotFactory.createCluster("", COMPONENT_NODE_ID_PREFIX + curComponentId,
						this.componentNodeLabel(curComponent), DotFactory.DOT_SHAPE_BOX, // shape
						DotFactory.DOT_STYLE_FILLED, // style
						null, // framecolor
						DotFactory.DOT_FILLCOLOR_WHITE, // fillcolor
						null, // fontcolor
						DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
						null)); // misc
				for (final DependencyGraphNode<AssemblyComponentOperationPair> curPair : componentOperationEntry.getValue()) {
					final Signature sig = curPair.getEntity().getOperation().getSignature();
					final StringBuilder opLabel = new StringBuilder(sig.getName());
					opLabel.append("(");
					final String[] paramList = sig.getParamTypeList();
					if (paramList.length > 0) {
						opLabel.append("..");
					}
					opLabel.append(")");

					strBuild.append(DotFactory.createNode("", this.getNodeId(curPair), this.nodeLabel(curPair, opLabel), DotFactory.DOT_SHAPE_OVAL,
							DotFactory.DOT_STYLE_FILLED, // style
							null, // framecolor
							this.getNodeFillColor(curPair), // fillcolor
							null, // fontcolor
							DotFactory.DOT_DEFAULT_FONTSIZE, // fontsize
							null, // imagefilename
							null // misc
							));
				}
				strBuild.append("}\n");
			}
		}
		ps.println(strBuild.toString());
	}

	/**
	 * Saves the dependency graph to the dot file if error is not true.
	 * 
	 * @param error
	 */

	@Override
	public void terminate(final boolean error) {
		if (!error) {
			try {
				this.saveToDotFile(this.dotOutputFile.getCanonicalPath(), this.includeWeights, this.shortLabels, this.includeSelfLoops);
			} catch (final IOException ex) {
				LOG.error("IOException while saving to dot file", ex);
			}
		}
	}

	public Configuration getCurrentConfiguration() {
		final Configuration configuration = new Configuration();

		configuration.setProperty(CONFIG_PROPERTY_NAME_DOT_OUTPUT_FILE, this.dotOutputFile.getAbsolutePath());
		configuration.setProperty(CONFIG_PROPERTY_NAME_INCLUDE_WEIGHTS, Boolean.toString(this.includeWeights));
		configuration.setProperty(CONFIG_PROPERTY_NAME_SHORT_LABELS, Boolean.toString(this.shortLabels));
		configuration.setProperty(CONFIG_PROPERTY_NAME_INCLUDE_SELF_LOOPS, Boolean.toString(this.includeSelfLoops));

		return configuration;
	}

	@Override
	@InputPort(
			name = AbstractMessageTraceProcessingFilter.INPUT_PORT_NAME_MESSAGE_TRACES,
			description = "Receives the message traces to be processed",
			eventTypes = { MessageTrace.class })
	public void inputMessageTraces(final MessageTrace t) {
		for (final AbstractMessage m : t.getSequenceAsVector()) {
			if (m instanceof SynchronousReplyMessage) {
				continue;
			}
			final AssemblyComponent senderComponent = m.getSendingExecution().getAllocationComponent().getAssemblyComponent();
			final AssemblyComponent receiverComponent = m.getReceivingExecution().getAllocationComponent().getAssemblyComponent();
			final int rootOperationId = OperationRepository.ROOT_OPERATION.getId();
			final Operation senderOperation = m.getSendingExecution().getOperation();
			final Operation receiverOperation = m.getReceivingExecution().getOperation();
			/* The following two get-calls to the factory return s.th. in either case */
			final AssemblyComponentOperationPairFactory pairFactory = this.getSystemEntityFactory().getAssemblyPairFactory();
			final AssemblyComponentOperationPair senderPair = (senderOperation.getId() == rootOperationId) ? OperationDependencyGraphAssemblyFilter.this.dependencyGraph // NOCS
					.getRootNode().getEntity()
					: pairFactory.getPairInstanceByPair(senderComponent, senderOperation);
			final AssemblyComponentOperationPair receiverPair = (receiverOperation.getId() == rootOperationId) ? OperationDependencyGraphAssemblyFilter.this.dependencyGraph // NOCS
					.getRootNode().getEntity()
					: pairFactory.getPairInstanceByPair(receiverComponent, receiverOperation);

			DependencyGraphNode<AssemblyComponentOperationPair> senderNode = OperationDependencyGraphAssemblyFilter.this.dependencyGraph.getNode(senderPair
					.getId());
			DependencyGraphNode<AssemblyComponentOperationPair> receiverNode = OperationDependencyGraphAssemblyFilter.this.dependencyGraph.getNode(receiverPair
					.getId());
			if (senderNode == null) {
				senderNode = new DependencyGraphNode<AssemblyComponentOperationPair>(senderPair.getId(), senderPair, t);

				if (m.getSendingExecution().isAssumed()) {
					senderNode.setAssumed();
				}

				OperationDependencyGraphAssemblyFilter.this.dependencyGraph.addNode(senderNode.getId(), senderNode);
			} else {
				senderNode.addOrigin(t);
			}

			if (receiverNode == null) {
				receiverNode = new DependencyGraphNode<AssemblyComponentOperationPair>(receiverPair.getId(), receiverPair, t);

				if (m.getReceivingExecution().isAssumed()) {
					receiverNode.setAssumed();
				}

				OperationDependencyGraphAssemblyFilter.this.dependencyGraph.addNode(receiverNode.getId(), receiverNode);
			} else {
				receiverNode.addOrigin(t);
			}

			final boolean assumed = this.isDependencyAssumed(senderNode, receiverNode);

			senderNode.addOutgoingDependency(receiverNode, assumed, t);
			receiverNode.addIncomingDependency(senderNode, assumed, t);

			this.invokeDecorators(m, senderNode, receiverNode);
		}
		OperationDependencyGraphAssemblyFilter.this.reportSuccess(t.getTraceId());
	}

}
