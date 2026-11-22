# Room entities
-keep class com.finora.app.data.local.entity.** { *; }

# Hilt generated
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Kotlin metadata
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions
