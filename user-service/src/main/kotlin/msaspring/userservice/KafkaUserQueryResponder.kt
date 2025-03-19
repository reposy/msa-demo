package msaspring.userservice

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
class KafkaUserQueryResponder(
    private val userRepository: UserRepository,
    @Value("\${spring.kafka.bootstrap-servers:user-kafka:9092}") private val bootstrapServers: String,
    private val kafkaSender: KafkaSender<String, Any>
) {
    fun startListening() {
        val props = mapOf<String, Any>(
            "bootstrap.servers" to bootstrapServers,
            "key.deserializer" to StringDeserializer::class.java,
            "value.deserializer" to JsonDeserializer::class.java,
            "group.id" to "user-query-request-group",
            "auto.offset.reset" to "earliest"
        )
        val jsonDeserializer = JsonDeserializer(UserQueryRequest::class.java)
        jsonDeserializer.addTrustedPackages("*")
        val receiverOptions = ReceiverOptions.create<String, UserQueryRequest>(props)
            .withValueDeserializer(jsonDeserializer)
            .subscription(listOf("user-query-request"))
        val receiver = KafkaReceiver.create(receiverOptions)

        receiver.receive()
            .flatMap { record ->
                val correlationId = record.headers().lastHeader("correlationId")?.let {
                    String(it.value(), StandardCharsets.UTF_8)
                } ?: ""
                val request = record.value()
                val responseMono = when (request.queryType) {
                    "findById" -> {
                        val userId = request.userId
                        if (userId != null) {
                            userRepository.findById(userId).flatMap { user ->
                                val response = UserQueryResponse(listOf(user))
                                sendResponse(correlationId, response)
                            }
                        } else {
                            Mono.empty()
                        }
                    }
                    "getRecent" -> {
                        val count = request.count ?: 10
                        userRepository.findAllByOrderByIdDesc(PageRequest.of(0, count))
                            .collectList()
                            .flatMap { users ->
                                val response = UserQueryResponse(users)
                                sendResponse(correlationId, response)
                            }
                    }
                    else -> Mono.empty()
                }
                responseMono.doOnSuccess { record.receiverOffset().acknowledge() }
            }
            .subscribe(
                { /* 성공 */ },
                { error -> println("Error processing user Kafka request: ${error.message}") }
            )
    }

    private fun sendResponse(correlationId: String, response: UserQueryResponse): Mono<Void> {
        val producerRecord = ProducerRecord<String, Any>(
            "user-query-response", null, response
        )
        producerRecord.headers().add(
            RecordHeader("correlationId", correlationId.toByteArray(StandardCharsets.UTF_8))
        )
        val senderRecord = SenderRecord.create(producerRecord, correlationId)
        return kafkaSender.send(Mono.just(senderRecord))
            .then()
            .doOnSuccess { println("Sent user response with correlationId: $correlationId") }
    }
}