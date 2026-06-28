# Arkitektur och Strategi: Individuell_labb_1k5

## 1. Promptstrategi och JSON-framtvingning
* **Roll och Isolation:** Systeminstruktionen tilldelar modellen en entydig roll och förbjuder konversationsinnehåll för att minimera brus.
* **JSON-framtvingning:** Det exakta dataformatet är hårdkodat. Instruktioner som "no markdown" och "no code fences" förhindrar oönskad formatering.
* **Determinism:** Parametern `temperature` är satt till 0.1 för att prioritera förutsägbarhet över kreativitet.
* **Injektionsskydd:** Modellen instrueras explicit att ignorera formateringsdirektiv i användarens inmatning.

## 2. Felhanteringsstrategi
* **Nätverkstimeouts:** För att förhindra trådblockering vid hög latens tillämpas en anslutningsgräns på 2000 ms och en läsgräns på 8000 ms.
* **Rate Limits (HTTP 429):** Vid överbelastning hos OpenAI aktiveras en exponentiell back-off-algoritm. Maximalt 3 försök genomförs, med en initial fördröjning på 1000 ms som fördubblas vid varje fel.
* **Parsning och Validering:** API-svaret deserialiseras i steg via typsäkra interna datastrukturer (`records`). Därefter tillämpas affärsregler via `jakarta.validation`.
* **Fallback-mekanism:** Om ett svar saknar giltig JSON, eller om DTO-valideringen misslyckas, fångas undantaget. Systemet loggar felet och returnerar ett standardiserat fallback-objekt istället för att krascha applikationen.

## 3. Tillförlitlighetsbedömning
* **Icke-deterministisk natur:** Språkmodeller har en inneboende risk för hallucinationer. Arkitekturen kan inte garantera semantisk korrekthet (exempelvis falsk score), men den skyddar systemet mot syntaktiska fel genom strikt validering.
* **Latensvariationer:** Extern infrastruktur erbjuder inga hårda SLA:er. Genom korta timeouts prioriteras den egna applikationens stabilitet och genomströmning framför att vänta på AI-svar.
* **Felisolering:** Transient felhantering och logisk isolering garanterar att externa avbrott hos leverantören degraderar funktionen graciöst, utan att propagera och orsaka systemomfattande driftstopp.