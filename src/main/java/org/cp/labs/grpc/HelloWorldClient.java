package org.cp.labs.grpc;

import java.time.Duration;

import io.codeprimate.example.app.proto.hello.HelloRequest;
import io.codeprimate.example.app.proto.hello.HelloResponse;
import io.codeprimate.example.app.proto.hello.HelloServerGrpc;
import io.grpc.Channel;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Client implemented with {@literal gRPC} and {@literal Protobuf}.
 *
 * @author Joh Blum
 */
@Getter(AccessLevel.PROTECTED)
public class HelloWorldClient {

	protected static final String HOST_PORT = "localhost:%d".formatted(HelloWorldServer.PORT);

	protected static final Duration TIMEOUT = Duration.ofSeconds(5);

	public static void main(String[] args) {
		String name = args.length > 0 ? args[0] : "";
		new HelloWorldClient().sayHello(name);
	}

	private final HelloServerGrpc.HelloServerBlockingStub serverProxy;

	public HelloWorldClient() {
		Channel channel = Grpc.newChannelBuilder(HOST_PORT, InsecureChannelCredentials.create()).build();
		this.serverProxy = HelloServerGrpc.newBlockingStub(channel);
	}

	public void sayHello(String name) {
		HelloRequest request = newRequest(name);
		HelloResponse response = getServerProxy().sayHello(request);
		System.out.println(response.getMessage());
	}

	private HelloRequest newRequest(String name) {
		return HelloRequest.newBuilder().setName(name).build();
	}
}
