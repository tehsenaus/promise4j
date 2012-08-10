What?
=====

This is a __completely type-safe__ implementation of [Promises](http://en.wikipedia.org/wiki/Promise_%28programming%29) for Java.

It is inspired by [jQuery Deferreds](http://api.jquery.com/category/deferred-object/).

Why?
====

Promises make asynchronous programming beautiful. And I _love_ generics.

How?
====

1.	Grab the code (I might provide a .jar soon)

2.	Create a Deferred, and return a Promise:
	```java
	protected Promise<MyResult, MyError, MyNotification> doStuff() {
		final Deferred<MyResult, MyError, MyNotification> d = new Deferred<MyResult, MyError, MyNotification>();

		// do stuff

		return d;
	}
	```

3. Resolve the deferred, handle the result:
	```java
	doStuff().then(new Promise.Handler<MyResult>() {
		public void call(MyResult value) {
			// we have a result!
		}
	});


	// Meanwhile, in doStuff...
	d.resolve(new MyResult());
	```

4. Make a pipeline:
	```java
	doStuff().pipe(new Promise.PipedHandler<MyResult, MyNewResult>() {
		public MyNewResult call(MyResult value) {
			return new MyNewResult();
		}
	}).then(new Promise.Handler<MyNewResult>() {
		public void call(MyNewResult value) {
			// we have a new result!
		}
	});
	```

5. Also chain promises, pass errors and notifications!

FAQ
===

*	__Any unit tests?__
	Not yet. But I'm using this in [(sort of) production code](https://market.android.com/details?id=com.dysoft.fingerlympics).