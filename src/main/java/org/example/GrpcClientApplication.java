package org.example;

import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class GrpcClientApplication {
	public static RemoteDBGrpc.RemoteDBBlockingStub blockingStub;
	private static ManagedChannel channel;

	public static void main(String[] args) {
		SpringApplication.run(GrpcClientApplication.class, args);

		// Moved the channel setup inside the main method after SpringApplication.run
		String target = "localhost:50051"; // Default target
		// Use command line arguments to change the target if necessary
		if (args.length > 1) {
			target = args[1];
		}

		// Setup the channel and blockingStub
		channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create()).build();
		blockingStub = RemoteDBGrpc.newBlockingStub(channel);
	}

	@PreDestroy
	public void cleanup() {
		// Shutdown the channel when the application context is closed
		if (channel != null && !channel.isShutdown()) {
			try {
				channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
