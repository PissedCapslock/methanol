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

package com.github.mizosoft.methanol;

import static com.github.mizosoft.methanol.testutils.TestUtils.headers;
import static com.github.mizosoft.methanol.testutils.Verification.verifyThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class MutableRequestTest {
  @Test
  void settingFields() {
    var publisher = BodyPublishers.ofString("XYZ");
    var request = MutableRequest.create()
        .uri("https://example.com")
        .method("PUT", publisher)
        .header("Content-Type", "text/plain")
        .timeout(Duration.ofSeconds(20))
        .version(Version.HTTP_2)
        .expectContinue(true);
    verifyThat(request)
        .hasUri("https://example.com")
        .isPUT()
        .hasBodyPublisher(publisher)
        .hasHeadersExactly("Content-Type", "text/plain")
        .hasTimeout(Duration.ofSeconds(20))
        .hasVersion(Version.HTTP_2)
        .hasExpectContinue(true);
  }

  @Test
  void settingFieldsBeforeSnapshot() {
    var publisher = BodyPublishers.ofString("XYZ");
    var request = MutableRequest.create()
        .uri("https://example.com")
        .method("PUT", publisher)
        .header("Content-Type", "text/plain")
        .timeout(Duration.ofSeconds(20))
        .version(Version.HTTP_2)
        .expectContinue(true)
        .toImmutableRequest();
    verifyThat(request)
        .hasUri("https://example.com")
        .isPUT()
        .hasBodyPublisher(publisher)
        .hasHeadersExactly("Content-Type", "text/plain")
        .hasTimeout(Duration.ofSeconds(20))
        .hasVersion(Version.HTTP_2)
        .hasExpectContinue(true);
  }

  @Test
  void setUriFromString() {
    verifyThat(MutableRequest.create().uri("https://example.com")).hasUri("https://example.com");
    verifyThat(MutableRequest.create("https://example.com")).hasUri("https://example.com");
    verifyThat(MutableRequest.GET("https://example.com")).hasUri("https://example.com");
    verifyThat(MutableRequest.POST("https://example.com", BodyPublishers.noBody()))
        .hasUri("https://example.com");
  }

  @Test
  void mutateHeaders() {
    var request = MutableRequest.create()
        .header("Content-Length", "1")
        .header("Accept-Encoding", "gzip");
    verifyThat(request)
        .hasHeadersExactly(
            "Content-Length", "1",
            "Accept-Encoding", "gzip");

    request.removeHeader("Content-Length");
    verifyThat(request).hasHeadersExactly("Accept-Encoding", "gzip");

    request.setHeader("Accept-Encoding", "deflate");
    verifyThat(request).hasHeadersExactly("Accept-Encoding", "deflate");

    request.setHeader("Content-Length", "2");
    verifyThat(request)
        .hasHeadersExactly(
            "Accept-Encoding", "deflate",
            "Content-Length", "2");

    request.headers(
        "Content-Type", "text/plain",
        "Accept-Language", "fr-FR");
    verifyThat(request)
        .hasHeadersExactly(
            "Accept-Encoding", "deflate",
            "Content-Length", "2",
            "Content-Type", "text/plain",
            "Accept-Language", "fr-FR");

    request.removeHeaders();
    verifyThat(request).hasNoHeaders();
  }

  @Test
  void addHttpHeaders() {
    var headers = headers(
        "Accept", "text/html",
        "Cookie", "sessionid=123",
        "Cookie", "password=321");
    var request = MutableRequest.create().headers(headers);
    verifyThat(request).hasHeadersExactly(headers);

    request.header("Accept-Encoding", "gzip");
    verifyThat(request)
        .hasHeadersExactly(
            "Accept", "text/html",
            "Cookie", "sessionid=123",
            "Cookie", "password=321",
            "Accept-Encoding", "gzip");
  }

  @Test
  void copying() {
    var request = MutableRequest.create()
        .POST(BodyPublishers.ofString("something"))
        .headers(
            "Content-Length", "1",
            "Accept-Encoding", "gzip")
        .timeout(Duration.ofSeconds(20))
        .version(Version.HTTP_1_1)
        .expectContinue(true);
    verifyThat(request.copy()).isDeeplyEqualTo(request);
    verifyThat(request.copy().toImmutableRequest()).isDeeplyEqualTo(request);
    verifyThat(request.toImmutableRequest()).isDeeplyEqualTo(request);
    verifyThat(MutableRequest.copyOf(request)).isDeeplyEqualTo(request);
    verifyThat(MutableRequest.copyOf(request).toImmutableRequest()).isDeeplyEqualTo(request);
    verifyThat(MutableRequest.copyOf(request.toImmutableRequest())).isDeeplyEqualTo(request);
  }

  @Test
  void changeHeadersAfterCopy() {
    var request = MutableRequest.create().header("Content-Length", "1");
    var requestCopy = request.copy().header("Accept-Encoding", "gzip");
    verifyThat(request).hasHeadersExactly("Content-Length", "1");
    verifyThat(requestCopy)
        .hasHeadersExactly(
            "Content-Length", "1",
            "Accept-Encoding", "gzip");
  }

  @Test
  void defaultFields() {
    verifyThat(MutableRequest.create())
        .isGET()
        .hasUri("")
        .hasNoHeaders()
        .hasNoBody()
        .hasNoTimeout()
        .hasNoVersion()
        .hasExpectContinue(false);
  }

  @Test
  void applyConsumer() {
    var request = MutableRequest.create().apply(r -> r.uri("https://example.com"));
    verifyThat(request).hasUri("https://example.com");
  }

  @Test
  void testToString() {
    assertThat(MutableRequest.GET("https://example.com"))
        .hasToString("https://example.com GET")
        .extracting(MutableRequest::toImmutableRequest)
        .hasToString("https://example.com GET");
  }

  @Test
  void staticFactories() {
    var uri = URI.create("https://example.com");

    verifyThat(MutableRequest.create(uri))
        .hasUri(uri)
        .isGET()
        .hasNoBody();

    verifyThat(MutableRequest.GET(uri))
        .hasUri(uri)
        .isGET()
        .hasNoBody();

    var publisher = BodyPublishers.ofString("something");
    verifyThat(MutableRequest.POST(uri, publisher))
        .hasUri(uri)
        .isPOST()
        .hasBodyPublisher(publisher);
  }

  @Test
  void methodShortcuts() {
    var request = MutableRequest.create();
    var publisher = BodyPublishers.ofString("something");

    request.POST(publisher);
    verifyThat(request).isPOST().hasBodyPublisher(publisher);

    request.GET();
    verifyThat(request).isGET().hasNoBody();

    request.PUT(publisher);
    verifyThat(request).isPUT().hasBodyPublisher(publisher);

    request.DELETE();
    verifyThat(request).isDELETE().hasNoBody();
  }

  @Test
  void removeHeadersIf() {
    var request = MutableRequest.create()
        .headers(
            "X-My-First-Header", "val1",
            "X-My-First-Header", "val2",
            "X-My-Second-Header", "val1",
            "X-My-Second-Header", "val2");

    request.removeHeadersIf((name, __) -> "X-My-First-Header".equals(name));
    verifyThat(request)
        .hasHeadersExactly(
            "X-My-Second-Header", "val1",
            "X-My-Second-Header", "val2");

    request.removeHeadersIf(
        (name, value) -> "X-My-Second-Header".equals(name) && "val1".equals(value));
    verifyThat(request).hasHeadersExactly("X-My-Second-Header", "val2");

    request.removeHeadersIf((__, ___) -> true);
    verifyThat(request).hasNoHeaders();
  }

  @Test
  void headersWithInvalidNumberOfArguments() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MutableRequest.create().headers(new String[0]));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MutableRequest.create().headers("Content-Length", "1", "Orphan"));
  }

  @Test
  void illegalHeaders() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MutableRequest.create().header("ba\r", "foo"));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MutableRequest.create().headers("Name", "…"));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MutableRequest.create().headers(headers("ba\r..", "foo")));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MutableRequest.create().headers(headers("Name", "…")));
  }

  @Test
  void illegalTimeout() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MutableRequest.create().timeout(Duration.ofSeconds(0)));
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MutableRequest.create().timeout(Duration.ofSeconds(-1)));
  }

  @Test
  void illegalMethodName() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> MutableRequest.create().method("ba\r", BodyPublishers.noBody()));
  }
}
