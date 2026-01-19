package com.videostream.controller;

import com.videostream.service.VideoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class HomeController {

    private VideoService videoService;

    public HomeController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/home")
    public String homePage() {
        return "home";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        String videoList = videoService.listAllVideos();
        model.addAttribute("videoList", videoList);
        return "admin";
    }

    @PostMapping("/admin/deleteVideos")
    public String deleteAllVideos(Model model) {
        try {
            String result = videoService.deleteAllVideos();
            model.addAttribute("message", result);
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "Access Denied: Only admins can delete videos!");
            model.addAttribute("messageType", "error");
        }
        return "admin";
    }

    /**
     * GET endpoint to demonstrate method-level security via direct URL access.
     * Try accessing: http://localhost:8080/api/deleteAllVideos
     * 
     * Even though this is a simple GET request accessible via browser URL,
     * the @PreAuthorize annotation on deleteAllVideos() method will still protect it.
     * Only ADMIN users can access this.
     */
    @GetMapping("/api/deleteAllVideos")
    public String deleteAllVideosViaApi(Model model) {
        try {
            String result = videoService.deleteAllVideos();
            model.addAttribute("message", result);
            model.addAttribute("messageType", "success");
        } catch (Exception e) {
            model.addAttribute("message", "❌ Access Denied: Only admins can delete videos! (Method-level security protected this)");
            model.addAttribute("messageType", "error");
        }
        return "admin";
    }
}
