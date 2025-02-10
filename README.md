# System Metrics gRPC Service

Este projeto consiste em um servidor e um cliente gRPC para monitoramento de métricas do sistema, incluindo uso de CPU e memória.

## Tecnologias Utilizadas
- **Java**
- **gRPC**
- **OSHI (Operating System and Hardware Information)**
- **Gradle**

## Estrutura do Projeto

O projeto contém dois arquivos principais:

1. **SystemMetricsServer.java** - Implementa um servidor gRPC que recebe métricas do sistema enviadas por clientes e responde com uma mensagem de confirmação.
2. **SystemMetricsClient.java** - Implementa um cliente gRPC que coleta métricas do sistema localmente e as envia periodicamente ao servidor.

## Como Funciona

### Servidor (`SystemMetricsServer`)
- Inicializa um servidor gRPC escutando na porta `50051`.
- Implementa o serviço `SystemMetricsGrpc.SystemMetricsImplBase`.
- Processa requisições `sendMetrics`, respondendo com uma mensagem contendo os valores recebidos.
- Registra logs para indicar o recebimento das métricas.
- Implementa um mecanismo de desligamento seguro ao encerrar a JVM.

### Cliente (`SystemMetricsClient`)
- Conecta-se ao servidor gRPC em `localhost:50051`.
- Coleta dados de CPU e memória utilizando a biblioteca **OSHI**.
- Envia as métricas coletadas ao servidor a cada 15 segundos.
- Registra logs com as respostas do servidor.
- Implementa um mecanismo de desligamento seguro ao encerrar a JVM.

## Como Executar

### Compilar e Instalar
1. Compile e instale o projeto utilizando Gradle:
   ```sh
   ./gradlew installDist
   ```

### Executar o Servidor
1. Inicie o servidor com o comando:
   ```sh
   ./build/install/examples/bin/system-metrics-server
   ```
2. O servidor estará rodando na porta `50051` e pronto para receber conexões.

### Executar o Cliente
1. Inicie o cliente com o comando:
   ```sh
   ./build/install/examples/bin/system-metrics-client
   ```
2. O cliente começará a enviar métricas para o servidor a cada 15 segundos.

## Observações
- Certifique-se de que o servidor esteja em execução antes de iniciar o cliente.
- Para personalizar a frequência de envio de métricas, edite o valor no `scheduler.scheduleAtFixedRate` dentro do `SystemMetricsClient.java`.
- O projeto pode ser adaptado para suportar autenticação e criptografia utilizando TLS em vez de conexões inseguras.

## Licença
Este projeto está licenciado sob a **Apache License 2.0**.

