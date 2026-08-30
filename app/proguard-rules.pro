# Conservar atributos para debugging e serialização
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,*Annotation*

# Fuel
-keep class com.github.kittinunf.fuel.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep class br.com.gazoza.alcoolougasolina.data.** { *; }
-keep class br.com.gazoza.alcoolougasolina.domain.** { *; }

# Firebase & Google Play Services
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }

# AdMob
-keep class com.google.ads.** { *; }
-keep class com.google.android.gms.ads.** { *; }

# Picasso
-keep class com.squareup.picasso.** { *; }

# Ignorar avisos de bibliotecas que impedem a compilação
-dontwarn com.github.kittinunf.fuel.**
-dontwarn androidx.room.paging.**
-dontwarn okio.**
-ignorewarnings
