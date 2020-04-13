# pOT Droid

pOT Droid ist eine Android (4.0+) Applikation, die das surfen im mods.de Forum
auf Android Handys erleichtert und beschleunigt.

## APK Downloaden

Das geht entweder auf Github bei den [Releases](https://github.com/janoliver/pOT-Droid/releases), oder für ältere Versionen [hier](http://potdroid.oelerich.org/).

## Features

* Browsen der mods.de Foren mit oder ohne User account
* Unterstützung für Bender
* Antworten schreiben/editieren/zitieren
* Volle Unterstützung von BBCode, Smileys, Icons etc.
* Bookmarks werden komplett unterstützt
* Optimiert für unterschiedliche Display-Größen und Orientierungen
* Kompletter PM Support
* Medien teilen, Speichern, ansehen
* Verschiedene Themes, teilweise selbst anpassbar
* Quickmod Integration
* Viele Gimmicks, die das Surfen im Forum erleichtern sollten.

## Links

* [mods.de Forum](http://forum.mods.de/bb/)
* [Play Store](https://play.google.com/store/apps/details?id=com.mde.potdroid)
* [Projekt Repository und Bug Tracker](https://github.com/janoliver/pOT-Droid/)

## Hacking

Ihr solltet die Android SDK und Android Studio installiert haben. Ihr solltet die SDK der
`targetSDKVersion` (siehe `build.gradle`) sowie die `support library` und die `app compatibility
library` installiert haben. Alle weiteren Dependencies werden über Maven gezogen.

Die `debug` build Variante wird als `com.mde.potdroid.dev` installiert, somit könnt ihr neben der
normalen App eure eigenen Änderungen testen.

## Thread-Ansicht mittels eigenen CSS und JS Dateien anpassen

Es werden alle `.css` und `.js` Dateien aus `/sdcard/Android/data/com.mde.potdroid/files/custom_style`
gelesen und in der Topic Ansicht _nach_ den Standard Stylesheets und Scripten eingefügt. Somit
könnt ihr mit einigen CSS / Javascript Kenntnissen die Topicansicht euren Wünschen anpassen.

Zum debuggen kann unter anderem der Chrome Browser auf dem Desktop verwendet werden. Dazu muss die
App in den Einstellungen unter `System` im Debug Modus sein. Siehe
[hier](https://developers.google.com/web/tools/chrome-devtools/debug/remote-debugging/remote-debugging?hl=en).

Wichtige Links:

 * [thread.html](https://github.com/janoliver/pOT-Droid/blob/master/src/main/assets/thread.html)
 * [bb.css](https://github.com/janoliver/pOT-Droid/blob/master/src/main/assets/bb.css)
 * [thread.js](https://github.com/janoliver/pOT-Droid/blob/master/src/main/assets/thread.js)

jQuery kann verwendet werden.