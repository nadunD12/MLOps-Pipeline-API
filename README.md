# MLOps Pipeline Management API

## Overview
The MLOps Pipeline Management API is a lightweight, scalable, RESTful web service built with Jakarta RESTful Web Services (JAX-RS) (using the Jersey framework) inside a standalone Grizzly HTTP container. Aimed at AI lab data scientists, it allows automated workflows to manage Workspaces, Models, and Model Evaluation Metrics dynamically. All data interacts purely over in-memory `ConcurrentHashMap` mechanisms.

## Build and Launch Instructions

### Prerequisites:
- Java JDK 17
- Apache Maven

1. **Clean and Compile the package**  
   Open your terminal in the root directory (where the `pom.xml` belongs) and run:
   ```bash
   mvn clean install
   ```

2. **Execute the Application Server**  
   Since we bundle a main class, Maven executes via `exec:java`:
   ```bash
   mvn exec:java -Dexec.mainClass="com.mlops.Main"
   ```
   Alternatively, run the fat shade JAR:
   ```bash
   java -jar target/mlops-api-1.0-SNAPSHOT.jar
   ```

3. The server will start and be available on `http://localhost:8080/api/v1/`.

---

## Example cURL Commands

1. **Discovery Request (Root)**
```bash
curl -X GET http://localhost:8080/api/v1/
```

2. **Create New Workspace**
```bash
curl -X POST http://localhost:8080/api/v1/workspaces \
  -H "Content-Type: application/json" \
  -d '{"teamName":"Computer Vision Lab", "storageQuotaGb":500}'
```

3. **Register New Model (*Replace UUID with the workspaceId from previous response*)**
```bash
curl -X POST http://localhost:8080/api/v1/models \
  -H "Content-Type: application/json" \
  -d '{"framework":"PyTorch", "status":"TRAINING", "workspaceId":"<workspace-uuid-here>"}'
```

4. **Append New Metric to Model (*Replace UUID with the modelId*)**
```bash
curl -X POST http://localhost:8080/api/v1/models/<model-uuid-here>/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore": 0.94}'
```

5. **Search Models by Status Filter**
```bash
curl -X GET "http://localhost:8080/api/v1/models?status=TRAINING"
```

---

## Conceptual Coursework Answers

### Part 1
**Question 1.1:** Explain the role of a MessageBodyWriter or a JSON provider (like Jackson) in this conversion process.  
**Answer:** A `MessageBodyWriter` (or JSON provider) serialises Java objects returned from JAX-RS resource methods into JSON streams that can be transmitted over HTTP. JAX-RS uses these providers to automatically convert POJOs to a requested MIME media type (e.g. `application/json`) without developers writing manual serialization pipelines bridging objects to texts.

**Question 1.2:** Define what statelessness means in this context and explain why it makes cloud APIs easier to scale horizontally across multiple servers.  
**Answer:** Statelessness in REST signifies that each distinct HTTP request contains all necessary data for the server to comprehend and fulfill it. The cloud cluster does not persist request context or "session information" on behalf of clients. Consequently, load balancers can direct simultaneous requests anywhere homogeneously—meaning 1 or 1,000 separate app servers perform equally effectively without clustering complex session caches. 

### Part 2
**Question 2.1:** Discuss how implementing HTTP Cache-Control headers on the GET workspaces endpoint could improve performance for the client and reduce unnecessary processing load on the server.  
**Answer:** Setting properties like `max-age` dictates a strict duration that client browsers or intermediary networks retain responses. If a client attempts to ping the identical query shortly after, it bypasses network transmission logic and utilizes its internal stored cache. This slashes response latency, reduces load bottlenecks on upstream infrastructure, and vastly scales client read capacity without degrading app server CPU operations.

**Question 2.2:** If a client needs to verify whether a specific workspace exists but wants to save bandwidth by not downloading the entire JSON body, which HTTP method should they use instead of GET? Explain your reasoning.  
**Answer:** The `HEAD` method. Architecturally, it is identical behaviorally to `GET`, but exclusively replies with matching foundational HTTP response Headers (including standard validation status codes like 200 OK or 404). This acts to ping entity existence safely skipping the bulky string payload decoding and network streaming transmission altogether. 

### Part 3
**Question 3.1:** When creating a new Model via a POST request... Discuss the security and data integrity reasons behind this architectural choice.  
**Answer:** Generating immutable constraints via internal systems negates client interception logic. Server UUIDs guarantee decentralized global uniqueness masking collision threats entirely. Giving users freedom exposes databases to overwrite injections if a matching primary key artificially slips in, or sequentially guessed IDs mapped improperly violating structural identity standards.

**Question 3.2:** If a user attempts to search for a framework containing spaces or special characters, how must the client modify the URL, and why is this encoding necessary?  
**Answer:** The client is obligated to properly apply "URL Encoding," transforming input like `Scikit Learn & Tools` into `Scikit%20Learn%20%26%20Tools`. Symbols like hashes (`#`), logic delimiters (`&`, `=`), and raw whitespace maintain native functionality inside standard URL decoding standards. Supplying these implicitly disrupts standard networking packet routers resulting in faulty parameters or corrupted resource resolutions.

### Part 4
**Question 4.1:** You can place annotations like `@Produces(MediaType.APPLICATION_JSON)` at either the class level or the individual method level. What is the benefit of class-level placement, and how does method-level overriding work?  
**Answer:** Applying class-level `@Produces` anchors a baseline media type constraint effectively radiating it implicitly to every HTTP mapped function wrapped beneath it, removing boilerplates completely. If specific operations necessitate exceptions structurally (e.g., streaming raw text or fetching an XML layout), appending `@Produces(MediaType.APPLICATION_XML)` directly overrides it dynamically only during overlapping method chains effectively shadowing the parent.

### Part 5
**Question 5.2:** HTTP status codes are categorised into classes... Explain fundamentally why a validation failure caused by the user providing a non-existent workspaceId must return a 4xx code rather than a 5xx code.  
**Answer:** Standard classifications cleanly bifurcate origins of API disruptions. The `4xx` suite dictates distinct explicit client faults. Meaning, the server is perfectly operational but the structured payload references invalid external artifacts (e.g., non-existent workspaceId matching rules = Error 400/422). Retaliating with `5xx` falsely suggests JVM failures, runtime panics, or application infrastructure downtime logic causing systemic confusion. 

**Question 5.4:** If an operation throws a specific custom exception... and you also have a global ExceptionMapper, how does the JAX-RS runtime determine which mapper to execute?  
**Answer:** JAX-RS frameworks harness polymorphic resolution principles applying the "nearest superclass match". The provider traverses registered mapping repositories dynamically scoring mappings sequentially against exception class branches. Inherently an exact mapping interceptor (`ExceptionMapper<LinkedWorkspaceNotFoundException>`) fundamentally takes structural priority bypassing trailing generic baseline catches mapping to plain `ExceptionMapper<Throwable>`.

**Question 5.5:** In your filter, you interact with ContainerRequestContext... List two pieces of crucial HTTP metadata you can extract... for debugging.  
**Answer:**
1. **HTTP Method** (e.g. `requestContext.getMethod()` returns GET, POST) — Allows diagnosing logic routing mismatch triggers natively.
2. **Path URI Coordinates** (e.g., `requestContext.getUriInfo().getRequestUri()`) — Immediately flags flawed request strings passing faulty trailing variables inside REST patterns directly missing standard validations.
