# Change Log

## Version 1.5.0

*14-5-2021*

* Methanol now has an [RFC-compliant][httpcaching_rfc] HTTP cache! It can store entries on disk or
  in memory. Give it a try!
  ```java
  void cache() throws InterruptedException, IOException {
    var cache = HttpCache.newBuilder()
        .cacheOnDisk(Path.of("cache-dir"), 100 * 1024 * 1024)
        .build();
    var client = Methanol.newBuilder()
        .cache(cache)
        .build();

    var request = MutableRequest.GET("https://i.imgur.com/NYvl8Sy.mp4");
    var response = (CacheAwareResponse<Path>) client.send(
        request, BodyHandlers.ofFile(Path.of("banana_cat.mp4")));

    System.out.printf(
        "%s (CacheStatus: %s, elapsed: %s)%n",
        response,
        response.cacheStatus(),
        Duration.between(response.timeRequestSent(), response.timeResponseReceived()));

    cache.close();
  }
  ```

* Added `CacheControl` to model the `Cache-Control` header and its directives. This is complementary
  to the new cache as all configuration is communicated through `Cache-Control`.
* Interceptors have been reworked. The old naming convention is deprecated. An interceptor is now either 
  a *client* or a *backend* interceptor instead of a pre/post decoration interceptor, where 'backend' refers
  to `Methanol`'s backing `HttpClient`. The cache intercepts requests after client but before backend
  interceptors. It was tempting to name the latter 'network interceptors', but that seemed rather confusing
  as not all 'network' requests can be intercepted (`HttpClient` can make its own intermediate requests
  like redirects & retries).
* Added `HttpStats`, which contains functions for checking response codes.
* Added `ForwardingEncoder` & `ForwardingDecoder`. These are meant for easier installation of adapters
  from the classpath.
* `System.Logger` API is now used instead of `java.util.logging`.
* Fix: Don't attempt to decompress responses to HEADs. This fixed failures like `unexpected end of gzip stream`.
* Fix: Decompressed responses now have their stale `Content-Encoding` & `Content-Length` headers removed.  
* Changed reactor dependency to API scope in the `methanol-jackson-flux` adapter.
* Upgraded [Jackson to 2.12.3](https://github.com/FasterXML/jackson-databind/blob/f67f2a194651609deeffc4dba868bb767c067c57/release-notes/VERSION-2.x#L52).
* Upgraded [Reactor to 3.4.6](https://github.com/reactor/reactor-core/releases/tag/v3.4.6).
* New [project website](https://mizosoft.github.io/methanol)!

## Version 1.4.1

*26-9-2020*

* Updated dependencies.
* Fix: Autodetect if a deflated stream is zlib-wrapped or not to not crash when some servers 
  incorrectly send raw deflated bytes for the `deflate` encoding.

## Version 1.4.0

*27-7-2020*

* Multipart progress tracking.

## Version 1.3.0

*22-6-2020*

* Default read timeout in `Methanol` client.
* API for tracking upload/download progress.
* High-level client interceptors.

## Version 1.2.0

*1-5-2020*

* Reactive JSON adapters with Jackson and Reactor.
* Common `MediaType` constants.
* XML adapters with JAXB.

## Version 1.1.0

*17-4-2020* 

* First "main-stream" release.

## Version 1.0.0

*25-3-2020*

* Dummy release.

[httpcaching_rfc]: https://datatracker.ietf.org/doc/html/rfc7234
