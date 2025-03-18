package msaspring.productservice

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.support.serializer.JsonSerializer
import reactor.kafka.sender.KafkaSender
import reactor.kafka.sender.SenderOptions

@Configuration
class ReactorKafkaSenderConfig {

    @Value("\${spring.kafka.bootstrap-servers:kafka:9092}")
    private lateinit var bootstrapServers: String

    @Bean
    fun reactorKafkaSender(): KafkaSender<String, Any> {
        val senderProps = mapOf<String, Any>(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java
        )
        val senderOptions = SenderOptions.create<String, Any>(senderProps)
        return KafkaSender.create(senderOptions)
    }
}