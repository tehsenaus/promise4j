package com.dystudios.promise4j;

/**
 * @author Sean Micklethwaite
 *         24/07/2012 00:25
 */
public interface Promise<T,TFail,TProgress> {
	Promise<T,TFail,TProgress> then(Handler<? super T> resolveHandler);
	Promise<T,TFail,TProgress> then(Handler<? super T> resolveHandler, Handler<? super TFail> failHandler);
	Promise<T,TFail,TProgress> then(Handler<? super T> resolveHandler, Handler<? super TFail> failHandler, Handler<? super TProgress> progressHandler);
	Promise<T,TFail,TProgress> always(Runnable runnable);

	<T2> Promise<T2,TFail,TProgress> pipe(PipedHandler<? super T, T2> resolveHandler);
	<T2> Promise<T2,TFail,TProgress> pipe(PromisedHandler<? super T, T2, ? extends TFail, ? extends TProgress> resolveHandler);
	<T2,TFail2> Promise<T2,TFail2,TProgress> pipe(PipedHandler<? super T, T2> resolveHandler, PipedHandler<? super TFail, TFail2> failHandler);
	<T2,TFail2,TProgress2> Promise<T2,TFail2,TProgress2> pipe(PipedHandler<? super T, T2> resolveHandler, PipedHandler<? super TFail, TFail2> failHandler, PipedHandler<? super TProgress, TProgress2> progressHandler);
	<TFail2> Promise<T,TFail2,TProgress> pipeFail(PipedHandler<? super TFail, TFail2> failHandler);
	<TProgress2> Promise<T,TFail,TProgress2> pipeProgress(PipedHandler<? super TProgress, TProgress2> progressHandler);

	interface Handler<T> {
		void call(T value);
	}
	interface PipedHandler<T, T2> {
		T2 call(T value);
	}
	interface PromisedHandler<T, T2, TFail, TProgress> extends PipedHandler<T, Promise<T2,TFail,TProgress>> {

	}
}
