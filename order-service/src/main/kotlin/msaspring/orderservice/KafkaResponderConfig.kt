package msaspring.orderservice

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaResponderConfig(private val kafkaOrderQueryResponder: KafkaOrderQueryResponder) {

    @PostConstruct
    fun initKafkaResponder() {
        kafkaOrderQueryResponder.startListening()
    }
}