package org.amino.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.internal.runners.CompositeRunner;
import org.junit.internal.runners.MethodValidator;
import org.junit.internal.runners.TestClassMethodsRunner;
import org.junit.internal.runners.TestClassRunner;
import org.junit.internal.runners.TestIntrospector;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/**
 * @author Zhi Gan
 * 
 */
public class Parallelized extends TestClassRunner {
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface Threaded {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface InitFor {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public static @interface CheckFor {
		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public static @interface ParallelSetting {
		int[] threadNumber();
	}

	private static class ThreadedClassMethodsRunner extends
			TestClassMethodsRunner {
		private final List<Method> fThreadedMethods;
		private final List<Method> fInitForMethods;
		private final List<Method> fCheckForMethods;
		private List<Method> fTestMethods;
		private int threadNum;
		private final boolean fRunTest; // if we need to run @Test methods?

		private ThreadedClassMethodsRunner(Class<?> klass, int threadNum,
				boolean runTest) {
			super(klass);
			fRunTest = runTest;
			if (fRunTest)
				fTestMethods = new TestIntrospector(getTestClass())
						.getTestMethods(Test.class);
			else
				fTestMethods = null;

			fThreadedMethods = new TestIntrospector(getTestClass())
					.getTestMethods(Threaded.class);

			fInitForMethods = new TestIntrospector(getTestClass())
					.getTestMethods(InitFor.class);

			fCheckForMethods = new TestIntrospector(getTestClass())
					.getTestMethods(CheckFor.class);
			this.threadNum = threadNum;
		}

		@Override
		public Description getDescription() {
			Description spec;
			if (!fRunTest)
				spec = Description.createSuiteDescription(getName());
			else
				spec = super.getDescription();
			List<Method> testMethods = fThreadedMethods;
			for (Method method : testMethods)
				spec.addChild(methodDescription(method));
			return spec;
		}

		@Override
		public void run(RunNotifier notifier) {
			if ((fTestMethods == null || fTestMethods.isEmpty())
					&& fThreadedMethods.isEmpty())
				notifier.fireTestFailure(new Failure(getDescription(),
						new Exception("No runnable methods")));
			try {
				if (fRunTest)
					for (Method method : fTestMethods)
						invokeTestMethod(method, notifier);
				for (Method md : fThreadedMethods) {
					invokeThreadedMethod(md, notifier);
				}
			} catch (Exception e) {
				notifier.fireTestFailure(new Failure(Description
						.createTestDescription(getTestClass(), "failed"), e));
				return;
			}
		}

		protected void invokeThreadedMethod(Method method, RunNotifier notifier) {
			Object test;
			try {
				test = createTest();
			} catch (InvocationTargetException e) {
				notifier.fireTestFailure(new Failure(methodDescription(method),
						e.getCause()));
				return;
			} catch (Exception e) {
				notifier.fireTestFailure(new Failure(methodDescription(method),
						e.getCause()));
				return;
			}
			createThreadMethodRunner(test, method, checkFor(method),
					initFor(method), notifier).run();
		}

		protected ThreadedMethodRunner createThreadMethodRunner(Object test,
				Method method, Method checkfor, Method initFor,
				RunNotifier notifier) {
			return new ThreadedMethodRunner(test, method, checkfor, initFor,
					notifier, methodDescription(method), threadNum);
		}

		private Method initFor(Method method) {
			String name = method.getName();
			for (Method check : fInitForMethods) {
				InitFor annotation = check.getAnnotation(InitFor.class);
				String value = annotation.value();
				if (value.equals(name))
					return check;
			}
			return null;
		}

		/**
		 * @param md
		 * @return
		 */
		private Method checkFor(Method md) {
			String name = md.getName();
			for (Method check : fCheckForMethods) {
				CheckFor annotation = check.getAnnotation(CheckFor.class);
				String value = annotation.value();
				if (value.equals(name))
					return check;
			}
			return null;
		}

		@Override
		protected String getName() {
			return String.format("%s{%d}", getTestClass().getName(), threadNum);
		}

		@Override
		protected String testName(final Method method) {
			Threaded annotation = method.getAnnotation(Threaded.class);
			if (annotation != null)
				return String.format("%s{%d}", method.getName(), threadNum);
			else
				return method.getName();
		}
	}

	static protected class MultipleRunner extends CompositeRunner {
		protected int[] threadNumber;

		public MultipleRunner(final Class<?> klass) {
			super(klass.getName());

			List<Integer> AMINO_NTHREAD = null;
			try {
				AMINO_NTHREAD = new ArrayList<Integer>();
				String thread_str = System.getenv("AMINO_THREADS");
				if (thread_str != null) {
					String[] threads = thread_str.split(", *");
					for (int i = 0; i < threads.length; i++) {
						AMINO_NTHREAD.add(Integer.valueOf(threads[i]));
					}
				}
			} catch (NumberFormatException e) {

			} finally {
				if (AMINO_NTHREAD == null || AMINO_NTHREAD.size() == 0) {
					ParallelSetting setting = klass
							.getAnnotation(ParallelSetting.class);
					if (setting != null) {
						threadNumber = setting.threadNumber();
					} else {
						threadNumber = new int[] { 4 };
					}
				} else {
					threadNumber = new int[AMINO_NTHREAD.size()];
					for (int i = 0; i < AMINO_NTHREAD.size(); i++) {
						threadNumber[i] = AMINO_NTHREAD.get(i).intValue();
					}
				}
			}

			for (int i = 0; i < threadNumber.length; i++) {
				super.add(new ThreadedClassMethodsRunner(klass,
						threadNumber[i], i == 0)); // here i==0 means we only
				// run @Test methods once
			}
		}

	}

	public Parallelized(final Class<?> klass) throws Exception {
		super(klass, new MultipleRunner(klass));
	}

	@Override
	protected void validate(MethodValidator methodValidator) {
		methodValidator.validateStaticMethods();
		methodValidator.validateInstanceMethods();
	}
}
