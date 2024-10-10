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
    }
}

rootProject.name = "Artio"
include(":app")
include(":core")
include(":core:ui")
include(":feature")
include(":feature:auth")
include(":feature:draw")
include(":feature:chat")
include(":core:model")
include(":core:firebase")
include(":core:database")
include(":core:datastore")
include(":core:data")
include(":feature:onboarding")
