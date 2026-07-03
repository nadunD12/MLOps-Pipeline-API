# MLOps Pipeline Management API

## Overview

The MLOps Pipeline Management API is a scalable RESTful web service. It is built with Jakarta RESTful Web Services. This is also known as JAX-RS. The API uses the Jersey framework. It runs inside a Grizzly HTTP container. This API helps AI lab data scientists. It provides an interface for automated MLOps pipelines. The pipelines can manage ML Workspaces, Machine Learning Models and Evaluation Metrics. All data is stored in memory. It uses thread-safe ConcurrentHashMap data structures. No external database is used.

## Build and Launch Instructions

### Prerequisites:

* Java JDK 17
* Apache Maven

### Steps:

**1. Clean and Compile the package**

Open your terminal in the root directory. This is where the `pom.xml` file belongs. Run:

```bash
mvn clean install
```

**2. Execute the Application Server**

Since we bundle a main class, Maven executes via `exec:java`:

```bash
mvn exec:java -Dexec.mainClass="com.mlops.Main"
```

**3. Alternatively run the fat shade JAR:**

```bash
java -jar target/mlops-api-1.0-SNAPSHOT.jar
```

The server will start. It will be available on `http://localhost:8080/api/v1/`.

## Example cURL Commands

**1. Discovery Request (Root)**
```bash
curl -X GET http://localhost:8080/api/v1/
```

**2. Create New Workspace**
```bash
curl -X POST http://localhost:8080/api/v1/workspaces \
-H "Content-Type: application/json" \
-d '{"teamName":"Computer Vision Lab", "storageQuotaGb":500}'
```

**3. Get All Workspaces**
```bash
curl -X GET http://localhost:8080/api/v1/workspaces
```

**4. Get a Specific Workspace by ID**
```bash
curl -X GET http://localhost:8080/api/v1/workspaces/<workspace-uuid-here>
```

**5. Register New Model (Replace UUID with the workspaceId from response)**
```bash
curl -X POST http://localhost:8080/api/v1/models \
-H "Content-Type: application/json" \
-d '{"framework":"PyTorch", "status":"TRAINING", "workspaceId":"<workspace-uuid-here>"}'
```

**6. Search Models by Status Filter**
```bash
curl -X GET "http://localhost:8080/api/v1/models?status=TRAINING"
```

**7. Append New Metric to Model (Replace UUID with the modelId)**
```bash
curl -X POST http://localhost:8080/api/v1/models/<model-uuid-here>/metrics \
-H "Content-Type: application/json" \
-d '{"accuracyScore": 0.94}'
```

**8. Get Evaluation Metrics History for a Model**
```bash
curl -X GET http://localhost:8080/api/v1/models/<model-uuid-here>/metrics
```

**9. Delete an Empty Workspace (successful deletion)**
```bash
curl -X DELETE http://localhost:8080/api/v1/workspaces/<workspace-uuid-here>
```

**10. Attempt to Delete a Workspace with Models (triggers 409 Conflict error)**
```bash
curl -X DELETE http://localhost:8080/api/v1/workspaces/<workspace-with-models-uuid>
```

---

## Conceptual Coursework Answers

### Part 1

**Question 1.1: Explain the role of a MessageBodyWriter or a JSON provider.**  
A MessageBodyWriter is a JAX-RS contract interface that is responsible for converting Java objects into a specific media type (such as JSON) so they can be written into an HTTP response body. When a resource method returns a Java object, the JAX-RS runtime inspects the `@Produces` annotation to determine the required media type. It then selects a registered MessageBodyWriter that supports both the Java type and the target media type. In this project, the Jackson JSON provider (`jersey-media-json-jackson`) acts as the MessageBodyWriter. It uses Java reflection to read the object's getter methods and maps them to corresponding JSON key-value pairs. For example, when a `MLWorkspace` object is returned, Jackson's MessageBodyWriter serialises its `getId()`, `getTeamName()`, and `getStorageQuotaGb()` methods into a JSON object with keys `id`, `teamName`, and `storageQuotaGb`. This automatic serialisation process removes the need for manual JSON string construction and ensures consistent output across all endpoints.

**Question 1.2: Define statelessness.**  
Statelessness is a core constraint of REST architecture. It means that each HTTP request from a client to the server must contain all the information needed to understand and process that request. The server does not store any session state or context about the client between requests. Every request is treated as an independent, self-contained unit. This design principle makes cloud APIs significantly easier to scale horizontally because any server instance in a load-balanced cluster can handle any incoming request without needing to synchronise session data with other servers. If one server goes down, the client can simply resend the same complete request to another available server without any loss of context. This eliminates the need for sticky sessions and shared session stores, greatly simplifying the deployment and operations of distributed systems.

### Part 2

**Question 2.1: Discuss HTTP Cache-Control headers.**  
HTTP Cache-Control headers are directives that instruct clients and intermediate proxies on how to cache responses. In this API, the `GET /api/v1/workspaces` endpoint sets `Cache-Control: max-age=60, public` to tell clients they can reuse the cached response for 60 seconds without contacting the server again. This improves client performance by reducing network latency, as the client can serve repeated requests from its local cache instead of making a round trip to the server. It also reduces unnecessary processing load on the server because fewer requests actually reach it. The `public` directive allows shared caches such as CDNs and proxy servers to also store the response, further distributing the load. Without these headers, clients would need to contact the server for every single request, which would increase both latency and server resource consumption.

**Question 2.2: Use the HEAD method to check if a workspace exists.**  
The HEAD method is the correct HTTP method for verifying the existence of a resource without downloading any data. It is identical to GET in terms of the request structure and the response headers and status code returned, but it deliberately omits the response body. In this API, a client can call `HEAD /api/v1/workspaces/{workspaceId}` to check if a workspace exists. If the workspace exists, the server returns an HTTP 200 OK status code with headers only. If it does not exist, the server returns HTTP 404 Not Found. This is more efficient than using GET because the client saves bandwidth by not downloading the full JSON body of the workspace. This is particularly useful in automated MLOps pipelines where a script only needs to confirm a workspace exists before proceeding with subsequent operations like registering a new model.

### Part 3

**Question 3.1: Server-side ID generation.**  
When creating a new Model via a POST request, it is best practice for the server to generate the unique `id` using `UUID.randomUUID()` rather than allowing the client to provide their own. This is important for two reasons. First, **security**: if clients could specify their own IDs, a malicious user could attempt to overwrite existing resources by submitting a request with a known or guessed ID. Server-generated UUIDs are cryptographically random and virtually impossible to predict, which prevents such enumeration and collision attacks. Second, **data integrity**: the server can guarantee that every ID is unique across the entire system. If clients were allowed to generate their own IDs, there would be a risk of duplicates, especially in distributed environments where multiple clients submit requests concurrently. By centralising ID generation on the server side, we ensure that the resource identifiers remain unique, consistent, and tamper-proof.

**Question 3.2: URL encoding.**  
When a client needs to search for a framework containing spaces or special characters, such as `?framework=Scikit Learn & Tools`, the client must URL-encode those characters before sending the request. Spaces must be encoded as `%20` (or `+` in query strings) and the ampersand `&` must be encoded as `%26`. The correctly encoded URL would be: `?framework=Scikit%20Learn%20%26%20Tools`. This encoding is necessary because certain characters have reserved meanings in the URL specification defined by RFC 3986. For example, `&` is used to separate multiple query parameters, and spaces are not permitted in URLs. Without proper encoding, the server would misinterpret `&` as a parameter delimiter and incorrectly split the value, resulting in a truncated or incorrect query. URL encoding ensures that the server receives the exact intended values and processes them correctly.

### Part 4

**Question 4.1: Class-level and method-level annotations.**  
In JAX-RS, annotations such as `@Produces(MediaType.APPLICATION_JSON)` and `@Consumes(MediaType.APPLICATION_JSON)` can be placed at either the class level or the individual method level. When placed at the class level, the annotation establishes a default media type for all resource methods within that class. This is beneficial because it reduces code duplication and ensures consistency. For example, in the `WorkspaceResource` class, placing `@Produces(MediaType.APPLICATION_JSON)` at the class level means that every GET, POST, and DELETE method will produce JSON responses without having to repeat the annotation on each method. However, if a specific method needs to support a different media type, such as returning XML or plain text, a method-level annotation can be used to override the class-level default for just that method. The JAX-RS runtime gives priority to more specific method-level annotations over the broader class-level ones. This two-tier approach provides both convenience and flexibility, allowing developers to establish sensible defaults while retaining the ability to make targeted exceptions.

### Part 5

**Question 5.2: HTTP status codes.**  
HTTP status codes are categorised into classes where each class represents a fundamentally different kind of outcome. The 4xx class indicates client errors and the 5xx class indicates server errors. A validation failure caused by the user providing a non-existent `workspaceId` must return a 4xx code (specifically HTTP 422 Unprocessable Entity in this API) because the error was caused by the client sending incorrect or invalid data. The server understood the request and processed it correctly, but the content of the request was semantically invalid. The server is functioning as intended; it is the client's responsibility to provide a valid `workspaceId`. Returning a 5xx code such as 500 Internal Server Error would be incorrect because 5xx codes indicate that the server itself has encountered an unexpected fault or bug. Sending a 5xx for a validation failure would mislead the client into thinking the server is broken, when in reality the client simply needs to correct the data and retry the request.

**Question 5.4: Exception mapping.**  
The JAX-RS runtime uses a specific type-matching algorithm to determine which exception mapper to execute when an exception is thrown. When an exception occurs during request processing, the runtime inspects all registered `ExceptionMapper` implementations and finds the one whose generic type parameter is the closest match to the actual exception class in the type hierarchy. For example, when a `LinkedWorkspaceNotFoundException` is thrown, the runtime finds the `LinkedWorkspaceNotFoundMapper` because it is an exact type match for that specific exception class. This specific mapper will always take priority over the `GlobalExceptionMapper<Throwable>`, even though `Throwable` is a superclass of all exceptions. The `GlobalExceptionMapper<Throwable>` acts as a safety net that only intercepts unexpected runtime errors, such as `NullPointerException` or `ArrayIndexOutOfBoundsException`, which do not have their own dedicated mapper. This prioritisation ensures that custom business exceptions receive their correct, specific HTTP status codes (like 409 or 422), while truly unexpected errors are caught by the global mapper and returned as HTTP 500 Internal Server Error.

**Question 5.5: In your filter you work with ContainerRequestContext and ContainerResponseContext. What are two key pieces of HTTP information you can get from these contexts that're very useful for fixing server problems?**  
* **HTTP Method and Request URI:** You can get these using `requestContext.getMethod()` and `requestContext.getUriInfo().getRequestUri()`. This information helps you know exactly which endpoint was called and with what path parameters. This makes it easy to reproduce and fix routing issues or strange parameter values.
* **Response Status Code:** You can get this using `responseContext.getStatus()`. If you log this in the response filter you can immediately see if the server returned a success, client error or server error for each request. When you look at the status code along with the request method and URI, you get a complete picture of what happened. This helps you find patterns of failures or strange behaviour.
