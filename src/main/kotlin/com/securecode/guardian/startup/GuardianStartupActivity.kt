package com.securecode.guardian.startup

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

/**
 * Startup activity for SecureCode Guardian
 */
class GuardianStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        // Initialize plugin services
        println("SecureCode Guardian initialized for project: ${project.name}")

        // You could automatically load rules here, check for updates, etc.
    }
}
