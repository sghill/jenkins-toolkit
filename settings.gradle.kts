rootProject.name = "jenkins-toolkit"

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            val retrofitVersion = version("retrofit") {
                strictly("[2.0.0, 3.0.0[")
            }
            library("retrofit-core", "com.squareup.retrofit2", "retrofit").versionRef(retrofitVersion)
            library("retrofit-jackson", "com.squareup.retrofit2", "converter-jackson").versionRef(retrofitVersion)
            bundle("retrofit", listOf("retrofit-core", "retrofit-jackson"))

            library("okhttp-bom", "com.squareup.okhttp3", "okhttp-bom").version {
                strictly("[4.0.0, 5.0.0[")
                reject("4.10.0-+")
            }
            library("okhttp-core", "com.squareup.okhttp3", "okhttp").withoutVersion()

            library("jackson-bom", "com.fasterxml.jackson", "jackson-bom").version {
                strictly("[2.0.0, 3.0.0[")
            }

            library("commons-io", "commons-io", "commons-io").version {
                strictly("[2.0.0, 3.0.0[")
            }
        }

        create("testLibs") {
            library("junit-bom", "org.junit", "junit-bom").version {
                strictly("[5.0.0, 6.0.0[")
                reject("5.9.0-+")
            }
            library("junit-api", "org.junit.jupiter", "junit-jupiter-api").withoutVersion()
            library("junit-parameters", "org.junit.jupiter", "junit-jupiter-params").withoutVersion()
            library("junit-engine", "org.junit.jupiter", "junit-jupiter-engine").withoutVersion()
            library("mockito-bom", "org.mockito", "mockito-bom").version {
                strictly("[4.0.0, 5.0.0[")
            }
            library("mockito-core", "org.mockito", "mockito-core").withoutVersion()
            library("assertj-core", "org.assertj", "assertj-core").version {
                strictly("[3.0.0, 4.0.0[")
            }
            bundle("junit5", listOf("junit-api", "junit-parameters", "mockito-core", "assertj-core"))

            library("okhttp-mockwebserver", "com.squareup.okhttp3", "mockwebserver").withoutVersion()
        }
    }
}