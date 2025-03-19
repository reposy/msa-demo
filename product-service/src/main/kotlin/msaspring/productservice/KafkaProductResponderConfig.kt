package msaspring.productservice

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaProductResponderConfig(private val kafkaProductQueryResponder: KafkaProductQueryResponder) {
    @PostConstruct
    fun initProductResponder() {
        kafkaProductQueryResponder.startListening()
    }
}