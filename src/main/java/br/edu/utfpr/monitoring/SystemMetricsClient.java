/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.edu.utfpr.monitoring;

import br.edu.utfpr.metrics.MetricsRequest;
import br.edu.utfpr.metrics.MetricsResponse;
import br.edu.utfpr.metrics.SystemMetricsGrpc;
import io.grpc.*;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.HardwareAbstractionLayer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * A simple client that requests a greeting from the {@link SystemMetricsServer}.
 */
public class SystemMetricsClient {

  private static final Logger logger = Logger.getLogger(SystemMetricsClient.class.getName());
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final SystemMetricsGrpc.SystemMetricsBlockingStub blockingStub;

  /** Construct client for accessing HelloWorld server using the existing channel. */
  public SystemMetricsClient(Channel channel) {
    // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
    // shut it down.

    // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
    blockingStub = SystemMetricsGrpc.newBlockingStub(channel);
  }

  /** Say hello to server. */
  public void greet() {
    // Coletar informações do sistema com OSHI
    SystemInfo systemInfo = new SystemInfo();
    HardwareAbstractionLayer hal = systemInfo.getHardware();
    CentralProcessor processor = hal.getProcessor();
    String hostName = "";
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (StatusRuntimeException | UnknownHostException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getMessage());
    }

    double cpuUsage = processor.getSystemCpuLoad(200) * 100;
    double memoryUsage = (1 - ((double) hal.getMemory().getAvailable() / hal.getMemory().getTotal())) * 100;

    MetricsRequest request = MetricsRequest.newBuilder()
            .setCpuUsage(cpuUsage)
            .setMemoryUsage(memoryUsage)
            .setHostname(hostName)
            .build();
    MetricsResponse response;
    try {
      response = blockingStub.sendMetrics(request);
    } catch (StatusRuntimeException e) {
      logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
      return;
    }
    logger.info("Greeting: " + response.getMessage());
  }

  /** Inicia o envio periódico de métricas */
  public void startSending() {
    String user = "world";
    scheduler.scheduleAtFixedRate(this::greet, 0, 15, TimeUnit.SECONDS);
  }

  /** Finaliza o cliente */
  public void shutdown() {
    logger.info("Encerrando cliente...");
    scheduler.shutdown();
  }

  /**
   * Greet server. If provided, the first element of {@code args} is the name to use in the
   * greeting. The second argument is the target server.
   */
  public static void main(String[] args) throws Exception {
    String user = "world";
    // Access a service running on the local machine on port 50051
    String target = "localhost:50051";
    // Allow passing in the user and target strings as command line arguments
    if (args.length > 0) {
      if ("--help".equals(args[0])) {
        System.err.println("Usage: [name [target]]");
        System.err.println("");
        System.err.println("  name    The name you wish to be greeted by. Defaults to " + user);
        System.err.println("  target  The server to connect to. Defaults to " + target);
        System.exit(1);
      }
      user = args[0];
    }
    if (args.length > 1) {
      target = args[1];
    }

    // Create a communication channel to the server, known as a Channel. Channels are thread-safe
    // and reusable. It is common to create channels at the beginning of your application and reuse
    // them until the application shuts down.
    //
    // For the example we use plaintext insecure credentials to avoid needing TLS certificates. To
    // use TLS, use TlsChannelCredentials instead.
    ManagedChannel channel = Grpc.newChannelBuilder(target, InsecureChannelCredentials.create())
        .build();

    SystemMetricsClient client = new SystemMetricsClient(channel);

    // Captura sinal de interrupção (Ctrl+C) e encerra corretamente
    Runtime.getRuntime().addShutdownHook(new Thread(client::shutdown));

    // Inicia o envio de métricas a cada 15 segundos
    client.startSending();

    // Mantém o programa rodando
    Thread.currentThread().join();

  }
}
