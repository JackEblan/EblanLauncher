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
include(":common")
include(":design-system")
include(":data:cache")
include(":data:datastore")
include(":data:repository")
include(":data:room")
include(":domain:common")
include(":domain:framework")
include(":domain:grid")
include(":domain:model")
include(":domain:repository")
include(":domain:use-case")
include(":feature:edit")
include(":feature:home")
include(":feature:pin")
include(":feature:settings:app-drawer")
include(":feature:settings:folder")
include(":feature:settings:general")
include(":feature:settings:gestures")
include(":feature:settings:home")
include(":feature:settings:settings")
include(":framework:bitmap")
include(":framework:file-manager")
include(":framework:icon-pack-manager")
include(":framework:launcher-apps")
include(":framework:notification-manager")
include(":framework:package-manager")
include(":framework:resources")
include(":framework:wallpaper-manager")
include(":framework:widget-manager")
include(":service")
include(":ui")
