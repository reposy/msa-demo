package msaspring.productservice

import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.header.internals.RecordHeader
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
class KafkaProductQueryResponder(
    private val productRepository: ProductRepository,
    @Value("\${spring.kafka.bootstrap-servers:kafka:9092}") private val bootstrapServers: String,
    private val kafkaSender: KafkaSender<String, Any>
) {
    fun startListening() {
        val props = mapOf<String, Any>(
            "bootstrap.servers" to bootstrapServers,
            "key.deserializer" to StringDeserializer::class.java,
            "value.deserializer" to JsonDeserializer::class.java,
            "group.id" to "product-query-request-group",
            "auto.offset.reset" to "earliest"
        )
        val jsonDeserializer = JsonDeserializer(ProductQueryRequest::class.java)
        jsonDeserializer.addTrustedPackages("*")
        val receiverOptions = ReceiverOptions.create<String, ProductQueryRequest>(props)
            .withValueDeserializer(jsonDeserializer)
            .subscription(listOf("product-query-request"))
        val receiver = KafkaReceiver.create(receiverOptions)

        receiver.receive()
            .flatMap { record ->
                val correlationId = record.headers().lastHeader("correlationId")?.let {
                    String(it.value(), StandardCharsets.UTF_8)
                } ?: ""
                val request = record.value()
                val responseMono = when (request.queryType) {
                    "findById" -> {
                        val productId = request.productId
                        if (productId != null) {
                            productRepository.findById(productId).flatMap { product ->
                                val response = ProductQueryResponse(listOf(product))
                                sendResponse(correlationId, response)
                            }
                        } else {
                            Mono.empty()
                        }
                    }
                    "getRecent" -> {
                        val count = request.count ?: 10
                        productRepository.findAllByOrderByIdDesc(PageRequest.of(0, count))
                            .collectList()
                            .flatMap { products ->
                                val response = ProductQueryResponse(products)
                                sendResponse(correlationId, response)
                            }
                    }
                    else -> Mono.empty()
                }
                responseMono.doOnSuccess { record.receiverOffset().acknowledge() }
            }
            .subscribe(
                { /* 성공 */ },
                { error -> println("Error processing product Kafka request: ${error.message}") }
            )
    }

    private fun sendResponse(correlationId: String, response: ProductQueryResponse): Mono<Void> {
        val producerRecord = ProducerRecord<String, Any>(
            "product-query-response", null, response
        )
        producerRecord.headers().add(
            RecordHeader("correlationId", correlationId.toByteArray(StandardCharsets.UTF_8))
        )
        val senderRecord = SenderRecord.create(producerRecord, correlationId)
        return kafkaSender.send(Mono.just(senderRecord))
            .then()
            .doOnSuccess { println("Sent product response with correlationId: $correlationId") }
    }
}