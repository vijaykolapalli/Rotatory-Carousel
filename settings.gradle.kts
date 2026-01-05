pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io") {
            // Optional: supply GitHub creds via gradle.properties to avoid JitPack 401s on private/rate-limited repos
            val user = providers.gradleProperty("jitpackUsername").orNull
            val token = providers.gradleProperty("jitpackToken").orNull
            if (!user.isNullOrBlank() && !token.isNullOrBlank()) {
                credentials {
                    username = user
                    password = token
                }
            }
        }
    }
}

rootProject.name = "Rotatory Carousel"
include(":app")
 