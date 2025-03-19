package msaspring.userservice

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class KafkaUserResponderConfig(private val kafkaUserQueryResponder: KafkaUserQueryResponder) {

    @PostConstruct
    fun initUserResponder() {
        kafkaUserQueryResponder.startListening()
    }
}