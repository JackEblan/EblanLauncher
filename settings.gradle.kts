pluginManagement {
    includeBuild("build-logic")
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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "EblanLauncher"
include(":app")
include(":broadcast-receiver")
include(":common")
include(":design-system")
include(":data:repository")
include(":data:room")
include(":domain:common")
include(":domain:framework")
include(":domain:grid")
include(":domain:model")
include(":domain:repository")
include(":domain:use-case")
include(":feature:home")
include(":framework:file-manager")
include(":framework:package-manager")
include(":framework:wallpaper-manager")
include(":framework:window-manager")
include(":framework:widget-manager")
include(":lint")
include(":service")
