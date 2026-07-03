# MLOps Pipeline Management API

## Overview
The MLOps Pipeline Management API is a lightweight, scalable, RESTful web service built with Jakarta RESTful Web Services (JAX-RS) using the Jersey framework inside a standalone Grizzly HTTP container. Aimed at AI lab data scientists, it provides a seamless interface for automated MLOps pipelines to manage ML Workspaces, Machine Learning Models, and Evaluation Metrics dynamically. All data is stored purely in-memory using thread-safe `ConcurrentHashMap` data structures — no external database is used.

## Build and Launch Instructions

### Prerequisites:
- Java JDK 17
- Apache Maven

### Steps:

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

### 1. Discovery Request (Root)
```bash
curl -X GET http://localhost:8080/api/v1/
```

### 2. Create New Workspace
```bash
curl -X POST http://localhost:8080/api/v1/workspaces \
  -H "Content-Type: application/json" \
  -d '{"teamName":"Computer Vision Lab", "storageQuotaGb":500}'
```

### 3. Get All Workspaces
```bash
curl -X GET http://localhost:8080/api/v1/workspaces
```

### 4. Get a Specific Workspace by ID
```bash
curl -X GET http://localhost:8080/api/v1/workspaces/<workspace-uuid-here>
```

### 5. Register New Model (Replace UUID with the workspaceId from previous response)
```bash
curl -X POST http://localhost:8080/api/v1/models \
  -H "Content-Type: application/json" \
  -d '{"framework":"PyTorch", "status":"TRAINING", "workspaceId":"<workspace-uuid-here>"}'
```

### 6. Search Models by Status Filter
```bash
curl -X GET "http://localhost:8080/api/v1/models?status=TRAINING"
```

### 7. Append New Metric to Model (Replace UUID with the modelId)
```bash
curl -X POST http://localhost:8080/api/v1/models/<model-uuid-here>/metrics \
  -H "Content-Type: application/json" \
  -d '{"accuracyScore": 0.94}'
```

### 8. Get Evaluation Metrics History for a Model
```bash
curl -X GET http://localhost:8080/api/v1/models/<model-uuid-here>/metrics
```

### 9. Delete an Empty Workspace (successful deletion)
```bash
curl -X DELETE http://localhost:8080/api/v1/workspaces/<workspace-uuid-here>
```

### 10. Attempt to Delete a Workspace with Models (triggers 409 Conflict error)
```bash
curl -X DELETE http://localhost:8080/api/v1/workspaces/<workspace-with-models-uuid>
```

---

## Conceptual Coursework Answers

### Part 1

**Question 1.1:** Explain the role of a MessageBodyWriter or a JSON provider (like Jackson) in this conversion process.  
**Answer:** A `MessageBodyWriter` (or JSON provider like Jackson) is responsible for serialising Java objects returned from JAX-RS resource methods into JSON byte streams that can be transmitted over HTTP. When a resource method returns a Java POJO, the JAX-RS runtime inspects the `@Produces` annotation to determine the desired output media type (e.g. `application/json`). It then locates a registered `MessageBodyWriter` capable of handling that type conversion. Jackson, which is included via the `jersey-media-json-jackson` dependency in our project, provides this writer automatically. It uses reflection to introspect the POJO's getter methods and maps them to corresponding JSON key-value pairs, handling the entire serialisation pipeline without the developer needing to write any manual conversion code.

**Question 1.2:** Define what statelessness means in this context and explain why it makes cloud APIs easier to scale horizontally across multiple servers.  
**Answer:** Statelessness in the REST architectural style means that each HTTP request from a client to the server must contain all the information the server needs to understand and process that request. The server does not store any client session context or conversational state between requests. This design principle directly benefits horizontal scaling because any incoming request can be routed to any available server instance behind a load balancer — no server needs prior knowledge of the client's previous interactions. This eliminates the need for sticky sessions or shared session caches between servers, making it trivial to add or remove server instances based on demand without affecting the client experience.

### Part 2

**Question 2.1:** Discuss how implementing HTTP Cache-Control headers on the GET workspaces endpoint could improve performance for the client and reduce unnecessary processing load on the server.  
**Answer:** By setting `Cache-Control` headers with directives like `max-age=60` on the GET workspaces response (as implemented in our `WorkspaceResource`), the server instructs clients and intermediary proxies to cache the response for a specified duration. During this window, subsequent identical GET requests from the client are served directly from the local cache without ever reaching the server. This reduces network latency for the client, decreases bandwidth consumption, and significantly lowers the processing load on the server since it does not need to re-execute the request handling logic, data retrieval, or JSON serialisation for cached responses.

**Question 2.2:** If a client needs to verify whether a specific workspace exists but wants to save bandwidth by not downloading the entire JSON body, which HTTP method should they use instead of GET? Explain your reasoning.  
**Answer:** The client should use the `HEAD` method. The HTTP `HEAD` method is functionally identical to `GET` — it triggers the same server-side logic and returns the same response headers and status code (e.g. `200 OK` if found, `404 Not Found` if not). However, critically, the server omits the response body entirely. This makes `HEAD` ideal for existence checks or metadata retrieval when the actual entity payload is not needed, as it conserves bandwidth and reduces response processing time on both the client and server. In our implementation, we have explicitly provided a `@HEAD` annotated method in `WorkspaceResource` for this exact purpose.

### Part 3

**Question 3.1:** When creating a new Model via a POST request, it is considered best practice for the server to generate the unique id. Discuss the security and data integrity reasons behind this architectural choice.  
**Answer:** Server-side ID generation using `UUID.randomUUID()` provides two critical guarantees. First, regarding **security**: if clients were allowed to specify their own IDs, a malicious actor could intentionally submit an ID that matches an existing record, effectively overwriting or corrupting legitimate data. They could also enumerate sequential IDs to probe for the existence of resources they should not have access to. Second, regarding **data integrity**: server-generated UUIDs are cryptographically random and statistically guaranteed to be globally unique, eliminating the risk of primary key collisions that could arise from client-submitted IDs. This ensures every record in the data store has a truly unique identifier without relying on the client to enforce that constraint.

**Question 3.2:** If a user attempts to search for a framework containing spaces or special characters, how must the client modify the URL, and why is this encoding necessary?  
**Answer:** The client must apply URL encoding (also called percent-encoding) to any special characters in the query parameter value. For example, the search `?framework=Scikit Learn & Tools` must be encoded as `?framework=Scikit%20Learn%20%26%20Tools`. This encoding is necessary because certain characters have reserved semantic meanings within a URL structure: `&` separates query parameters, `=` separates keys from values, `#` denotes a fragment identifier, and spaces are not permitted in URLs. Without proper encoding, the URL parser would misinterpret these characters as structural delimiters rather than literal data, resulting in truncated or corrupted query parameter values being received by the server.

### Part 4

**Question 4.1:** You can place annotations like `@Produces(MediaType.APPLICATION_JSON)` at either the class level or the individual method level. What is the benefit of class-level placement, and how does method-level overriding work?  
**Answer:** Placing `@Produces(MediaType.APPLICATION_JSON)` at the class level establishes a default media type for all resource methods within that class, eliminating the need to repeat the annotation on every individual method. This reduces boilerplate code and ensures consistency across the entire resource. In our implementation, both `WorkspaceResource` and `ModelResource` use class-level `@Produces` annotations. If a specific method within the class needs to produce a different media type (e.g. `application/xml` or `text/plain`), it can declare its own `@Produces` annotation at the method level, which overrides the class-level default for that method only. The JAX-RS runtime always gives precedence to the most specific (method-level) annotation when both are present.

### Part 5

**Question 5.2:** HTTP status codes are categorised into classes (e.g., 2xx, 4xx, 5xx). Explain fundamentally why a validation failure caused by the user providing a non-existent workspaceId must return a 4xx code rather than a 5xx code.  
**Answer:** HTTP status code classes strictly delineate the origin of the fault. The `4xx` class (Client Error) indicates that the problem lies with the client's request — the client has submitted invalid, malformed, or logically incorrect data that the server cannot process. In contrast, the `5xx` class (Server Error) indicates that the server itself has encountered an internal failure, such as an unhandled exception, a crashed process, or an unavailable dependency. When a client provides a non-existent `workspaceId`, the server is functioning perfectly; it is the client's input that is invalid. Returning `5xx` would falsely signal that the server infrastructure is broken, misleading monitoring systems, operations teams, and the client application's error-handling logic. Our implementation correctly uses `422 Unprocessable Entity` via the `LinkedWorkspaceNotFoundMapper` for this scenario.

**Question 5.4:** If an operation throws a specific custom exception (e.g., LinkedWorkspaceNotFoundException) and you also have a global ExceptionMapper\<Throwable\>, how does the JAX-RS runtime determine which mapper to execute?  
**Answer:** The JAX-RS runtime uses a "most specific type match" resolution strategy. When an exception is thrown, the runtime examines all registered `ExceptionMapper` providers and selects the one whose generic type parameter is the closest match in the exception's class hierarchy. An `ExceptionMapper<LinkedWorkspaceNotFoundException>` is an exact type match and will always take priority over the generic `ExceptionMapper<Throwable>`, which matches all exceptions. The global `Throwable` mapper serves as a catch-all safety net that only activates for unexpected exceptions (e.g. `NullPointerException`, `ArrayIndexOutOfBoundsException`) that do not have a dedicated, more specific mapper registered with the runtime.

**Question 5.5:** In your filter, you interact with ContainerRequestContext and ContainerResponseContext. List two pieces of crucial HTTP metadata you can extract from these contexts that are highly valuable for debugging server issues.  
**Answer:**
1. **HTTP Method and Request URI** (via `requestContext.getMethod()` and `requestContext.getUriInfo().getRequestUri()`) — This combination allows developers to identify exactly which endpoint was invoked and with what path parameters, making it straightforward to reproduce and diagnose routing issues or unexpected parameter values.
2. **Response Status Code** (via `responseContext.getStatus()`) — Logged in the response filter, this immediately reveals whether the server returned a success (2xx), client error (4xx), or server error (5xx) for each request. Correlating the status code with the request method and URI provides a complete audit trail for identifying patterns of failures or unexpected behaviour.
