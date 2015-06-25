package org.eclipse.heap3d.debug;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.heap3d.graph.structures.NodeManager;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;

public class DebugEventHandler implements IDebugEventSetListener {

  private IDebugTarget currentDebugTarget;
  private NodeManager nodeManager;

  public DebugEventHandler(NodeManager nodeManager) {
    this.nodeManager = nodeManager;
    DebugPlugin.getDefault().addDebugEventListener(this);
  }
  
  @Override
  public void handleDebugEvents(DebugEvent[] events) {
    for (DebugEvent event : events) {
      try {
        handleEvent(event);
      } catch (DebugException e) {
      }
    }
  }

  private void handleEvent(DebugEvent event) throws DebugException {
    if (event.isEvaluation())
      return;
    if (event.getKind() == DebugEvent.RESUME) {
      handleResumeEvent();
      return;
    }

    Object sourceObject = event.getSource();

    if (sourceObject instanceof IDebugTarget) {
      handleDebugTarget(event, (IDebugTarget) sourceObject);
    } else if (sourceObject instanceof IJavaThread) {
      handleJavaThread(event, sourceObject);
    }
  }

  private void handleJavaThread(DebugEvent event, Object sourceObject) throws DebugException {
    IJavaThread javaThread = (IJavaThread) sourceObject;
    if (javaThread.getDebugTarget() != currentDebugTarget)
      return;

    if (!javaThread.hasStackFrames() || !javaThread.isSuspended() || javaThread.isTerminated())
      return;

    IJavaStackFrame javaStackFrame = (IJavaStackFrame) javaThread
        .getTopStackFrame();

    nodeManager.resetWorkerAndRun(() -> {
      try {
        nodeManager.notifyOfNewStack(javaStackFrame);
      } catch (DebugException e) {
      }
    });
  }

  private void handleDebugTarget(DebugEvent event, IDebugTarget debugTarget) {
    if (event.getKind() == DebugEvent.CREATE && currentDebugTarget == null) {
      currentDebugTarget = debugTarget;
    } else if (event.getKind() == DebugEvent.TERMINATE
        && currentDebugTarget == debugTarget) {
      currentDebugTarget = null;
      nodeManager.resetWorkerAndRun(() -> {
        nodeManager.notifyOfTermination();
      });
    }
  }

  private void handleResumeEvent() {
    nodeManager.resetWorkerAndRun(() -> {
      nodeManager.notifyOfTermination();
    });
  }
}
