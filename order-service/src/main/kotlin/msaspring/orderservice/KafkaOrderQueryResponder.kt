package msaspring.orderservice

import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import java.nio.charset.StandardCharsets

@Service
class KafkaOrderQueryResponder(
    private val orderRepository: OrderRepository,
    @Value("\${spring.kafka.bootstrap-servers:kafka:9092}") private val bootstrapServers: String,
    private val kafkaSender: KafkaSender<String, Any>
) {

    // 리스너 설정: 'order-query-request' 토픽을 구독하고, 요청에 대해 응답 메시지를 생성하여 'order-query-response' 토픽으로 발행
    fun startListening() {
        // Consumer 설정
        val props = mapOf<String, Any>(
            "bootstrap.servers" to bootstrapServers,
            "key.deserializer" to StringDeserializer::class.java,
            "value.deserializer" to JsonDeserializer::class.java,
            "group.id" to "order-query-request",
            "auto.offset.reset" to "earliest"
        )
        // OrderQueryRequest를 역직렬화하도록 JsonDeserializer 설정(필요한 경우 trusted packages 추가)
        val jsonDeserializer = JsonDeserializer(OrderQueryRequest::class.java)
        jsonDeserializer.addTrustedPackages("*")
        val receiverOptions = ReceiverOptions.create<String, OrderQueryRequest>(props)
            .withValueDeserializer(jsonDeserializer)
            .subscription(listOf("order-query-request"))
        val receiver = KafkaReceiver.create(receiverOptions)

        receiver.receive()
            .flatMap { record ->
                val correlationId = record.headers().lastHeader("correlationId")?.let {
                    String(it.value(), StandardCharsets.UTF_8)
                } ?: ""
                val request = record.value()
                val responseMono = when (request.queryType) {
                    "findById" -> {
                        val orderId = request.orderId
                        if (orderId != null) {
                            orderRepository.findById(orderId).flatMap { order ->
                                val response = OrderQueryResponse(listOf(order))
                                sendResponse(correlationId, response)
                            }
                        } else {
                            Mono.empty()
                        }
                    }
                    "getRecent" -> {
                        val count = request.count ?: 10
                        orderRepository.findAllByOrderByIdDesc(PageRequest.of(0, count))
                            .collectList()
                            .flatMap { orders ->
                                val response = OrderQueryResponse(orders)
                                sendResponse(correlationId, response)
                            }
                    }
                    else -> Mono.empty()
                }
                // 응답 Mono 처리 후 acknowledge() 호출하여 Publisher를 반환
                responseMono.doOnSuccess { record.receiverOffset().acknowledge() }
            }
            .subscribe(
                { /* 처리 성공 */ },
                { error -> println("Error processing Kafka request: ${error.message}") }
            )
    }

    // 응답 메시지를 'order-query-response' 토픽에 발행하는 함수
    private fun sendResponse(correlationId: String, response: OrderQueryResponse): Mono<Void> {
        val producerRecord = org.apache.kafka.clients.producer.ProducerRecord<String, Any>(
            "order-query-response", null, response
        )
        producerRecord.headers().add(
            org.apache.kafka.common.header.internals.RecordHeader("correlationId", correlationId.toByteArray(StandardCharsets.UTF_8))
        )
        val senderRecord = SenderRecord.create(producerRecord, correlationId)
        return kafkaSender.send(Mono.just(senderRecord))
            .then()
            .doOnSuccess { println("Sent response with correlationId: $correlationId") }
    }
}