# Log file Analyzer 2023
## Verwendete Tools
- JDK 11 wird genutzt
- SpringBoot 2.6.7
- Thymeleaf, Bootstrap 5, JQuery 

# Install
- Application auf dem Server im Nebenordner installieren
- Zugriff über den Port 8081
- Profile sind zu beachten welchen zum Ausführen der App genutzt wird

## Projekt Funktionen

- Anzeigen von allen LogFiles auf dem Server
- Dynamischer Aufbau der WebApplication ohne viel zu konfigurieren
- Filter von mehreren unterschiedlichen Logeinträgen mit Bezug auf den Quell Ort (den Applikationen)
- Filter Zeitfenstern um die Fehleranalyse einfacher zu gestalten
- Dynamisches erkennen von unterschiedlichen LogFiles (Alleine bei uns auf dem TestServer sind mehr als 10 unterschiedliche Typen)
- Selbst konfigurierend ob Unix oder Windows System
- Security Springboot als Authentifizierung um Datenschutz zu gewähren

## Projekt Vorteile

- Fehlersuche zu vereinfachen 
- Fehler zu erkennen bevor es der Kund macht
- Ständige traversieren durch Ordnerstrukturen per Hand, wäre nicht mehr nötig
- Erweiterbar für clusterbares Monitoring bei uns im Haus