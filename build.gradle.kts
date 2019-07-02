import org.apache.tools.ant.taskdefs.condition.Os

group = "app.teamhub"
version = "4.1-SNAPSHOT"

plugins {
    `maven-publish`
    kotlin("multiplatform") version "1.3.40"
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
                jvmTarget ="1.8"
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


tasks {
    val copyPackageJson by registering(Copy::class) {
        from(file("package.json"))
        into(file("$buildDir/node_module"))
    }

    val copyJS by registering(Copy::class) {
        from(file("$buildDir/classes/kotlin/js/main/${project.name}.js"))
        into(file("$buildDir/node_module"))
        rename("${project.name}\\.js", "index.js")
    }
    
    val copySourceMap by registering(Copy::class) {
        from(file("$buildDir/classes/kotlin/js/main/${project.name}.js.map"))
        into(file("$buildDir/node_module"))
        rename("${project.name}\\.js.map", "index.js.map")
    }
    
    val publishToNpm by registering(Exec::class) {
        dependsOn(copyPackageJson, copyJS, copySourceMap)
        workingDir("$buildDir/node_module")
        if(Os.isFamily(Os.FAMILY_WINDOWS)) {
            commandLine("cmd", "/c", "npm unpublish --force --registry http://localhost:4873 kotlin-diff-utils & npm publish --registry http://localhost:4873")
        } else {
            commandLine("npm", "unpublish", "--force", "--registry http://localhost:4873", "kotlin-diff-utils")
            commandLine("npm", "publish", "--registry http://localhost:4873")
        }
    }
}