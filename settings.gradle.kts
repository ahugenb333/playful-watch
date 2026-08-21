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

includeBuild("../../ComposeOClock") {
    dependencySubstitution {
        substitute(module("org.splitties.compose.oclock:core")).using(project(":oclock-core"))
        substitute(module("org.splitties.compose.oclock:watchface-renderer"))
            .using(project(":oclock-watchface-renderer"))
    }
}

rootProject.name = "playful-watch"

include(":playful-foundation")
include(":watchfaces")
include(":wear")
include(":phone")
