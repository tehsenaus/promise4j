package com.dystudios.promise4j;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sean Micklethwaite
 *         24/07/2012 00:44
 */
public class Deferred<T,TFail,TProgress> implements Promise<T,TFail,TProgress> {

	List<Handler<? super T>> resolveHandlers = new ArrayList<Handler<? super T>>();
	List<Handler<? super TFail>> failHandlers = new ArrayList<Handler<? super TFail>>();
	List<Handler<? super TProgress>> progressHandlers = new ArrayList<Handler<? super TProgress>>();
	boolean done = false, error = false;
	T value = null;
	TFail errorValue = null;
	Promise<? extends T,? extends TFail,? extends TProgress> resolvedWith = null;

	public void resolve(T value) {
		resolve(value, false);
	}
	public void resolve(Promise<? extends T,? extends TFail,? extends TProgress> promise) {
		if (promise == null) {
			resolve(null, false);
		} else {
			if ( isDone(false) ) return;
			resolvedWith = promise.then(this.resolveHandler(true), this.failHandler(true), this.progressHandler(true));
		}
	}
	protected void resolve(T value, boolean ignoreDone) {
		if ( isDone(ignoreDone) ) return;
		for(Handler<? super T> handler : resolveHandlers) {
			handler.call(value);
		}
		done = true;
		this.value = value;
	}
	protected boolean isDone(boolean ignoreDone) {
		return ignoreDone ? done && resolvedWith != null : done || resolvedWith != null;
	}

	public void reject(TFail value) {
		reject(value, false);
	}
	protected void reject(TFail value, boolean ignoreDone) {
		if ( isDone(ignoreDone) ) return;
		for(Handler<? super TFail> handler : failHandlers) {
			handler.call(value);
		}
		done = error = true;
		this.errorValue = value;
	}
	public void notify(TProgress value) {
		notify(value, false);
	}
	protected void notify(TProgress value, boolean ignoreDone) {
		if ( isDone(ignoreDone) ) return;
		for(Handler<? super TProgress> handler : progressHandlers) {
			handler.call(value);
		}
	}

	public Promise<T,TFail,TProgress> then(Handler<? super T> resolveHandler) {
		if(resolveHandler != null) {
			if (done && !error) {
				resolveHandler.call(value);
			} else {
				resolveHandlers.add(resolveHandler);
			}
		}
		return this;
	}
	public Promise<T,TFail,TProgress> then(Handler<? super T> resolveHandler, Handler<? super TFail> failHandler) {
		if(failHandler != null) {
			if(error) {
				failHandler.call(errorValue);
			} else {
				failHandlers.add(failHandler);
			}
		}
		return this.then(resolveHandler);
	}
	public Promise<T,TFail,TProgress> then(Handler<? super T> resolveHandler, Handler<? super TFail> failHandler, Handler<? super TProgress> progressHandler) {
		if(progressHandler != null) progressHandlers.add(progressHandler);
		return this.then(resolveHandler, failHandler);
	}

	public Promise<T, TFail, TProgress> always(final Runnable runnable) {
		Handler<Object> handler = new Handler<Object>() {
			public void call(Object value) {
				runnable.run();
			}
		};
		return this.then(handler, handler);
	}

	public <T2> Promise<T2,TFail,TProgress> pipe(PipedHandler<? super T, T2> resolveHandler) {
		Deferred<T2,TFail,TProgress> deferred = new Deferred<T2, TFail, TProgress>();
		this.then(new PipedResolveHandler<T2>(resolveHandler, deferred), this.failHandler(),this.progressHandler());
		return deferred;
	}

	public <T2> Promise<T2, TFail, TProgress> pipe(PromisedHandler<? super T, T2, ? extends TFail, ? extends TProgress> resolveHandler) {
		Deferred<T2,TFail,TProgress> deferred = new Deferred<T2, TFail, TProgress>();
		this.then(new PromisedResolveHandler<T2>(resolveHandler, deferred),
				this.failHandler(),this.progressHandler());
		return deferred;
	}

	public <T2,TFail2> Promise<T2,TFail2,TProgress> pipe(PipedHandler<? super T, T2> resolveHandler, PipedHandler<? super TFail, TFail2> failHandler) {
		Deferred<T2,TFail2,TProgress> deferred = new Deferred<T2, TFail2, TProgress>();
		this.then(new PipedResolveHandler<T2>(resolveHandler, deferred),
				new PipedFailHandler<TFail2>(failHandler, deferred),this.progressHandler());
		return deferred;
	}

	public <T2,TFail2,TProgress2> Promise<T2,TFail2,TProgress2> pipe(PipedHandler<? super T, T2> resolveHandler, PipedHandler<? super TFail, TFail2> failHandler, PipedHandler<? super TProgress, TProgress2> progressHandler) {
		Deferred<T2,TFail2,TProgress2> deferred = new Deferred<T2, TFail2, TProgress2>();
		this.then(new PipedResolveHandler<T2>(resolveHandler, deferred),
				new PipedFailHandler<TFail2>(failHandler, deferred),
				new PipedProgressHandler<TProgress2>(progressHandler,deferred)
			);
		return deferred;
	}

	public <TFail2> Promise<T, TFail2, TProgress> pipeFail(PipedHandler<? super TFail, TFail2> failHandler) {
		Deferred<T,TFail2,TProgress> deferred = new Deferred<T, TFail2, TProgress>();
		this.then(this.resolveHandler(),
				new PipedFailHandler<TFail2>(failHandler, deferred), this.progressHandler());
		return deferred;
	}

	public <TProgress2> Promise<T, TFail, TProgress2> pipeProgress(PipedHandler<? super TProgress, TProgress2> progressHandler) {
		Deferred<T,TFail,TProgress2> deferred = new Deferred<T, TFail, TProgress2>();
		this.then(this.resolveHandler(), this.failHandler(),
				new PipedProgressHandler<TProgress2>(progressHandler, deferred));
		return deferred;
	}

	Handler<T> resolveHandler() {
		return new DelegateResolveHandler(this);
	}
	Handler<TFail> failHandler() {
		return new DelegateFailHandler(this);
	}
	Handler<TProgress> progressHandler() {
		return new DelegateProgressHandler(this);
	}
	Handler<T> resolveHandler(boolean resolvedWithPromise) {
		return new DelegateResolveHandler(this, resolvedWithPromise);
	}
	Handler<TFail> failHandler(boolean resolvedWithPromise) {
		return new DelegateFailHandler(this, resolvedWithPromise);
	}
	Handler<TProgress> progressHandler(boolean resolvedWithPromise) {
		return new DelegateProgressHandler(this, resolvedWithPromise);
	}

	protected class DelegateResolveHandler implements Handler<T> {
		Deferred<T, ?, ?> deferred;
		boolean resolvedWithPromise = false;

		public DelegateResolveHandler(Deferred<T, ?, ?> deferred) {
			this.deferred = deferred;
		}

		public DelegateResolveHandler(Deferred<T, ?, ?> deferred, boolean resolvedWithPromise) {
			this.deferred = deferred;
			this.resolvedWithPromise = resolvedWithPromise;
		}

		public void call(T value) {
			deferred.resolve(value, resolvedWithPromise);
		}
	}
	protected class DelegateFailHandler implements Handler<TFail> {
		Deferred<?, TFail, ?> deferred;
		boolean resolvedWithPromise = false;

		public DelegateFailHandler(Deferred<?, TFail, ?> deferred) {
			this.deferred = deferred;
		}

		public DelegateFailHandler(Deferred<?, TFail, ?> deferred, boolean resolvedWithPromise) {
			this.deferred = deferred;
			this.resolvedWithPromise = resolvedWithPromise;
		}

		public void call(TFail value) {
			deferred.reject(value, resolvedWithPromise);
		}
	}
	protected class DelegateProgressHandler implements Handler<TProgress> {
		Deferred<?, ?, TProgress> deferred;
		boolean resolvedWithPromise = false;

		public DelegateProgressHandler(Deferred<?, ?, TProgress> deferred) {
			this.deferred = deferred;
		}

		public DelegateProgressHandler(Deferred<?, ?, TProgress> deferred, boolean resolvedWithPromise) {
			this.deferred = deferred;
			this.resolvedWithPromise = resolvedWithPromise;
		}

		public void call(TProgress value) {
			deferred.notify(value, resolvedWithPromise);
		}
	}

	protected class PipedResolveHandler<T2> implements Handler<T> {
		PipedHandler<? super T,T2> handler;
		Deferred<T2,?,?> deferred;

		public PipedResolveHandler(PipedHandler<? super T, T2> handler, Deferred<T2,?,?> deferred) {
			this.handler = handler;
			this.deferred = deferred;
		}

		public void call(T value) {
			T2 nextValue = handler.call(value);
			deferred.resolve(nextValue);
		}
	}
	protected class PromisedResolveHandler<T2> implements Handler<T> {
		PromisedHandler<? super T, T2, ? extends TFail, ? extends TProgress> handler;
		Deferred<T2,TFail,TProgress> deferred;

		public PromisedResolveHandler(PromisedHandler<? super T, T2, ? extends TFail, ? extends TProgress> handler, Deferred<T2,TFail,TProgress> deferred) {
			this.handler = handler;
			this.deferred = deferred;
		}

		public void call(T value) {
			deferred.resolve(handler.call(value));
		}
	}
	protected class PipedFailHandler<TFail2> implements Handler<TFail> {
		PipedHandler<? super TFail,TFail2> handler;
		Deferred<?,TFail2,?> deferred;

		public PipedFailHandler(PipedHandler<? super TFail, TFail2> handler, Deferred<?,TFail2,?> deferred) {
			this.handler = handler;
			this.deferred = deferred;
		}

		public void call(TFail value) {
			deferred.reject(handler.call(value));
		}
	}
	protected class PipedProgressHandler<TProgress2> implements Handler<TProgress> {
		PipedHandler<? super TProgress,TProgress2> handler;
		Deferred<?,?,TProgress2> deferred;

		public PipedProgressHandler(PipedHandler<? super TProgress, TProgress2> handler, Deferred<?, ?, TProgress2> deferred) {
			this.handler = handler;
			this.deferred = deferred;
		}

		public void call(TProgress value) {
			deferred.notify(handler.call(value));
		}
	}
}
