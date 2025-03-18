package msaspring.userservice

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
class KafkaUserQueryService(
    private val sender: KafkaSender<String, Any>,
    private val receiver: KafkaReceiver<String, UserQueryResponse>
) {

    fun queryUserById(userId: Long): Mono<User> {
        val correlationId = System.currentTimeMillis().toString()
        val request = UserQueryRequest(queryType = "findById", userId = userId)
        val producerRecord = ProducerRecord<String, Any>("user-query-request", null, request)
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
            .map { response ->
                response.users.firstOrNull() ?: throw RuntimeException("User not found in response")
            }
        return sendMono.then(responseMono)
    }

    fun queryRecentUsers(count: Int): Mono<List<User>> {
        val correlationId = System.currentTimeMillis().toString()
        val request = UserQueryRequest(queryType = "getRecent", count = count)
        val producerRecord = ProducerRecord<String, Any>("user-query-request", null, request)
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
            .map { response -> response.users }
        return sendMono.then(responseMono)
    }
}