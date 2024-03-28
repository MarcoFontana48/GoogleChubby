plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":server"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("io.javalin.community.openapi:javalin-openapi-plugin:5.6.3-2")
    implementation("io.javalin.community.openapi:javalin-swagger-plugin:5.6.3-2")
    implementation("io.etcd:jetcd-core:0.7.6")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

application {
    mainClass.set("chubby.setup.EtcdSetup")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("etcd_setup") {
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args()
}