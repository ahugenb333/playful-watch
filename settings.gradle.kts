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

includeBuild(composeOClockDir()) {
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

fun composeOClockDir(): java.io.File {
    val fromLocalProperties = readComposeOClockDirFromLocalProperties()
    val candidates = buildList {
        if (fromLocalProperties != null) add(fromLocalProperties)
        add(file("../compose-oclock"))
    }
    return candidates.firstOrNull { dir ->
        dir.resolve("settings.gradle.kts").isFile
    } ?: error(
        """
        Compose O'Clock not found. Use a side-by-side checkout or set compose.oclock.dir in local.properties:
          git clone https://github.com/ahugenb333/compose-oclock.git ../compose-oclock
          echo compose.oclock.dir=/path/to/compose-oclock >> local.properties
        """.trimIndent(),
    )
}

fun readComposeOClockDirFromLocalProperties(): java.io.File? {
    val propsFile = file("local.properties")
    if (!propsFile.isFile) return null
    val key = "compose.oclock.dir"
    val line = propsFile.readLines()
        .map { it.trim() }
        .firstOrNull { it.startsWith("$key=") || it.startsWith("$key =") }
        ?: return null
    val raw = line.substringAfter("=").trim()
    if (raw.isEmpty()) return null
    return file(raw)
}
