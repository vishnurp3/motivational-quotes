# Motivational Quotes

This project is a small Java application that generates motivational quotes using the ChatGPT API.
If the API does not return a usable quote or is unavailable, the application falls back to reading quotes from a file on the classpath.

The goal of the project is to demonstrate a simple, reliable wrapper around the ChatGPT API with a deterministic fallback.

---

## How it works

At runtime, the application attempts to generate a quote using ChatGPT.
If the generated response is empty, invalid, or an error occurs, a fallback generator reads a random quote from a text file packaged with the application.

The caller interacts only with a single service interface and is unaware of whether the quote came from the API or the fallback.

---

## Architecture

<img src="docs/architecture.png" alt="Quote Generation Architecture" width="800">

**Flow explanation:**

1. `QuoteService` requests a quote from the configured `QuoteGenerator`.
2. The primary generator calls the ChatGPT API.
3. If the primary generator fails or returns an unusable quote, the fallback generator is used.
4. The fallback reads a random quote from a classpath file.
5. If both fail, an error is raised.

---

## Project structure (high level)

* **Domain**

    * Core interfaces such as `QuoteGenerator` and `QuoteRepository`
* **Application**

    * Generator composition and orchestration
* **Infrastructure**

    * ChatGPT API client implementation
    * Classpath-based quote repository
* **App**

    * Entry point for running the application

---

## Prerequisites

* Java 21
* Maven
* A valid ChatGPT API key

---

## Configuration

The ChatGPT API key must be provided as an environment variable:

```
OPENAI_API_KEY
```

The application reads this value at startup.
If the key is missing or invalid, the fallback mechanism will be triggered at runtime.

---

## Running the application

This is a standard Maven-based Java project.

* To run unit and integration tests, use your usual Maven test workflow.
* To invoke the ChatGPT API and see the full flow in action, run the `App` class.
* Quotes used for fallback are stored in a text file on the classpath (`quotes.txt`).

---

## Continuous Integration

The project uses GitHub Actions to run the test suite on every push and pull request to the main branch.
