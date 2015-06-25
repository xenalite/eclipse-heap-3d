package org.eclipse.heap3d.graph.structures;

public class HeapGraph {
  //
  // private List<HeapGraphLevel> _levels = new ArrayList<>();
  // private List<HeapGraphLevel> _oldLevels = new ArrayList<>();
  //
  // private List<StaticHeapGraphLevel> _staticLevels = new ArrayList<>();
  // private List<StaticHeapGraphLevel> _staticOldLevels = new ArrayList<>();
  //
  // private GraphImpl<Node, HeapEdge> interLevelGraph = new GraphImpl<>();
  //
  // private Collection<StackNode> _stackNodes = new ArrayList<>();
  // private Collection<StackNode> _nodesToBe = null;
  //
  // private Map<Node, Shape> nodeToShape = new HashMap<>();
  //
  // private IAnimation animation = new NullAnimation();
  //
  // private final IRenderEngine _renderEngine;
  // private final Lock LOCK = new ReentrantReadWriteLock().writeLock();
  //
  // public HeapGraph(IRenderEngine renderEngine) {
  // _renderEngine = renderEngine;
  // }
  //
  // private void resetStack() {
  // int currentLevel = 0;
  // int currentStaticLevel = 0;
  //
  // _renderEngine.removeText();
  //
  // //Clear everything
  // _renderEngine.clear3DSpace();
  // nodeToShape.clear();
  //
  // _oldLevels = _levels;
  // _levels = new ArrayList<>();
  //
  // _staticOldLevels = _staticLevels;
  // _staticLevels = new ArrayList<>();
  //
  // interLevelGraph = new GraphImpl<>();
  //
  // for (StackNode stackNode : _stackNodes) {
  // if (!(stackNode instanceof StaticNode)) {
  // if (currentLevel < _oldLevels.size()) {
  // updateLevel(stackNode, currentLevel);
  // } else {
  // addLevel(stackNode, currentLevel);
  // }
  // ++currentLevel;
  // } else {
  // if (currentStaticLevel < _staticLevels.size()) {
  // updateLevel(stackNode, currentStaticLevel);
  // } else {
  // addLevel(stackNode, currentStaticLevel);
  // }
  // ++currentStaticLevel;
  // }
  // }
  // }
  //
  // int count = 0;
  //
  // public void inLoop() {
  //
  // if (animation.executeStepAndCheckIfDone()) {
  // buildEdges();
  // Set<Entry<Node, Shape>> nodes = nodeToShape.entrySet();
  // for(Entry<Node, Shape> e : nodes){
  // Node n = e.getKey();
  // Shape s = e.getValue();
  // float[] pos = s.getPosition();
  // float[] rot = s.getRotation();
  //
  // if (n instanceof StackNode) {
  // StackNode sn = (StackNode)n;
  // if(sn.getReferences()!= null && sn.getReferences().isEmpty()) {
  // _renderEngine.printTo3DSpace(pos[0], pos[1], pos[2]+3,
  // rot[0], rot[1], rot[2], 0.2f,
  // ((StackNode) n).getName());
  // }
  // }
  // }
  // animation = new NullAnimation();
  // }else if(animation instanceof Animation){
  // if(((Animation) animation).hasMoveEvents()){
  // _renderEngine.removeText();
  // buildEdges();
  // }
  // }
  //
  // boolean newNodes = receiveNodes();
  // if (newNodes) {
  // Set<Entry<Node, Shape>> oldNodes = new HashSet<>(nodeToShape.entrySet());
  // resetStack();
  // animation.finalise();
  // animation = new Animation(oldNodes, nodeToShape.entrySet());
  // if (!animation.hasAnythingChanged()) {
  // animation = new NothingChangedAnimation();
  // }
  // }
  //
  // }
  //
  // private void updatePosition(Node node) {
  // if (!nodeToShape.containsKey(node))
  // throw new IllegalStateException("updatePosition");
  //
  // HeapGraphLevel level = node.getLevel();
  // Shape s = nodeToShape.get(node);
  // Vector3f position = level.getPosition(node);
  // s.setPosition(position.x, position.y, position.z);
  // }
  //
  // private void updateLevel(StackNode stackNode, int level) {
  // NodesComparator comparator = new NodesComparator();
  //
  // HeapGraphLevel oldLevel;
  //
  // if (!(stackNode instanceof StaticNode)) {
  // oldLevel = _oldLevels.get(level);
  // } else {
  // oldLevel = _staticOldLevels.get(level);
  // }
  //
  // StackNode oldStackNode = oldLevel.getRoot();
  //
  // if (oldStackNode.equals(stackNode)) {
  //
  // HeapGraphLevel levelGraph;
  //
  // if (!(stackNode instanceof StaticNode)) {
  // levelGraph = new HeapGraphLevel(level);
  // _levels.add(levelGraph.getId(), levelGraph);
  // } else {
  // levelGraph = new StaticHeapGraphLevel(level);
  // _staticLevels.add(levelGraph.getId(), (StaticHeapGraphLevel) levelGraph);
  // }
  //
  // if (!stackNode.hasReference() && levelGraph.addVertex(stackNode))
  // initialiseNewShape(stackNode);
  // else
  // buildGraph(stackNode, levelGraph);
  // //copy positions
  // boolean containsAll = true;
  // for (Node n : levelGraph.getVertices()){
  // if (oldLevel.containsVertex(n)) {
  // Point2D transform = oldLevel.getLayout().getRawPosition(n);
  // levelGraph.getLayout().setRawPosition(n, transform);
  // } else {
  // containsAll = false;
  // }
  // }
  // //Check if the levels are equal in every way
  // if (containsAll) {
  // //equal so only need to update the layout
  // levelGraph.updateLayout();
  // } else {
  // //Not equal so run the layout
  // levelGraph.runLayout();
  // }
  // updatePositions(levelGraph);
  //
  // } else {
  // addLevel(stackNode, level);
  // }
  // }
  //
  //
  // //region add
  // private void addLevel(StackNode stackNode, int level) {
  // HeapGraphLevel levelGraph;
  //
  // if (!(stackNode instanceof StaticNode)) {
  // levelGraph = new HeapGraphLevel(level);
  // _levels.add(levelGraph.getId(), levelGraph);
  // } else {
  // levelGraph = new StaticHeapGraphLevel(level);
  // _staticLevels.add(levelGraph.getId(), (StaticHeapGraphLevel) levelGraph);
  // }
  //
  // if (!stackNode.hasReference() && levelGraph.addVertex(stackNode)) {
  // initialiseNewShape(stackNode);
  // } else {
  // buildGraph(stackNode, levelGraph);
  // }
  //
  // levelGraph.runLayout();
  // updatePositions(levelGraph);
  // }
  //
  // private void buildEdges() {
  // iterateThroughLevels(_levels);
  // iterateThroughLevels(_staticLevels);
  // }
  //
  // private void iterateThroughLevels(List<? extends HeapGraphLevel> levels)
  // {
  // for (HeapGraphLevel levelGraph : levels) {
  // for (Node n : levelGraph.getVertices()) {
  // Collection<HeapEdge> outEdges =
  // levelGraph.getLayout().getGraph().getOutEdges(n);
  // for (HeapEdge edge : outEdges) {
  // Node child = levelGraph.getLayout().getGraph().getOpposite(n, edge);
  // edge.connect(nodeToShape.get(n), nodeToShape.get(child), Colour.WHITE,
  // _renderEngine);
  // }
  // Collection<HeapEdge> edges = interLevelGraph.getOutEdges(n);
  // if (edges != null) {
  // for (HeapEdge edge : edges) {
  // Node from = interLevelGraph.getSource(edge);
  // Node to = interLevelGraph.getDest(edge);
  // edge.connect(nodeToShape.get(from), nodeToShape.get(to), Colour.WHITE,
  // _renderEngine);
  // }
  // }
  // }
  // }
  // }
  //
  // private void buildGraph(StackNode root, HeapGraphLevel level) {
  // buildNodes(root, level);
  //
  // for (Node node : level.getVertices()) {
  // node.getReferences().stream()
  // .filter(child -> child.first.getLevel() == node.getLevel())
  // .forEach(child -> level.addEdge(new HeapEdge(child.second), node,
  // child.first));
  // }
  // }
  //
  // private void initialiseNewShape(Node node) {
  // if (nodeToShape.containsKey(node))
  // throw new IllegalStateException("initialiseNewShape");
  //
  // Shape s = node.createShape();
  // nodeToShape.put(node, s);
  // _renderEngine.addTo3DSpace(s);
  // }
  //
  // private void buildNodes(Node node, HeapGraphLevel level) {
  // if (nodeToShape.containsKey(node) || !level.addVertex(node))
  // throw new IllegalStateException("buildNodes");
  //
  // initialiseNewShape(node);
  // updatePosition(node);
  //
  // for (Pair<Node, String> child : node.getReferences()) {
  // if (nodeToShape.containsKey(child.first))
  // interLevelGraph.addEdge(new HeapEdge(child.second), node, child.first);
  // else
  // buildNodes(child.first, level);
  // }
  // }
  // //endregion
  //
  // //region remove
  // private List<HeapGraphLevel> removeLevel(HeapGraphLevel levelGraph) {
  // List<HeapGraphLevel> levels = new ArrayList<>();
  // for (Node node : levelGraph.getVertices()) {
  // removeLinesFrom3DSpace(node.getLevel().getOutEdges(node));
  // removeLinesFrom3DSpace(interLevelGraph.getOutEdges(node));
  // removeLinesFrom3DSpace(interLevelGraph.getInEdges(node));
  //
  // Collection<HeapEdge> inEdges = interLevelGraph.getInEdges(node);
  // if (!isEmpty(inEdges))
  // for(HeapEdge edge : inEdges)
  // levels.add(interLevelGraph.getOpposite(node, edge).getLevel());
  // removeNodeFromGraphAnd3DSpace(node);
  // }
  // _levels.remove(levelGraph.getId());
  // _staticLevels.remove(levelGraph.getId());
  // return levels;
  // }
  //
  // private void removeNodeFromGraphAnd3DSpace(Node node) {
  // Shape s = nodeToShape.get(node);
  // _renderEngine.removeFrom3DSpace(s);
  // interLevelGraph.removeVertex(node);
  // nodeToShape.remove(node);
  // }
  //
  // private <T> boolean isEmpty(Collection<T> collection) {
  // return collection == null || collection.isEmpty();
  // }
  //
  // private void updatePositions(HeapGraphLevel level) {
  // level.getPositionsToUpdate().forEach(this::updatePosition);
  // }
  //
  // private void removeLinesFrom3DSpace(Collection<HeapEdge> edges) {
  // if (edges != null)
  // for (HeapEdge edge : edges){
  //
  // List<Line> lines = edge.getLines();
  // if(lines != null) {
  // for (Line l : lines)
  // _renderEngine.removeFrom3DSpace(l);
  // } else
  // {
  // // The lines weren't drawn -> usually means program stepped before
  // finishing the last animations
  // }
  // _renderEngine.removeFrom3DSpace(edge.getArrow());
  // }
  // }
  // //endregion
  //
  // public Set<Entry<Node, Shape>> getCurrentNodes() {
  // return nodeToShape.entrySet();
  // }
  //
  // public void giveNodes(Collection<StackNode> nodesToBe) {
  // LOCK.lock();
  // _nodesToBe = nodesToBe;
  // LOCK.unlock();
  // }
  //
  // private boolean receiveNodes() {
  // boolean receivedNewNodes;
  // LOCK.lock();
  // receivedNewNodes = _nodesToBe != null;
  // if (receivedNewNodes)
  // _stackNodes = _nodesToBe;
  // _nodesToBe = null;
  // LOCK.unlock();
  //
  // return receivedNewNodes;
  // }
}
