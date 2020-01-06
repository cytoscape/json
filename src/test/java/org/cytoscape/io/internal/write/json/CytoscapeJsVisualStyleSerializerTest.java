package org.cytoscape.io.internal.write.json;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyVersion;
import org.cytoscape.ding.DVisualLexicon;
import org.cytoscape.view.presentation.property.values.ObjectPosition;
import org.cytoscape.view.presentation.property.values.Justification;
import org.cytoscape.view.presentation.property.values.Position;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.dependency.EdgeColorDependencyFactory;
import org.cytoscape.ding.dependency.NodeSizeDependencyFactory;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsVisualStyleModule;
import org.cytoscape.io.internal.write.json.serializer.CytoscapeJsVisualStyleSerializer;
import org.cytoscape.io.internal.write.json.serializer.ValueSerializerManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.internal.VisualStyleFactoryImpl;
import org.cytoscape.view.vizmap.internal.mappings.ContinuousMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.DiscreteMappingFactory;
import org.cytoscape.view.vizmap.internal.mappings.PassthroughMappingFactory;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CytoscapeJsVisualStyleSerializerTest extends AbstractJsonNetworkViewWriterTest {

	private CytoscapeJsVisualStyleSerializer serializer;

	private VisualStyle style;
	private VisualLexicon lexicon;

	private PassthroughMappingFactory passthroughFactory;
	private ContinuousMappingFactory continuousFactory;
	private DiscreteMappingFactory discreteFactory;

	private CyNetworkViewManager viewManager;
	
	
	@Before
	public void setUp() throws Exception {
		super.setUp();
		MockitoAnnotations.initMocks(this);
		
		Set<CyNetworkView> views = new HashSet<>();
		views.add(view);
		viewManager = mock(CyNetworkViewManager.class);
		when(viewManager.getNetworkViewSet()).thenReturn(views);

		final CyVersion cyVersion = mock(CyVersion.class); 
		final CustomGraphicsManager cgManager = mock(CustomGraphicsManager.class);
		lexicon = new DVisualLexicon(cgManager);
		
		final ValueSerializerManager manager = new ValueSerializerManager();
		serializer = new CytoscapeJsVisualStyleSerializer(manager, lexicon, cyVersion, viewManager);

		final CyServiceRegistrar cyServiceRegistrar = mock(CyServiceRegistrar.class);
		when(cyServiceRegistrar.getService(CyEventHelper.class)).thenReturn(mock(CyEventHelper.class));
		
		
		passthroughFactory = new PassthroughMappingFactory(cyServiceRegistrar);
		discreteFactory = new DiscreteMappingFactory(cyServiceRegistrar);
		continuousFactory = new ContinuousMappingFactory(cyServiceRegistrar);
		
		style = generateVisualStyle(lexicon);
		setDefaults();
		setMappings();
		
		// Simple test to check Visual Style contents
		assertEquals("vs1", style.getTitle());
		
			
	}

	@After
	public void tearDown() throws Exception {
	}

	private final VisualStyle generateVisualStyle(final VisualLexicon lexicon) {
		final Set<VisualLexicon> lexSet = new HashSet<VisualLexicon>();
		lexSet.add(lexicon);

		final CyEventHelper eventHelper = mock(CyEventHelper.class);
		
		final CyServiceRegistrar serviceRegistrar = mock(CyServiceRegistrar.class);
		when(serviceRegistrar.getService(CyEventHelper.class)).thenReturn(eventHelper);
		
		final VisualMappingFunctionFactory ptFactory = mock(VisualMappingFunctionFactory.class);
		final VisualStyleFactoryImpl visualStyleFactory = new VisualStyleFactoryImpl(serviceRegistrar,
				ptFactory);

		return visualStyleFactory.createVisualStyle("vs1");
	}

	private final void setDefaults() {
		// Node default values
		style.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(10, 10, 200));
		style.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 200);

		style.setDefaultValue(BasicVisualLexicon.NODE_WIDTH, 40d);
		style.setDefaultValue(BasicVisualLexicon.NODE_HEIGHT, 30d);
		style.setDefaultValue(BasicVisualLexicon.NODE_SIZE, 60d);

		style.setDefaultValue(BasicVisualLexicon.NODE_SHAPE, NodeShapeVisualProperty.ROUND_RECTANGLE);

		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_PAINT, Color.BLUE);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 2d);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 150);

		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_COLOR, Color.BLUE);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 18);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_FACE, new Font("Helvetica", Font.PLAIN, 12));
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_TRANSPARENCY, 122);
		style.setDefaultValue(DVisualLexicon.NODE_LABEL_POSITION,
				new ObjectPosition(Position.NORTH_EAST, Position.CENTER, Justification.JUSTIFY_CENTER, 0,0));

		// For Selected
		style.setDefaultValue(BasicVisualLexicon.NODE_SELECTED_PAINT, Color.RED);

		// Edge default values
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, new Color(12,100,200));
		style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, new Color(222, 100, 10));

		style.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 100);

		style.setDefaultValue(BasicVisualLexicon.EDGE_LINE_TYPE, LineTypeVisualProperty.DOT);

		style.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 3d);

		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, Color.red);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_FACE, new Font("SansSerif", Font.BOLD, 12));
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, 11);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 220);

		style.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.DELTA);
		style.setDefaultValue(BasicVisualLexicon.EDGE_SOURCE_ARROW_SHAPE, ArrowShapeVisualProperty.T);
		
		style.setDefaultValue(DVisualLexicon.EDGE_TARGET_ARROW_UNSELECTED_PAINT, new Color(20, 100, 100));
		style.setDefaultValue(DVisualLexicon.EDGE_SOURCE_ARROW_UNSELECTED_PAINT, new Color(10, 100, 100));
		
		// For Selected
		style.setDefaultValue(BasicVisualLexicon.EDGE_SELECTED_PAINT, Color.PINK);
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_SELECTED_PAINT, Color.ORANGE);
	}

	private final void setMappings() {
		// Passthrough mappings
		final VisualMappingFunction<String, String> nodeLabelMapping = passthroughFactory.createVisualMappingFunction(
				CyNetwork.NAME, String.class, BasicVisualLexicon.NODE_LABEL);
		final VisualMappingFunction<String, String> edgeLabelMapping = passthroughFactory.createVisualMappingFunction(
				CyEdge.INTERACTION, String.class, BasicVisualLexicon.EDGE_LABEL);
		style.addVisualMappingFunction(nodeLabelMapping);
		style.addVisualMappingFunction(edgeLabelMapping);

		// Continuous mappings
		// Simple two points mapping.
		final ContinuousMapping<Integer, Paint> nodeLabelColorMapping = (ContinuousMapping<Integer, Paint>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_LABEL_COLOR);
		
		final ContinuousMapping<Double, Integer> nodeOpacityMapping = (ContinuousMapping<Double, Integer>) continuousFactory
				.createVisualMappingFunction("Betweenness Centrality", Double.class, BasicVisualLexicon.NODE_TRANSPARENCY);
		
		final ContinuousMapping<Integer, Double> nodeWidthMapping = (ContinuousMapping<Integer, Double>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_WIDTH);
		final ContinuousMapping<Integer, Double> nodeHeightMapping = (ContinuousMapping<Integer, Double>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_HEIGHT);
		
		// Complex multi-point mapping
		final ContinuousMapping<Integer, Paint> nodeColorMapping = (ContinuousMapping<Integer, Paint>) continuousFactory
				.createVisualMappingFunction("Degree", Integer.class, BasicVisualLexicon.NODE_FILL_COLOR);

		final BoundaryRangeValues<Paint> lc1 = new BoundaryRangeValues<Paint>(Color.black, Color.yellow, Color.green);
		final BoundaryRangeValues<Paint> lc2 = new BoundaryRangeValues<Paint>(Color.red, Color.pink, Color.blue);
		nodeLabelColorMapping.addPoint(3, lc1);
		nodeLabelColorMapping.addPoint(10, lc2);
		style.addVisualMappingFunction(nodeLabelColorMapping);
		
		final BoundaryRangeValues<Paint> color1 = new BoundaryRangeValues<Paint>(Color.black, Color.red, Color.orange);
		final BoundaryRangeValues<Paint> color2 = new BoundaryRangeValues<Paint>(Color.white, Color.white, Color.white);
		final BoundaryRangeValues<Paint> color3= new BoundaryRangeValues<Paint>(Color.green, Color.pink, Color.blue);
		
		// Shuffle insertion.
		nodeColorMapping.addPoint(2, color1);
		nodeColorMapping.addPoint(5, color2);
		nodeColorMapping.addPoint(10, color3);

		final BoundaryRangeValues<Double> bv0 = new BoundaryRangeValues<Double>(20d, 20d, 20d);
		final BoundaryRangeValues<Double> bv1 = new BoundaryRangeValues<Double>(200d, 200d, 400d);
		nodeWidthMapping.addPoint(1, bv0);
		nodeWidthMapping.addPoint(20, bv1);
		nodeHeightMapping.addPoint(1, bv0);
		nodeHeightMapping.addPoint(20, bv1);

		final BoundaryRangeValues<Integer> trans0 = new BoundaryRangeValues<Integer>(10, 10, 10);
		final BoundaryRangeValues<Integer> trans1 = new BoundaryRangeValues<Integer>(80, 80, 100);
		final BoundaryRangeValues<Integer> trans2 = new BoundaryRangeValues<Integer>(222, 222, 250);
		nodeOpacityMapping.addPoint(0.22, trans0);
		nodeOpacityMapping.addPoint(0.61, trans1);
		nodeOpacityMapping.addPoint(0.95, trans2);

		style.addVisualMappingFunction(nodeWidthMapping);
		style.addVisualMappingFunction(nodeHeightMapping);
		style.addVisualMappingFunction(nodeOpacityMapping);
		style.addVisualMappingFunction(nodeColorMapping);

		// Discrete mappings
		final DiscreteMapping<String, NodeShape> nodeShapeMapping = (DiscreteMapping<String, NodeShape>) discreteFactory
				.createVisualMappingFunction("Node Type", String.class, BasicVisualLexicon.NODE_SHAPE);
		nodeShapeMapping.putMapValue("gene", NodeShapeVisualProperty.DIAMOND);
		nodeShapeMapping.putMapValue("protein", NodeShapeVisualProperty.ELLIPSE);
		nodeShapeMapping.putMapValue("compound", NodeShapeVisualProperty.ROUND_RECTANGLE);
		nodeShapeMapping.putMapValue("pathway", NodeShapeVisualProperty.OCTAGON);

		style.addVisualMappingFunction(nodeShapeMapping);

		final DiscreteMapping<String, ObjectPosition> nodeLabelPosMapping = (DiscreteMapping<String, ObjectPosition>) discreteFactory
				.createVisualMappingFunction("Node Type", String.class, DVisualLexicon.NODE_LABEL_POSITION);
		nodeLabelPosMapping.putMapValue("gene", new ObjectPosition(Position.SOUTH, Position.NORTH_WEST, Justification.JUSTIFY_CENTER, 0,0));
		nodeLabelPosMapping.putMapValue("protein", new ObjectPosition(Position.EAST, Position.WEST, Justification.JUSTIFY_CENTER, 0,0));

		style.addVisualMappingFunction(nodeLabelPosMapping);
		
		// Add mapping from boolean value to size
		final DiscreteMapping<Boolean, Integer> nodeLabelSizeMapping = (DiscreteMapping<Boolean, Integer>) discreteFactory
				.createVisualMappingFunction("Node Type2", Boolean.class, DVisualLexicon.NODE_LABEL_FONT_SIZE);
		nodeLabelSizeMapping.putMapValue(true, 13);
		nodeLabelSizeMapping.putMapValue(false, 22);
		style.addVisualMappingFunction(nodeLabelSizeMapping);

		final DiscreteMapping<String, Paint> edgeColorMapping = (DiscreteMapping<String, Paint>) discreteFactory
				.createVisualMappingFunction("interaction", String.class,
						BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		edgeColorMapping.putMapValue("pp", Color.green);
		edgeColorMapping.putMapValue("pd", Color.red);

		style.addVisualMappingFunction(edgeColorMapping);
		
		final DiscreteMapping<String, Integer> edgeTransparencyMapping = (DiscreteMapping<String, Integer>) discreteFactory
				.createVisualMappingFunction("interaction", String.class,
						BasicVisualLexicon.EDGE_TRANSPARENCY);
		edgeTransparencyMapping.putMapValue("pp", 222);
		edgeTransparencyMapping.putMapValue("pd", 123);

		style.addVisualMappingFunction(edgeTransparencyMapping);
	}

	@Test
	public void testSerializeWithoutLock() throws Exception {
		// Unlock all
		final Set<VisualPropertyDependency<?>> locks = style.getAllVisualPropertyDependencies();
		for(VisualPropertyDependency<?> dep: locks) {
			dep.setDependency(false);
		}
		testUnlocked(writeVS("target/vs.json"));
	}

	@Test
	public void testSerializeWithLock() throws Exception {
		// Set Locks
		final NodeSizeDependencyFactory nodeSizeDefFactory = new NodeSizeDependencyFactory(lexicon);
		style.addVisualPropertyDependency(nodeSizeDefFactory.createVisualPropertyDependency());
		final EdgeColorDependencyFactory edgeColorDepFactory = new EdgeColorDependencyFactory(lexicon);
		style.addVisualPropertyDependency(edgeColorDepFactory.createVisualPropertyDependency());
		
		final Set<VisualPropertyDependency<?>> locks = style.getAllVisualPropertyDependencies();
		for(VisualPropertyDependency<?> dep: locks) {
			dep.setDependency(true);
		}
		testLocked(writeVS("target/vs-locked.json"));
	}
	
	@Test
	public void testBypass() throws Exception {
		final Set<VisualPropertyDependency<?>> locks = style.getAllVisualPropertyDependencies();
		for(VisualPropertyDependency<?> dep: locks) {
			dep.setDependency(false);
		}
		// Add bypass
		view.getNodeViews().stream()
			.forEach(view->view.setLockedValue(BasicVisualLexicon.NODE_FILL_COLOR, Color.ORANGE));

		view.getEdgeViews().stream()
			.forEach(view->view.setLockedValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.RED));
		
		// Set only one
		view.getNodeViews().iterator().next().setLockedValue(BasicVisualLexicon.NODE_WIDTH, 123.0d);
		view.getEdgeViews().iterator().next().setLockedValue(BasicVisualLexicon.EDGE_WIDTH, 20.0d);
		
		testBypassValues(writeVS("target/vs.json"));
	}

	
	private final File writeVS(final String fileName) throws Exception {
		assertNotNull(serializer);
		assertNotNull(style);

		final CyVersion cyVersion = mock(CyVersion.class); 
		TaskMonitor tm = mock(TaskMonitor.class);
		final Set<VisualStyle> styles = new HashSet<VisualStyle>();
		styles.add(style);

		final ObjectMapper jsMapper = new ObjectMapper();
		jsMapper.registerModule(new CytoscapeJsVisualStyleModule(lexicon, cyVersion, viewManager));

		File temp = new File(fileName);
		OutputStream os = new FileOutputStream(temp);
		CytoscapeJsVisualStyleWriter writer = new CytoscapeJsVisualStyleWriter(os, jsMapper, styles, lexicon);
		writer.run(tm);

		return temp;
	}
	
	private final void testUnlocked(File temp) throws Exception {
		final JsonNode rootNode = read(temp);
		testDefaultsUnlocked(rootNode);
	}

	private final void testLocked(File temp) throws Exception {
		final JsonNode rootNode = read(temp);
		testDefaultsLocked(rootNode);
	}
	
	
	private final JsonNode read(File generatedJsonFile) throws IOException {
		final FileInputStream fileInputStream = new FileInputStream(generatedJsonFile);
		final BufferedReader reader = new BufferedReader(new InputStreamReader(fileInputStream,
				EncodingUtil.getDecoder()));

		final ObjectMapper mapper = new ObjectMapper();

		final JsonNode root = mapper.readValue(reader, JsonNode.class);
		reader.close();
		return root;
	}
	
	private final void testDefaultsUnlocked(final JsonNode root) throws Exception {
		final Map<String, JsonNode> result = getNodesAndEdges(root);
		final JsonNode nodeCSS = result.get("node").get("css");
		final JsonNode edgeCSS = result.get("edge").get("css");
	
		testNodeDefaultsUnlocked(nodeCSS);
		testEdgeDefaultsUnlocked(edgeCSS);
	}


	private final void testDefaultsLocked(final JsonNode root) throws Exception {
		final Map<String, JsonNode> result = getNodesAndEdges(root);
		final Map<String, Set<JsonNode>> mappings = getMappings(root);
		
		final JsonNode nodeCSS = result.get("node").get("css");
		final JsonNode edgeCSS = result.get("edge").get("css");

		testNodeDefaultsLocked(nodeCSS);
		testEdgeDefaultsLocked(edgeCSS);
		
		testNodeMappings(mappings.get("node"));
	}
	
	private final void testNodeMappings(final Set<JsonNode> nodeMappings) {
		for(JsonNode jnode: nodeMappings) {
			final String mappingName = jnode.get("selector").asText();
			System.out.println(mappingName);
			if(mappingName.contains("Node_Type2")) {
				if(mappingName.contains("=")) {
					fail("Wrong boolean handler: = is not supported for boolean mapping.");
				}
				
			} else {
				
			}
		}
		
	}


	private final Map<String,JsonNode> getNodesAndEdges(final JsonNode root) {
		final Map<String,JsonNode> graphObjects = new HashMap<String, JsonNode>();

		// VS JSON is a array of Visual Styles
		assertTrue(root.isArray());
		assertEquals(1, root.size());
		final JsonNode style1 = root.get(0);
		assertEquals("vs1", style1.get("title").asText());
		final JsonNode styleObject = style1.get("style");
		assertNotNull(styleObject);
		
		JsonNode nodes = null;
		JsonNode edges = null;
		
		for(JsonNode jNode: styleObject) {
			final JsonNode css = jNode.get("css");
			final JsonNode selectorType = jNode.get("selector");
			System.out.println(selectorType.asText());
			if(selectorType.asText().equals("node")) {
				nodes = jNode;
			} else if(selectorType.asText().equals("edge")) {
				edges = jNode;
			}
		}
		assertNotNull(nodes);
		assertNotNull(edges);

		graphObjects.put("node", nodes);
		graphObjects.put("edge", edges);

		return graphObjects;
	}
	
	private final Map<String,Set<JsonNode>> getMappings(final JsonNode root) {
		final Map<String,Set<JsonNode>> mappings = new HashMap<String, Set<JsonNode>>();

		final JsonNode style1 = root.get(0);
		final JsonNode styleObject = style1.get("style");
		
		final Set<JsonNode> nodes = new HashSet<JsonNode>();
		final Set<JsonNode> edges = new HashSet<JsonNode>();
		
		for(JsonNode jNode: styleObject) {
			final JsonNode css = jNode.get("css");
			final JsonNode selectorType = jNode.get("selector");
			System.out.println(selectorType.asText());
			if(selectorType.asText().startsWith("node[")) {
				nodes.add(jNode);
			} else if(selectorType.asText().startsWith("edge[")) {
				edges.add(jNode);
			}
		}
		assertEquals(35, nodes.size());
		assertEquals(4, edges.size());

		mappings.put("node", nodes);
		mappings.put("edge", edges);

		return mappings;
	}
	
	private final String calcOpacity(int opacity) {
		return Double.toString(opacity/255d);
		
	}
	
	private final void testNodeDefaultsCommon(final JsonNode nodeCSS) {
		assertEquals("rgb(10,10,200)", nodeCSS.get("background-color").asText());
		assertEquals(calcOpacity(200), nodeCSS.get("background-opacity").asText());

		assertEquals("rgb(0,0,255)", nodeCSS.get("border-color").asText());
		assertEquals(calcOpacity(150), nodeCSS.get("border-opacity").asText());
		assertTrue(2d == nodeCSS.get("border-width").asDouble());
		
		// Font defaults
		//assertEquals("Helvetica-Light", nodeCSS.get("font-family").asText());
		assertEquals("18", nodeCSS.get("font-size").asText());
		assertEquals("normal", nodeCSS.get("font-weight").asText());
		assertEquals("rgb(0,0,255)", nodeCSS.get("color").asText());
		assertEquals(calcOpacity(122), nodeCSS.get("text-opacity").asText());

		assertEquals("roundrectangle", nodeCSS.get("shape").asText());
	}
	
	private void testEdgeDefaultsCommon(JsonNode edgeCSS) throws Exception {
		assertTrue(3d == edgeCSS.get("width").asDouble());
		
		//assertEquals("SansSerif", edgeCSS.get("font-family").asText());
		assertTrue(11d == edgeCSS.get("font-size").asDouble());
		assertEquals("bold", edgeCSS.get("font-weight").asText());
		assertEquals("rgb(255,0,0)", edgeCSS.get("color").asText());
		assertEquals(calcOpacity(220), edgeCSS.get("text-opacity").asText());

		assertEquals("dotted", edgeCSS.get("line-style").asText());

		assertEquals("triangle", edgeCSS.get("target-arrow-shape").asText());
		assertEquals("tee", edgeCSS.get("source-arrow-shape").asText());
	}
	
	private final void testEdgeMappingsCommon() {
	
		// Find selector:
		
	}
	
	
	private void testNodeDefaultsUnlocked(JsonNode nodeCSS) throws Exception {
		testNodeDefaultsCommon(nodeCSS);
		assertTrue(40d == nodeCSS.get("width").asDouble());
		assertTrue(30d == nodeCSS.get("height").asDouble());
	}
	
	private void testEdgeDefaultsUnlocked(JsonNode edgeCSS) throws Exception {
		testEdgeDefaultsCommon(edgeCSS);
		assertEquals("rgb(12,100,200)", edgeCSS.get("line-color").asText());
		assertEquals("rgb(10,100,100)", edgeCSS.get("source-arrow-color").asText());
		assertEquals("rgb(20,100,100)", edgeCSS.get("target-arrow-color").asText());
	}
	
	
	// Tests for Locked Style
	private void testNodeDefaultsLocked(JsonNode nodeCSS) throws Exception {
		testNodeDefaultsCommon(nodeCSS);
		assertTrue(60d == nodeCSS.get("width").asDouble());
		assertTrue(60d == nodeCSS.get("height").asDouble());
	}
	
	private void testEdgeDefaultsLocked(JsonNode edgeCSS) throws Exception {
		testEdgeDefaultsCommon(edgeCSS);
		assertEquals("rgb(222,100,10)", edgeCSS.get("line-color").asText());
		assertEquals("rgb(222,100,10)", edgeCSS.get("source-arrow-color").asText());
		assertEquals("rgb(222,100,10)", edgeCSS.get("target-arrow-color").asText());
	}
	
	private void testBypassValues(File temp) throws Exception {
		final JsonNode rootNode = read(temp);
		assertTrue(rootNode.isArray());
		assertEquals(1, rootNode.size());
		JsonNode styleNode = rootNode.get(0).get("style");
		assertNotNull(styleNode);
		
		Set<JsonNode> bypassNodes = new HashSet<>();
		for(JsonNode n: styleNode) {
			JsonNode selector = n.get("selector");
			
			String value = selector.asText();
			if(value.startsWith("node[ id =")) {
				bypassNodes.add(n);
			}
		}
		
		Set<JsonNode> bypassEdges = new HashSet<>();
		for(JsonNode n: styleNode) {
			JsonNode selector = n.get("selector");
			
			String value = selector.asText();
			if(value.startsWith("edge[ id =")) {
				bypassEdges.add(n);
			}
		}
		
		assertEquals(view.getModel().getNodeCount(), bypassNodes.size());
		
		bypassNodes.stream()
			.map(node->node.get("css"))
			.forEach(cssNode->checkNodeBypassElement(cssNode));
		
		bypassEdges.stream()
			.map(node->node.get("css"))
			.forEach(cssNode->checkEdgeBypassElement(cssNode));
		
	}
	
	private final void checkNodeBypassElement(JsonNode cssNode) {
		assertNotNull(cssNode);
		assertTrue(cssNode.isObject());
		
		if(cssNode.size() == 1) {
			JsonNode colorNode = cssNode.get("background-color");
			assertNotNull(colorNode);
			assertEquals("rgb(255,200,0)", colorNode.asText());
		} else {
			assertEquals(2, cssNode.size());
			JsonNode widthNode = cssNode.get("width");
			assertNotNull(widthNode);
			assertEquals(Double.valueOf(123), (Double)widthNode.asDouble());
		}
	}
	
	private final void checkEdgeBypassElement(JsonNode cssNode) {
		assertNotNull(cssNode);
		assertTrue(cssNode.isObject());
		
		if(cssNode.size() == 1) {
			JsonNode colorNode = cssNode.get("line-color");
			assertNotNull(colorNode);
			assertEquals("rgb(255,0,0)", colorNode.asText());
		} else {
			System.out.println(cssNode);
			assertEquals(2, cssNode.size());
			JsonNode widthNode = cssNode.get("width");
			assertNotNull(widthNode);
			assertEquals(Double.valueOf(20), (Double)widthNode.asDouble());
		}
	}
}