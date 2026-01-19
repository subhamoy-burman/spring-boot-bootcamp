# Method-Level Security Demo - Voice Over Script (300 words)

## Complete Voice-Over Script

"Welcome to the Method-Level Security demonstration. Today we'll learn how to protect business logic methods, not just URLs.

**Setup:** We have three users—Virat and Messi with ROLE_USER, and Admin with ROLE_ADMIN. The VideoService has a deleteAllVideos() method protected with @PreAuthorize('hasRole(ADMIN)'). This annotation checks permissions BEFORE the method executes.

**Test 1 - Admin User (Success):**
Let's log in as Admin with password admin123. Click Admin Panel. Notice we access it successfully. Now click 'Delete All Videos'. Success! The method executed because the admin has ROLE_ADMIN. The @PreAuthorize check passed.

**Test 2 - Regular User (Failure):**
Now logout and log in as Virat with password password. Try accessing Admin Panel. Access denied! URL-level security blocked us. But here's the key point: even if a regular user somehow bypassed URL security and called deleteAllVideos() directly, the @PreAuthorize annotation would STILL protect it.

**Two Security Layers:**
Layer 1 - URL Security: Protects the /admin endpoint. Layer 2 - Method Security: Protects the deleteAllVideos() method. Why both? Defense in depth. If someone bypasses URL security through a different endpoint, method-level security still protects the sensitive operation.

**Key Concepts:**
@PreAuthorize checks permissions BEFORE execution. If denied, AccessDeniedException is thrown and the method never runs. @EnableGlobalMethodSecurity activates this feature. Without it, @PreAuthorize is ignored. We use SpEL expressions like hasRole('ADMIN'), same as URL security.

**Why It Matters:**
With URL-level security alone, you'd need to protect each endpoint separately. With method-level security, you protect the method ONCE, and it's protected everywhere it's called—web UI, REST API, scheduled tasks, anywhere.

**Summary:**
Method-level security is production-grade thinking. It ensures sensitive business logic is protected at the source, not just at the endpoint level. This is defense in depth in action."

---

## Quick Demo Steps

1. **Login as Admin** → admin123 → Access Admin Panel ✓
2. **Click Delete Videos** → Success message ✓
3. **Logout and Login as Virat** → password → Access Denied ✓
4. **Understand:** URL blocks access, but method-level security provides extra protection

---

## Code Reference

```java
// Enable method security
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig { ... }

// Protect method
@PreAuthorize("hasRole('ADMIN')")
public void deleteAllVideos() { ... }
```
