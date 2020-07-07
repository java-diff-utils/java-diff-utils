import org.apache.tools.ant.taskdefs.condition.Os

group = "dev.gitlive"
version = "4.1.5"

plugins {
    `maven-publish`
    signing
    kotlin("native.cocoapods")
    kotlin("multiplatform")
}

repositories {
    mavenLocal()
    google()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/kotlin-dev")
}

kotlin {
    jvm {
        val main by compilations.getting {
            kotlinOptions {
                jvmTarget = "1.8"
            }

        }
    }

    js {
        val main by compilations.getting {
            kotlinOptions {
                sourceMap = true
                sourceMapEmbedSources = "always"
                moduleKind = "commonjs"
            }
        }
    }

    iosArm64()
    iosX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
            }
        }
        val jsMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-js")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib-jdk8"))
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("junit:junit:4.12")
                implementation("org.assertj:assertj-core:3.11.1")
            }
        }
    }
}


fun SigningExtension.whenRequired(block: () -> Boolean) {
    setRequired(block)
}

tasks {
    val copyPackageJson by registering(Copy::class) {
        from(file("package.json"))
        into(file("$buildDir/node_module"))
    }

    val copyJS by registering(Copy::class) {
        from(file("$buildDir/classes/kotlin/js/main/${project.name}.js"))
        into(file("$buildDir/node_module"))
    }

    val copySourceMap by registering(Copy::class) {
        from(file("$buildDir/classes/kotlin/js/main/${project.name}.js.map"))
        into(file("$buildDir/node_module"))
    }

    val copyReadMe by registering(Copy::class) {
        from(file("$buildDir/README.md"))
        into(file("$buildDir/node_module"))
    }

    val publishToNpm by registering(Exec::class) {
        doFirst {
            mkdir("$buildDir/node_module")
        }
        dependsOn(copyPackageJson, copyJS, copySourceMap, copyReadMe)
        workingDir("$buildDir/node_module")
        if(Os.isFamily(Os.FAMILY_WINDOWS)) {
            commandLine("cmd", "/c", "npm publish")
        } else {
            commandLine("npm", "publish")
        }
    }
}

val javadocJar by tasks.creating(Jar::class) {
    archiveClassifier.value("javadoc")
}

var shouldSign = true

tasks.withType<Sign>().configureEach {
    onlyIf { shouldSign }
}

tasks.named("publishToMavenLocal").configure {
    shouldSign = false
}

publishing {
    repositories {
        maven {
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = project.findProperty("sonatypeUsername") as String? ?: System.getenv("sonatypeUsername")
                password = project.findProperty("sonatypePassword") as String? ?: System.getenv("sonatypePassword")
            }
        }
    }

    publications.all {
        this as MavenPublication

        artifact(javadocJar)

        pom {
            name.set("kotlin-diff-utils")
            description.set("The DiffUtils library for computing diffs, applying patches, generationg side-by-side view in Java.")
            url.set("https://github.com/GitLiveApp/kotlin-diff-utils")
            inceptionYear.set("2009")

            scm {
                url.set("https://github.com/GitLiveApp/kotlin-diff-utils")
                connection.set("scm:git:https://github.com/GitLiveApp/kotlin-diff-utils.git")
                developerConnection.set("scm:git:https://github.com/GitLiveApp/kotlin-diff-utils.git")
                tag.set("HEAD")
            }

            issueManagement {
                system.set("GitHub Issues")
                url.set("https://github.com/GitLiveApp/kotlin-diff-utils/issues")
            }

            developers {
                developer {
                    name.set("Tobias Warneke")
                    email.set("t.warneke@gmx.net")
                }
            }

            licenses {
                license {
                    name.set("The Apache Software License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                    comments.set("A business-friendly OSS license")
                }
            }

        }
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
