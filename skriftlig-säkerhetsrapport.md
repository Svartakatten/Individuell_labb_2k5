# Säkerhetsrapport: Analys och Åtgärder enligt OWASP Top 10

## 1. A01:2021 – Broken Access Control
**Identifiering:** Applikationens primära endpoint exponerades ursprungligen utan några mekanismer för åtkomstkontroll. Detta innebar att API:et var publikt tillgängligt för alla klienter med nätverksåtkomst.

**Åtgärd:** Ett säkerhetslager implementerades via Spring Security (`SecurityConfig`). Metoden `SecurityFilterChain` konfigurerades med direktivet `authorizeHttpRequests(auth -> auth.anyRequest().authenticated())`, vilket stänger alla endpoints som standard och tvingar klienten att tillhandahålla giltiga autentiseringsuppgifter via HTTP-headern.

**Analys & Prioritering:** Avsaknaden av åtkomstkontroll utgör en kritisk risk för "Resource Exhaustion". Eftersom proxy-tjänsten konsumerar en extern, avgiftsbelagd AI-leverantör (OpenAI), skapar en oskyddad endpoint en direkt finansiell sårbarhet. En illvillig aktör kunde via automatiserade skript genomföra massanrop, vilket omedelbart hade förbrukat systemets API-krediter (tömt AI-budgeten) och resulterat i en Denial of Service (DoS) för användare. Att blockera obehörig åtkomst på HTTP-nivå eliminerar denna attackvektor innan anropet når applikationens affärslogik.

---

## 2. A06:2021 – Vulnerable and Outdated Components
**Identifiering:** Applikationen förlitar sig på Spring Boot och flera externa tredjepartsbibliotek. Inledningsvis saknades en process för att granska dessa beroenden, vilket innebar att kända sårbarheter (CVE:er) i äldre versioner av inbyggda bibliotek kunde ärvas och utnyttjas.

**Åtgärd:** OWASP Dependency-Check Maven Plugin integrerades i projektets `pom.xml`. Verktyget konfigurerades med `<failBuildOnCVSS>7.0</failBuildOnCVSS>`. Vidare adderades kommandot `mvn dependency-check:check` till CI/CD-pipelinen i GitHub Actions. Bygget avbryts numera per automatik om kritiska sårbarheter hittas.

**Analys & Prioritering:** Ett system är sällan säkrare än dess svagaste beroende. Utan kontinuerlig skanning riskerar applikationen att drabbas av fjärrkörning av kod (RCE) eller dataexponering genom brister i ramverk såsom Jackson eller Tomcat. Att manuellt spåra CVE:er är ohållbart i en produktionsmiljö. Genom att injicera skanningsverktyget direkt i CI-pipelinen (en metodik känd som "Shift-Left Security") skapas en strikt "Gatekeeper"-mekanism som förhindrar att sårbara Docker-avbilder överhuvudtaget byggs eller publiceras till registret.

---

## 3. A07:2021 – Identification and Authentication Failures
**Identifiering:** Utöver bristen på åtkomstkontroll saknade applikationen initialt funktionalitet för att kryptografiskt eller logiskt verifiera en klients identitet och hantera systemhemligheter säkert.

**Åtgärd:** Autentisering konfigurerades genom HTTP Basic Auth (`httpBasic()`) med hjälp av en `InMemoryUserDetailsManager`. För att undvika hårdkodning av autentiseringsuppgifter konfigurerades lösenordet att läsas in från miljövariabeln `API_CLIENT_SECRET` (injicerad via `@Value` och GitHub Secrets).

**Analys & Prioritering:** För att säkerställa spårbarhet och förhindra obehörig tillgång krävs en entydig identitetsverifiering. Hårdkodade nycklar i källkoden utgör en primär säkerhetsbrist vid versionshantering. Genom att bryta ut nyckelhanteringen från applikationskoden och låta injektionen ske vid exekvering (via CI/CD-hemligheter eller körningsmiljön) minimeras ytan för nyckelläckage. Detta garanterar att endast de klienter eller system som besitter den distribuerade hemligheten kan identifiera sig korrekt och exekvera AI-förfrågningarna.