package introproject.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the application home page.
 * 
 * Spring Concepts Demonstrated:
 * - @Controller stereotype annotation (marks this as MVC controller)
 * - @GetMapping (handles HTTP GET requests)
 * - View name resolution (logical view name → template file)
 * - Component scanning discovers this bean
 */
@Controller
public class HomeController {
    
    /**
     * Handle requests to the root path "/" (home page).
     * 
     * @return Logical view name (Thymeleaf template name)
     */
    @GetMapping("/")
    public String home() {
        System.out.println(" Home page requested");
        return "home";
    }
    
    /**
     * Handle requests to /about page.
     * 
     * @return View name
     */
    @GetMapping("/about")
    public String about() {
        System.out.println(" About page requested");
        return "about";
    }
}
