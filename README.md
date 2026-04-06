# mqtt-broker-learning

一个以学习为目标的 Java MQTT Broker 骨架（基于 Java NIO），结构对齐你的 C 项目分层思路。

> 这是模板工程，只搭接口和占位实现，不包含完整 Broker 逻辑。

## 当前模板结构

- `config/`：运行参数与环境变量解析
- `bootstrap/`：Broker 生命周期编排入口
- `transport/poller/`：事件轮询抽象（未来对接 `Selector`）
- `transport/socket/`：监听与连接抽象
- `protocol/`：MQTT 编解码抽象
- `session/`：会话存储抽象
- `subscription/`：订阅存储抽象
- `router/`：消息路由抽象

## 运行和编译

要求：JDK 11+，Maven 3.9+

```bash
mvn clean test
mvn clean package
java -jar target/mqtt-broker-learning-0.1.0-SNAPSHOT.jar
```

可选环境变量：

- `MQTT_PORT`：默认 `1883`
- `MQTT_IDLE_SECONDS`：默认 `120`
- `MQTT_POLL_TIMEOUT_MS`：默认 `1000`
- `MQTT_MAX_CONNECTIONS`：默认 `8192`
- `MQTT_BACKLOG`：默认 `1024`

## 你下一步建议实现顺序（学习导向）

1. `NioSocketAcceptor`：`ServerSocketChannel` 打开、绑定、non-blocking、accept
2. `NioPoller`：`Selector.register/select` 事件循环
3. `PlaceholderMqttCodec`：固定头 + Remaining Length 解码
4. `InMemorySessionStore`：`clientId/channel` 双索引
5. `InMemorySubscriptionStore`：topic filter 索引与匹配
6. `DefaultMessageRouter`：PUBLISH 路由到匹配订阅者
7. `BrokerServer`：把上述模块接成可运行 Reactor 主循环

## 模板设计原则

- 接口先行：先定义边界，再填实现
- 主循环最小化：先跑通 `CONNECT/PINGREQ/DISCONNECT`
- 一次只实现一个模块，并补对应单测
