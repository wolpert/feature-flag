plugins {
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
}
nexusPublishing {
    repositories {
        sonatype()
    }
}
allprojects {
    group = "com.codeheadsystems"
    version = "1.0.6"
}
