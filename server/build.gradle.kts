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
    implementation(project(":control"))
    implementation(project(":utilities"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("io.etcd:jetcd-core:0.7.6")
    implementation("org.jetbrains:annotations:21.0.1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

application {
    mainClass.set("chubby.server.ChubbyCell")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<JavaExec>("run_client_0-local") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client0","password","local")
}

tasks.register<JavaExec>("run_client_1-local") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client1","password","local")
}

tasks.register<JavaExec>("run_client_2-local") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client2","password","local")
}

tasks.register<JavaExec>("run_client_3-local") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client3","password","local")
}

tasks.register<JavaExec>("run_client_0-cell1") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client0","password","cell1")
}

tasks.register<JavaExec>("run_client_1-cell1") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client1","password","cell1")
}

tasks.register<JavaExec>("run_client_2-cell1") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client2","password","cell1")
}

tasks.register<JavaExec>("run_client_3-cell1") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client3","password","cell1")
}

tasks.register<JavaExec>("run_client_0-cell2") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client0","password","cell2")
}

tasks.register<JavaExec>("run_client_1-cell2") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client1","password","cell2")
}

tasks.register<JavaExec>("run_client_2-cell2") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client2","password","cell2")
}

tasks.register<JavaExec>("run_client_3-cell2") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client3","password","cell2")
}

tasks.register<JavaExec>("run_client_unauthorized0-local") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("notAValidUsername","password","local")
}

tasks.register<JavaExec>("run_client_unauthorized1-local") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client0","notAValidPassword","local")
}

tasks.register<JavaExec>("run_client_unauthorized0-cell1") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("notAValidUsername","password","cell1")
}

tasks.register<JavaExec>("run_client_unauthorized1-cell1") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client0","notAValidPassword","cell1")
}

tasks.register<JavaExec>("run_client_unauthorized0-cell2") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("notAValidUsername","password","cell2")
}

tasks.register<JavaExec>("run_client_unauthorized1-cell2") {
    standardInput = System.`in`
    mainClass.set(application.mainClass)
    classpath = sourceSets.main.get().runtimeClasspath
    args("client0","notAValidPassword","cell2")
}
