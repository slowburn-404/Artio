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
include(":core")
include(":core:data")
include(":core:data:database")
include(":core:data:firebase")
include(":core:data:datastore-preferences")
include(":core:ui")
include(":feature")
include(":feature:auth")
include(":feature:draw")
include(":feature:chat")
