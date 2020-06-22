# methanol-protobuf

`BodyAdapter` implementations for Google's [Protocol Buffers][protocol_buffers] format. Any subtype
of `MessageLite` is supported for decoding and encoding.

## Installation

Add this module as a dependency:

### Gradle

```gradle
dependencies {
  implementation 'com.github.mizosoft.methanol:methanol-protobuf:1.3.0'
}
```

### Maven

```xml
<dependencies>
  <dependency>
    <groupId>com.github.mizosoft.methanol</groupId>
    <artifactId>methanol-protobuf</artifactId>
    <version>1.3.0</version>
  </dependency>
</dependencies>
```

### Registering providers

`Encoder` and `Decoder` implementations are not service-provided by default. You must add
provider declarations yourself if you intend to use them for dynamic request/response conversion.

#### Module path

Add this class to your module:

```java
public class ProtobufProviders {
  private ProtobufProviders() {}

  public static class EncoderProvider {
    private EncoderProvider() {}

    public static BodyAdapter.Encoder provider() {
      return ProtobufAdapterFactory.createEncoder();
    }
  }

  public static class DecoderProvider {
    private DecoderProvider() {}

    public static BodyAdapter.Decoder provider() {
      // May optionally provide an ExtensionRegistryLite for proto2 extensions
      ExtensionRegistryLite registry = ...
      return ProtobufAdapterFactory.createDecoder(registry);
    }
  }
}
```

Then add provider declarations in your `module-info.java`:

```java
provides BodyAdapter.Encoder with ProtobufProviders.EncoderProvider;
provides BodyAdapter.Decoder with ProtobufProviders.DecoderProvider;
```

#### Class path

If you're running from the classpath, you must instead implement delegating `Encoder` and `Decoder`
that forward to the instances created by `ProtobufAdapterFactory`. Then declare them in
`META-INF/services` entries as described in `ServiceLoader`'s [Javadoc][ServiceLoader].

## Usage

```java
// For request
MyMessage message = ...
HttpRequest request = HttpRequest.newBuilder(...)
    .POST(MoreBodyPublishers.ofObject(message, MediaType.APPLICATION_X_PROTOBUF))
     ...
    .build();

// For response
HttpResponse<MyMessage> response = client.send(request, MoreBodyHandlers.ofObject(MyMessage.class));
```

[protocol_buffers]: https://developers.google.com/protocol-buffers
[ServiceLoader]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/ServiceLoader.html
