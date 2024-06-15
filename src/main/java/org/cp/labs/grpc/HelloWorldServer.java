package org.cp.labs.grpc;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import io.codeprimate.example.app.proto.hello.HelloRequest;
import io.codeprimate.example.app.proto.hello.HelloResponse;
import io.codeprimate.example.app.proto.hello.HelloServerGrpc;
import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.stub.StreamObserver;

import org.cp.elements.lang.StringUtils;

import lombok.AccessLevel;
import lombok.Getter;

/**
 * Server implemented with {@literal gRPC} and {@literal Protobuf}.
 *
 * @author Joh Blum
 */
@Getter(AccessLevel.PROTECTED)
public class HelloWorldServer {

	public static final int PORT = 21051;

	protected static final Duration TIMEOUT = Duration.ofSeconds(15);

	public static void main(String[] args) throws Exception {
		HelloWorldServer server = new HelloWorldServer();
		server.start();
		server.block();
	}

	private final Server server;

	public HelloWorldServer() {
		this(PORT);
	}

	public HelloWorldServer(int port) {
		this.server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
			.addService(new HelloWorldService())
			.build();
	}

	public void block() throws InterruptedException {
		getServer().awaitTermination();
	}

	public void start() throws IOException {
		Server server = getServer().start();
		Runtime.getRuntime().addShutdownHook(shutdownHookThread());
		System.out.printf("gRPC Server started; listening on port [%d]%n", server.getPort());
	}

	private Thread shutdownHookThread() {
		return new Thread(shutdownHookRunnable(), "gRPC Server shutdown");
	}

	private Runnable shutdownHookRunnable() {

		return () -> {
			try {
				stop();
				System.out.println("gRPC Server shutdown");
			}
			catch (InterruptedException e) {
				System.err.printf("Failed to stop gRPC Server listening on port [%d]%n", getServer().getPort());
				Thread.currentThread().interrupt();
			}
		};
	}

	public void stop() throws InterruptedException {
		getServer().shutdown().awaitTermination(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
	}

	static class HelloWorldService extends HelloServerGrpc.HelloServerImplBase {

		static final String DEFAULT_NAME = "World";
		static final String MESSAGE_TEMPLATE = "Hello %s!";

		@Override
		public void sayHello(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
			String username = request.getName();
			HelloResponse response = HelloResponse.newBuilder().setMessage(buildMessage(username)).build();
			responseObserver.onNext(response);
			responseObserver.onCompleted();
		}

		private String buildMessage(String name) {
			String resolvedName = StringUtils.hasText(name) ? name : DEFAULT_NAME;
			return MESSAGE_TEMPLATE.formatted(resolvedName);
		}
	}
}
