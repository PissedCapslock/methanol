/*
 * Copyright (c) 2019, 2020 Moataz Abdelnasser
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.mizosoft.methanol.testutils;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Publisher;

/** Collects BodyPublisher's content. */
public class BodyCollector implements Flow.Subscriber<ByteBuffer> {
  private final CompletableFuture<ByteBuffer> future;
  private final List<ByteBuffer> buffers;

  public BodyCollector() {
    future = new CompletableFuture<>();
    buffers = new ArrayList<>();
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    requireNonNull(subscription);
    subscription.request(Long.MAX_VALUE);
  }

  @Override
  public void onNext(ByteBuffer item) {
    requireNonNull(item);
    buffers.add(item);
  }

  @Override
  public void onError(Throwable throwable) {
    requireNonNull(throwable);
    future.completeExceptionally(throwable);
  }

  @Override
  public void onComplete() {
    future.complete(collect(buffers));
  }

  public CompletableFuture<ByteBuffer> future() {
    return future;
  }

  public static ByteBuffer collect(List<ByteBuffer> buffers) {
    var compacted = ByteBuffer.allocate(buffers.stream().mapToInt(ByteBuffer::remaining).sum());
    buffers.forEach(compacted::put);
    return compacted.flip();
  }

  public static ByteBuffer collect(Publisher<ByteBuffer> publisher) {
    var collector = new BodyCollector();
    publisher.subscribe(collector);
    return collector.future().join();
  }

  public static CompletableFuture<ByteBuffer> collectAsync(Publisher<ByteBuffer> publisher) {
    var collector = new BodyCollector();
    publisher.subscribe(collector);
    return collector.future();
  }

  public static CompletableFuture<String> collectStringAsync(
      Publisher<ByteBuffer> publisher, Charset charset) {
    return collectAsync(publisher).thenApply(bytes -> charset.decode(bytes).toString());
  }

  public static String collectAscii(Publisher<ByteBuffer> publisher) {
    return collectString(publisher, US_ASCII);
  }

  public static String collectUtf8(Publisher<ByteBuffer> publisher) {
    return collectString(publisher, UTF_8);
  }

  public static String collectString(Publisher<ByteBuffer> publisher, Charset charset) {
    return charset.decode(collect(publisher)).toString();
  }
}
