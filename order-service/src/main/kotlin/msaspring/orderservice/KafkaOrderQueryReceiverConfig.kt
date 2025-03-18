package msaspring.orderservice

import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JsonDeserializer
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions

@Configuration
class KafkaOrderQueryReceiverConfig {

    @Value("\${spring.kafka.bootstrap-servers:kafka:9092}")
    private lateinit var bootstrapServers: String

    @Bean
    fun orderQueryReceiver(): KafkaReceiver<String, OrderQueryResponse> {
        val props = mapOf<String, Any>(
            "bootstrap.servers" to bootstrapServers,
            "key.deserializer" to StringDeserializer::class.java,
            "value.deserializer" to JsonDeserializer::class.java,
            "group.id" to "order-query-response-group",
            "auto.offset.reset" to "earliest"
        )
        val jsonDeserializer = JsonDeserializer(OrderQueryResponse::class.java)
        jsonDeserializer.addTrustedPackages("*")
        val receiverOptions = ReceiverOptions.create<String, OrderQueryResponse>(props)
            .withValueDeserializer(jsonDeserializer)
            .subscription(listOf("order-query-response"))
        return KafkaReceiver.create(receiverOptions)
    }
}