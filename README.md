# Command Journaling
## Implementazione del Command Pattern in Quarkus

Questo progetto fornisce un’implementazione di riferimento del **Command Pattern** in un’applicazione Quarkus, basata su un’architettura pulita e robusta che integra journaling, recupero automatico dei comandi e idempotenza, garantendo così un sistema resiliente e facilmente manutenibile.

## Architettura

Il codice sorgente è organizzato in package basati sui layer architetturali, promuovendo una chiara separazione delle responsabilità:

- **`api`**: Contiene i controller REST (*Resource). Questo è il livello più esterno, che gestisce le richieste HTTP e funge da punto di ingresso all'applicazione.
- **`application`**: Contiene la logica applicativa e i casi d'uso. La suddivisione in command e model è un'ottima pratica. Qui risiedono le azioni che l'applicazione può compiere.
- **`domain`**: È il cuore dell'applicazione. Contiene le entità e le regole di business principali (Command, CommandHandler, JournalEntry). Questo package non dipende da nessun altro strato dell'applicazione, principio chiave delle architetture pulite.
- **`infra`**: Contiene le implementazioni tecniche di interfacce definite nel dominio (come i Repository) e tutto ciò che riguarda l'infrastruttura (database, logging, metriche).
- **`service`**: Contiene servizi che orchestrano operazioni, specialmente per le query (JournalService, StatisticsService), fungendo da ponte tra l'API e l'infrastruttura.

## Funzionalità

- **Command Pattern**: Separa la richiesta di un'azione (il **Comando**) dalla sua esecuzione (l'**Handler**), migliorando la coesione e l'organizzazione del codice.
- **Journaling**: Ogni comando eseguito viene registrato su MongoDB. Questo fornisce una traccia di audit completa e costituisce la base per il recupero e le analytics.
- **Recupero Automatico**: All'avvio, l'applicazione recupera e riesegue automaticamente i comandi interrotti (ad esempio, a causa di un crash), garantendo la consistenza dei dati (`at-least-once delivery`).
- **Idempotenza**: Gli handler dei comandi sono progettati per essere idempotenti. Eseguire lo stesso comando più volte (ad esempio durante il recupero) non produce effetti collaterali indesiderati.

## Getting Started

1. Assicurarsi di avere Docker in esecuzione.
2. Eseguire il seguente comando dalla root del progetto:
   ```sh
   ./mvnw quarkus:dev
   ```
3. Utilizzando **Quarkus Dev Services**, un'istanza di MongoDB verrà avviata e configurata automaticamente.

## Usage

### 1. Invia un Comando di Creazione Utente

Usa `curl` per inviare un comando. Riceverai una risposta `HTTP 202 Accepted` che indica che il comando è stato accettato per l'elaborazione.

```sh
curl -i -X POST http://localhost:8080/api/commands/users \
  -H "Content-Type: application/json" \
  -d '{"username": "marco", "email": "marco@example.com", "actor": "test-user"}'
```

### 2. Verifica l'Idempotenza

Esegui lo stesso comando una seconda volta. Noterai nei log dell'applicazione un messaggio che indica che l'operazione è stata saltata, poiché l'utente esiste già. Questo dimostra che il sistema è idempotente.

### 3. Controlla il Journal

Per vedere la traccia di tutti i comandi eseguiti, interroga l'endpoint del journal:

```sh
curl http://localhost:8080/api/journal | jq
```

Vedrai le entry per entrambi i comandi, entrambe con stato `COMPLETED`, ma con risultati diversi a dimostrazione dell'idempotenza.

## Aggiungere Nuovi Comandi

L'architettura è pensata per essere estensibile. Per aggiungere un nuovo comando, segui questi 3 passi:

1. **Crea il Record del Comando**: Definisci un nuovo `record` che implementa `Command` nel package `application`.
2. **Crea l'Handler del Comando**: Implementa la logica di business in una nuova classe che implementa `CommandHandler` nel package `application`.
3. **Crea l'Endpoint API**: Aggiungi un metodo in una delle classi nel package `api` per creare e dispatchare il nuovo comando.

Il `CommandDispatcher` rileverà e registrerà automaticamente il nuovo handler senza richiedere modifiche.

## API Endpoints

- `POST /api/commands/users` - Accetta un comando per creare un nuovo utente
- `GET /api/journal` - Mostra tutte le entry nel journal dei comandi
- `GET /api/journal/{commandId}` - Mostra una specifica entry nel journal
- `GET /api/statistics/commands` - Fornisce statistiche sul numero di comandi eseguiti per tipo
- `GET /api/statistics/avg-time/{type}` - Calcola il tempo medio di esecuzione per un tipo di comando
- `GET /q/health` - Endpoint per health check

## Requisiti

- Java 21 
- Maven 3.8+
- Docker (per MongoDB tramite Dev Services)

## Tecnologie

- Quarkus
- MongoDB
- Jakarta CDI
- RESTEasy Reactive