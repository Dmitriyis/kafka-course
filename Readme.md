Spring Boot приложение. 

В приложении реализован функционал создания сообщения на брокере Kafka в топике orders (ProducerService).
Также реализованы два консьюмера: BatchMessageConsumerService отвечает за пакетную обработку сообщений, а SingleMessageConsumerService отвечает за одиночную обработку сообщений.

Запуск приложения:
1. выполнить команду по поднятию кластера kafka в root папки проекта docker-compose up -d
2. для сборки исходников надо выполнить команду в root папки проекта ./mvnw clean install
3. для запуска проекта выполнить команду java -jar target/kafka-yandex-0.0.1-SNAPSHOT.jar
4. api для создания сообщения в топике orders http://localhost:8085/create-orders/<name order>


Структура классов:

1. KafkaConfig — конфигурация отвечает за создания Topic Producer и Consumer.
2. Order — DTO-структура для передачи формата сообщения между Producer и Consumer.
3. ProducerService — отвечает за отправку сообщений в broker Kafka в топик orders.
4. SingleMessageConsumerService — отвечает за одиночную обработку сообщений от брокера Kafka из топика orders.
5. BatchMessageConsumerConsumerService — отвечает за пакетную обработку сообщений от брокера Kafka из топика orders.
6. OrderRestController — API принимает запросы по HTTP на создание новых заказов.

Описание конфигураций Topic Producer и Consumer.

Topic:
1. replicas = 2 (указываем, что в кластере Kafka данный топик должен иметь две копии партиций: лидер + копия, не может быть больше чем брокеров в кластаре).
2. partitions = 3 (указываем, что данный топик будет содержать три партиции для возможности параллельной обработки данных).
3. retention.ms = 604800000 (указываем время хранения сообщений в топике, после чего они будут удалены).
4. min.insync.replicas = 2 (указываем минимальное количество брокеров, которые должны находиться в синхронизированном состоянии, чтобы топик оставался доступным для записи).

Producer:
1. key.serializer = StringSerializer.class (указываем, что ключ топика будет сериализован с использованием типа String).
2. value.serializer = KafkaJsonSchemaSerializer.class (указываем, что значения топика будут сериализованы в формат JSON с привязкой к схеме в schema-registry).
3. acks = all (указываем, что сообщение должно дойти до всех min.insync.replicas, и они подтвердят его сохранение).
4. retries = 2147483647 (указываем, сколько повторных запросов на сохранение сообщения будет отправлено при неполучении клиентом от брокера Kafka сообщения о том, что он его сохранил)
4. delivery.timeout.ms = 60000 (указываем время, отведённое на доставку сообщения брокеру Kafka и получение ответа от него успешного ответа)
5. enable.idempotence = true (данный параметр Producer позволяет сохранять порядок сообщений и исключать дублирование сообщений на брокере Kafka при повторных отправках. Это достигается за счёт того, что каждому сообщению будет присвоен свой порядковый номер, и брокер при получении сообщения с таким порядковым номером будет его игнорировать).
6. max.in.flight.requests.per.connection = 3 (указываем, что Producer может параллельно обрабатывать 3 неподтверждённых сообщения от брокера Kafka).

Consumer:
0. singleMessageConsumer:
    1. key.deserializer = StringDeserializer.class (указываем, что ключ топика будет десериализован с использованием типа String).
    2. value.deserializer = ErrorHandlingDeserializer.class (обёртка над основным десериализатором, для возможности обработки ошибок десериализации)
    3. spring.deserializer.value.delegate.class = KafkaJsonSchemaDeserializer.class (указываем, что десериализация будет в формате JSON с привязкой к схеме).
    4. json.value.type = Order.class (указываем структуру класса для десериализации сообщения).
    5. group.id = <group name> (указываем имя группы для данного консюмера).
    6. auto.offset.reset = earliest (указываем, что сообщения нужно считывать с самого раннего доступного offset).
    7. enable.auto.commit = true (указываем автоматическую фиксацию сообщения после его обработки).
    8. session.timeout.ms = 6000ms (указываем промежуток времени отправки сигнала от Consumer к брокеру Kafka для подтверждения работоспособности. Если за это время Consumer не отправит сигнала, брокер будет считать, что он недоступен, и выполнит перебалансировку партиций топика).
    9. fetch.max.wait.ms = 500ms (указываем, с каким промежутком времени Consumer будет опрашивать брокер Kafka на наличие новых сообщений в топике).
0. batchMessageConsumer
    1. max.poll.records = 10 (указываем, сколько сообщений из топика мы будем считывать за один раз).
    2. fetch.min.bytes = 500 (указываем, что если в топике наберется сообщений на 500 байт, то консюмер их получит, не дожидаясь промежутка времени опроса fetch.max.wait.ms)

Настройка ConcurrentKafkaListenerContainerFactory:
1. указываем, что данный Listener будет обрабатывать сообщения пачкой (setBatchListener(true))
2. указываем, что данный Listener будет вручную подтверждать получение сообщений (factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL))
3. указываем ErrorHandler для обработки исключения в работе Consumer (factory.setCommonErrorHandler(errorHandler))

DefaultErrorHandler:
1. настраиваем FixedBackOff для повторных попыток обработки сообщений с промежутком времени (FixedBackOff(1000L, 2))
2. указываем, какие типы исключений должен обрабатывать текущий DefaultErrorHandler (handler.addNotRetryableExceptions(Exception.class))
3. указываем, чтобы не было автоматического подтверждения обработки сообщения после работы DefaultErrorHandler (handler.setAckAfterHandle(false)).

