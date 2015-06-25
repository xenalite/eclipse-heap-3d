package org.eclipse.heap3d.utils;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.eval.EvaluationManager;
import org.eclipse.jdt.debug.eval.IEvaluationEngine;
import org.eclipse.jdt.debug.eval.IEvaluationListener;
import org.eclipse.jdt.debug.eval.IEvaluationResult;

public class Utils {

  public static <A, B> Entry<A, B> createPair(A first, B second) {
    return new AbstractMap.SimpleImmutableEntry<>(first, second);
  }

  public static void debugSleep(long timeMillis) {
    try {
      Thread.sleep(timeMillis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  public static void hackTooltipTiming() {
    try {
      Field fieldBehavior = Tooltip.class.getDeclaredField("BEHAVIOR");
      fieldBehavior.setAccessible(true);
      Object objBehavior = fieldBehavior.get(null);

      Field fieldActivationTimer = objBehavior.getClass().getDeclaredField("activationTimer");
      fieldActivationTimer.setAccessible(true);
      Timeline activationTimer = (Timeline) fieldActivationTimer.get(objBehavior);

      activationTimer.getKeyFrames().clear();
      activationTimer.getKeyFrames().add(new KeyFrame(new Duration(1)));

      Field fieldHideTimer = objBehavior.getClass().getDeclaredField("hideTimer");
      fieldHideTimer.setAccessible(true);
      Timeline hideTimer = (Timeline) fieldActivationTimer.get(objBehavior);

      hideTimer.getKeyFrames().clear();
      hideTimer.getKeyFrames().add(new KeyFrame(new Duration(1)));

      Field fieldleftTimer = objBehavior.getClass().getDeclaredField("leftTimer");
      fieldleftTimer.setAccessible(true);
      Timeline leftTimer = (Timeline) fieldActivationTimer.get(objBehavior);
      leftTimer.getKeyFrames().clear();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static IJavaElement getJavaElement(IStackFrame stackFrame) {
    // Get the corresponding element.
    ILaunch launch = stackFrame.getLaunch();
    if (launch == null) {
      return null;
    }
    ISourceLocator locator = launch.getSourceLocator();
    if (locator == null)
      return null;

    Object sourceElement = locator.getSourceElement(stackFrame);
    if (sourceElement instanceof IJavaElement) {
      return (IJavaElement) sourceElement;
    } else if (sourceElement instanceof IResource) {
      IJavaProject project = JavaCore.create(((IResource) sourceElement).getProject());
      if (project.exists()) {
        return project;
      }
    }
    return null;
  }
  
  private static class Listener implements IEvaluationListener, Callable<IEvaluationResult> {
    private final Object lockObject = new Object();
    private IEvaluationResult[] result = new IEvaluationResult[1];
    
    @Override
    public IEvaluationResult call() throws Exception {
      synchronized (lockObject) {
        while(result[0] == null)
          lockObject.wait();
        return result[0];
      }
    }

    @Override
    public void evaluationComplete(IEvaluationResult result) {
      synchronized (lockObject) {
        this.result[0] = result;
        lockObject.notifyAll();
      }
    }
  }
  
  public static IJavaValue evaluateExpression(IJavaStackFrame stackFrame, IJavaObject object) {
    if (stackFrame.isSuspended()) {
      IJavaElement javaElement = Utils.getJavaElement(stackFrame);
      if (javaElement != null) {
        IJavaProject project = javaElement.getJavaProject();
        IEvaluationEngine engine = null;
        try {
          String expression = "if(this instanceof java.util.Collection) return this.toArray();";

          engine = EvaluationManager.newAstEvaluationEngine(project, (IJavaDebugTarget) stackFrame.getDebugTarget());

          ExecutorService es = Executors.newFixedThreadPool(1);
          Listener l = new Listener();
          Future<IEvaluationResult> value = es.submit(l);
          
          if (object == null) {
            engine.evaluate(expression, stackFrame, l, DebugEvent.EVALUATION, false);
          } else {
            engine.evaluate(expression, object, (IJavaThread) stackFrame.getThread(), l, DebugEvent.EVALUATION, false);
          }
          return value.get().getValue();
          
        } catch (CoreException e) {
          e.printStackTrace();
        } catch (InterruptedException e) {} 
        catch (ExecutionException e) {
        }
      }
    }
    return null;
  }
}
