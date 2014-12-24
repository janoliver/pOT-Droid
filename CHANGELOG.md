# Changelog

## Version 4

### Changelog 4.0.0

  * Angepasst an's Material Design und weitreichende Design Änderungen
  * Neue HTTP engine mit einigen neuen Features
  * Bildercache, Bilder Sharing Funktion
  * Usability wurde verbessert
  * Swipe to refresh und Blättern per Swipe eingebaut
  * Neuer BBCode Editor
  * Diverse neue Einstellungsmöglichkeiten
  * Light theme

## Version 3

### Changelog 3.0.2, 3.0.3

  * Bugfixes

### Changelog 3.0.1

  * Startactivity wieder konfigurierbar
  * diverse neue Settings

### Changelog 3.0.0

  * Komplett neu geschrieben
  * Fragment basiert
  * Tablet/Landscape Views
  * PM System
  * UI überarbeitet
  * Neue HTTP request engine
  * Neue parser
  * Externe Bibliotheken entfernt oder ersetzt
  * uvm.

## Version 2

### Changelog Alpha 28 (pOT Droid 2.0.8)

  * LPIP Board hinzugefügt

### Changelog Alpha 28 (pOT Droid 2.0.8)

  * Bender Support (siehe Einstellungen!)
  * Menütaste funktioniert wieder
  * Beim Wiederaufrufen der App wird der Thread nicht mehr neu geladen, sondern zur Startactivity weitergeleitet
  * Kleinere Bugfixes und 3rd Party Libs erneuert.

### Changelog Alpha 27 (pOT Droid 2.0.7)

  * [tex] Support
  * Bugfixes
  * PM notifictaion jetzt auch nach reboot.

### Changelog Alpha 26 (pOT Droid 2.0.6)

  * Bugfixes

### Changelog Alpha 25 (pOT Droid 2.0.5)

  * Bugfixes

### Changelog Alpha 24 (pOT Droid 2.0.4)

  * Komplett überarbeitetes Layout, überall Holo
  * Actionbars
  * Sidebar (wischen vom linken Bildschirmrand) mit ungelesenen Bookmarks
  * Post icons hinzugefügt
  * Eigene Activity zum Posten
  * Code an vielen Stellen modernisiert
  * Viele, viele Bugs gefixt
  * Viele, viele kleinere Verbesserungen

### Changelog Alpha 23 (pOT Droid 0.23)

  * peter,pansen Bug gefixt
  * kleinere Bugs im bbcode parser gefixt
  * links aus dem Browser (und auch aus der app) können erstmal nicht mehr mit potdroid geöffnet werden.

### Changelog Alpha 22 (pOT Droid 0.22)

  * New PM Notifications
  * Verhalten von touch auf Thread getauscht (long touch -> erste Seitem, short touch -> letzte Seite)
  * Startactivity auswählbar. (Forenübersicht, Bookmarks, einzelnes Forum.)
  * Ein paar Bugs behoben.

### Changelog Alpha 21 (pOT Droid 0.21)

  * Spoiler durch button ersetzt
  * Bilder können automatisch verkleinert werden (siehe settings)
  * Diverse Bugs behoben

### Changelog Alpha 20 (pOT Droid 0.20)

  * Neuer, schnellerer BBCode Parser, mehr BBCode wird unterstützt
  * Links zum Forum aus einem Browser können mit potdroid geöffnet werden
  * editieren/antworten: Textfeld ist vorausgewählt und der Cursor ans Ende geschoben.
  * Langes Drücken eines Threads in der Board-Ansicht (NICHT in den Bookmarks!) führt zur letzten Seite.

### Changelog Alpha 19 (pOT Droid 0.19)

  * Orientierungsbug gefixt.

### Changelog Alpha 18 (pOT Droid 0.18)

Einmal neu Einloggen ist wie immer nötig!

  * Eine Berechtigung weniger, Logs werden nicht mehr auf die SD Karte geschrieben
  * Passwort wird nicht mehr gespeichert.
  * Threadseiten Bug gefixt (Danke Danzelot!)
  * Orientierungsbug gefixt
  * Neue Einstellung: Dauer des nötigen Touches für das Post Menü. (Das mit Zitieren usw.)

### Changelog Alpha 17 (pOT Droid 0.17)

Hab ich alles vergessen.

### Changelog Alpha 16 (pOT Droid 0.16)

Einmal neu Einloggen ist wie immer nötig!

  * Angepasst ans ICS Holo theme (danke Noch-ein-Kamel!) und dessen Actionbar
  * Der gesamte Code für die Models und deren Handling (also Parsing der xml api files usw.) ist neu geschrieben. Alle Activtys wurden kommentiert und aufgeräumt.
  * Jegliche Kommunikation mit dem INternet läuft jetzt per gzip ab. (Danke enos!)
  * Icons in den Menüs
  * Crashbug bei geschlossenen Threads und ausgeloggten Usern behoben
  * Mata BUtton in der Actionbar hat ein in den Settings einstellbares Ziel.
  * Migration zu github für bessere Zusammenarbeit. Mal gucken. :D
  * neue bbcode tags mod/spoiler, sowie b/k gefixt.

### Changelog Alpha 15 (pOT Droid 0.15)

  * Experimentelle "Gravitation" nach links in Threads. In den Settings einstellbar. Issue #14
  * Themes (dark, light) für die Forenübersichten usw. 
  * die gesamte Codebase aufgeräumt und einen einheitlicheren Stil verwendet
  * Uncaught exceptions werden jetzt unter "/sdcard/Android/data/com.janoliver.potdroid/files" in Log Dateien geschrieben. Bitte bei Bug Reports anhängen!

### Changelog Alpha 14 (pOT Droid 0.14)

  * login bug gefixt.
  * posten/editieren jetzt schneller
  * Bookmarks hinzufügen/entfernen
  * about Dialog in den Preferences
  * Einstellung, ob ungelesene Posts markiert werden sollen
  * leichte Änderungen in der Threadansicht (links)
  * Menü in der Threadansicht verändert: vor/zurück Buttons raus, Einstellungen Button rein.

Ihr müsst euch einmal neu einloggen!

### Changelog Alpha 13 (pOT Droid 0.13)

  * user agent jetzt unique nach Login
  * Buttons im Thread größer
  * Thread layout überarbeitet
  * Bilder laden im WLAN / nie / immer
  * Ladedialog beim editieren/posten. Kann abgebrochen werden.
  * Eigene Posts werden gehighlighted.

Ihr müsst euch einmal neu einloggen!

### Changelog Alpha 12 (pOT Droid 0.12)

  * Bug mit den Seitenzahlen behoben
  * Buttons zum Navigieren im Thread hinzugefügt
  * Login System überarbeitet: Cookie wird jetzt Sessionübergreifend gespeichert (-> schnellerer Start der App)
  * Cache System der Foren- und Kategorieübersicht überarbeitet -> schnelleres Browsen
  * Einige Crashbugs behoben (hoffentlich....)
  * Ein bisschen den Code verschönert
  * Kleinere, kosmetische Änderungen.

### Changelog Alpha 11 (pOT Droid 0.11)

  * Kompatibel zu 2.1
  * Forenübersichten werden jetzt bei jedem Appaufruf nur einmal geladen.
  * Orientierung kann jetzt überall geändert werden außer beim Laden und Post schreiben. Es wird beim Drehen nicht mehr neu geladen.
  * Updatebenachrichtigung entfernt.

### Changelog Alpha 10 (pOT Droid 0.10)

  * Login gefixt
  * Bilder können durch langes Drücken versteckt werden
  * Ladezeiten drastisch verringert (danke enos!!)
  * Post-Titel werden angezeigt.

### Changelog Alpha 9 (pOT Droid 0.9)

  * Login gefixt
  * Text "Blättern durch wischen" rausgenommen

### Changelog Alpha 8 (pOT Droid 0.8)

  * Themes in der Threadansicht
  * Bug gefixt, dass manchmal bei Bookmarks nicht zum richtigen Post gescrollt wird. Die setting dazu kann ruhig auf einem kleinen Wert stehen. (ich weiss nichtmal, ob es ueberhaupt benoetigt wird...)
  * Smileys
  * bestimmt noch irgendwas, was ich wieder vergessen habe...

### Changelog Alpha 7 (pOT Droid 0.7)

  * Update check jetzt überall, einmalig bei Programmstart.
  * volume buttons können in den Settings zum Blättern eingestellt werden.
  * neue, experimentelle Thread-Ansicht. (Unter der Haube...) 
  * Bilder per click nachladbar!
  * schneller im Laden und scrollen!
  * intern sehr viel einfacher handzuhaben.
  * an/ausstellbar in den Settings.

### Changelog Alpha 6 (pOT Droid 0.6)

  * Bug mit der Auswahl der Threadseite gefixt (bei nur einem Post auf der neuen Seite)
  * Viele Serverrequests rausgenommen = schneller
  * Lade-Vorgänge können abgebrochen werden.
  * sicherlich noch weitere Änderungen, die ich aber vergessen habe. :(

### Changelog Alpha 5 (pOT Droid 0.5)

  * Thread Seite im Dialog auswaehlbar
  * Bookmarks werden als Gelesen markiert
  * Antworten (+zitieren) und Editieren von Posts moeglich (nur Titel+Text) (Es werden derzeit keine geschlossenen Threads beachtet)
  * Bug gefixt dass man nicht eingeloggt ist, wenn das Handy offline war
  * Information bei neuer Version (in den Settings ausstellbar). Diese wird im Moment nur in der Forenübersicht geprüft. Ich muss mir da noch eine bessere Lösung überlegen, damit es nicht zu nervig ist, aber aktuell.
  * Versionsnummern eingeführt: Aktuell: 0.5

### Changelog Alpha 4

  * Login Bug gefixt, Sonderzeichen jetzt möglich
  * Ladevorgänge überarbeitet
  * Kein crash mehr bei fehlender Internetverbindung, sondern entsprechende Anzeige
  * Buttons zum Blättern in Threads und Foren ins Menü eingebaut, da der Wisch-Bug im Thread noch nicht behoben werden konnte.
  * Repository bereinigt und Pfade relativiert

### Changelog Alpha 3

  * Neuer Login Dialog
  * Langes klicken auf Threads erlaubt springen zur letzten/ersten Seite
  * Bookmarks werden ab dem 1. ungelesenen Post angezeigt
  * Indikator für ungelesen bei Bookmarks etwas breiter
  * Lade-Dialog bei Threadaufruf. 
  * Möglichkeit, Posts vorzuladen. ->Scrollen wird smoother, dauert aber länger
  * Setting, Bilder nicht anzuzeigen. Sollte noch durch ein Anzeigen-bei-click oder einen Link auf das Bild erweitert werden
