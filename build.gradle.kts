plugins {
    //trick: for the same plugin versions in all sub-modules
    alias(libs.plugins.androidApplication).apply(false)
    alias(libs.plugins.androidKmpLibrary).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.composeMultiplatform).apply(false)
    alias(libs.plugins.kotlinSerialization).apply(false)
    alias(libs.plugins.ksp).apply(false)
    alias(libs.plugins.hilt).apply(false)
    // Necesario con el plugin aplicado en varios módulos hermanos: sin esto, cada
    // módulo lo carga en un classloader distinto y el build service compartido de
    // Maven Central falla al crear enableAutomaticMavenCentralPublishing.
    alias(libs.plugins.mavenPublish).apply(false)
}
