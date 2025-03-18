package msaspring.orderservice

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderRecord
import reactor.kafka.receiver.KafkaReceiver
import java.nio.charset.StandardCharsets
import java.time.Duration

@Service
class KafkaOrderQueryService(
    private val sender: KafkaSender<String, Any>,
    private val receiver: KafkaReceiver<String, OrderQueryResponse>
) {

    fun queryOrderById(orderId: Long): Mono<Order> {
        val correlationId = System.currentTimeMillis().toString()
        val request = OrderQueryRequest(queryType = "findById", orderId = orderId)
        val producerRecord = ProducerRecord<String, Any>("order-query-request", null, request)
        producerRecord.headers().add(
            RecordHeader("correlationId", correlationId.toByteArray(StandardCharsets.UTF_8))
        )
        val senderRecord = SenderRecord.create(producerRecord, correlationId)

        // 전송 후 응답 토픽에서 해당 correlationId로 응답 받기
        val sendMono = sender.send(Mono.just(senderRecord)).then()
        val responseMono = receiver.receive()
            .filter { record ->
                val header = record.headers().lastHeader("correlationId")
                header != null && String(header.value(), StandardCharsets.UTF_8) == correlationId
            }
            .next()
            .timeout(Duration.ofSeconds(10))
            .map { it.value() }
            .map { response ->
                response.orders.firstOrNull() ?: throw RuntimeException("Order not found in response")
            }
        return sendMono.then(responseMono)
    }

    fun queryRecentOrders(count: Int): Mono<List<Order>> {
        val correlationId = System.currentTimeMillis().toString()
        val request = OrderQueryRequest(queryType = "getRecent", count = count)
        val producerRecord = ProducerRecord<String, Any>("order-query-request", null, request)
        producerRecord.headers().add(
            RecordHeader("correlationId", correlationId.toByteArray(StandardCharsets.UTF_8))
        )
        val senderRecord = SenderRecord.create(producerRecord, correlationId)

        val sendMono = sender.send(Mono.just(senderRecord)).then()
        val responseMono = receiver.receive()
            .filter { record ->
                val header = record.headers().lastHeader("correlationId")
                header != null && String(header.value(), StandardCharsets.UTF_8) == correlationId
            }
            .next()
            .timeout(Duration.ofSeconds(10))
            .map { it.value() }
            .map { response -> response.orders }
        return sendMono.then(responseMono)
    }
}