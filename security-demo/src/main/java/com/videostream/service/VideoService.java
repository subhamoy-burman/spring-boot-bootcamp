package com.videostream.service;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class VideoService {

    /**
     * This method is protected by @PreAuthorize annotation.
     * Only users with ROLE_ADMIN can execute this method.
     * If a non-admin user tries to call this, AccessDeniedException will be thrown.
     */
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAllVideos() {
        return "✓ All videos deleted successfully! (Admin action)";
    }

    /**
     * This method is accessible to all authenticated users.
     * No @PreAuthorize annotation means no method-level security.
     */
    public String listAllVideos() {
        return "Listing all videos: Video1, Video2, Video3";
    }
}
