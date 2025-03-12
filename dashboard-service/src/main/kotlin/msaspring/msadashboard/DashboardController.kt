package msaspring.msadashboard

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
class DashboardController(private val dashboardApiClient: DashboardApiClient) {

    @GetMapping("/")
    fun dashboard(model: Model): Mono<String> {
        return dashboardApiClient.getDashboardData()
            .map { data ->
                model.addAttribute("data", data)
                "dashboard"
            }
    }
}