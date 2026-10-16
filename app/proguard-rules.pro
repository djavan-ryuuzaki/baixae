# youtubedl-android: o JSON do yt-dlp é lido com Jackson por reflexão nas classes de mapeamento.
-keep class com.yausername.** { *; }
-keep class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.jackson.**
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Apache Commons Compress (usado pelo youtubedl-android para extrair Python e ffmpeg) instancia classes por reflexão.
-keep class org.apache.commons.compress.** { *; }
-dontwarn org.apache.commons.compress.**
