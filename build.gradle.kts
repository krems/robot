plugins {
    java
}

group = "ru.oval"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.telegram:telegrambots:4.7")
    implementation("org.apache.logging.log4j:log4j-api:2.13.1")
    implementation("org.apache.logging.log4j:log4j-core:2.13.1")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.13.1")
    compileOnly("org.projectlombok:lombok:1.18.12")
    annotationProcessor("org.projectlombok:lombok:1.18.12")


    testCompileOnly("org.projectlombok:lombok:1.18.12")
    testImplementation("junit", "junit", "4.12")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
}
