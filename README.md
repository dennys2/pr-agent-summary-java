# GitHub Code Review Agent

An AI-powered agent that automatically reviews GitHub Pull Requests using **Spring AI** and **OpenAI GPT-4o**.
Given a PR, the agent fetches the diff, analyses every changed file, and posts a structured code review directly on GitHub.

---

## Architecture

```
CommandLineRunner
      │
      ▼
CodeReviewAgent           ← orchestrates the agentic loop via ChatClient
      │
      ▼
ChatClient (Spring AI)    ← sends prompt + tool definitions to OpenAI
      │
      │  Model decides which tools to call and in what order
      ▼
GitHubTools (@Tool)       ← tool methods the model can invoke
      │
      ▼
GitHubClient              ← RestClient calling the GitHub REST API
      │
      ▼
GitHub API                ← reads PR info/diff, posts review
```

**Agentic flow per review:**

1. `getPullRequestInfo` — fetch title, author, branches, description
2. `getPullRequestDiff` — fetch all changed files with their patches
3. *(model analyses the code)*
4. `postReviewComment` — post the full review as a PR comment
5. Return confirmation to the CLI

---

## Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.x |
| AI | Spring AI 1.0.0 + OpenAI GPT-4o |
| HTTP client | Spring `RestClient` |
| Build | Maven |

---

## Prerequisites

- Java 21
- Maven 3.9+
- OpenAI API key (with GPT-4o access)
- GitHub personal access token with `repo` scope (to read PRs and post reviews)

---

## Configuration

Export the following environment variables before running:

```bash
export OPENAI_API_KEY=sk-...
export GITHUB_TOKEN=ghp_...

# Target PR (required)
export REVIEW_OWNER=your-org-or-user
export REVIEW_REPO=your-repo
export REVIEW_PR_NUMBER=42
```

On Windows (PowerShell):

```powershell
$env:OPENAI_API_KEY  = "sk-..."
$env:GITHUB_TOKEN    = "ghp_..."
$env:REVIEW_OWNER    = "your-org-or-user"
$env:REVIEW_REPO     = "your-repo"
$env:REVIEW_PR_NUMBER = "42"
```

---

## How to Run

```bash
# Build
mvn clean package -q

# Run
java -jar target/github-code-review-agent-0.0.1-SNAPSHOT.jar
```

Or directly via Maven:

```bash
mvn spring-boot:run
```

You can also override the target PR at runtime:

```bash
java -jar target/github-code-review-agent-0.0.1-SNAPSHOT.jar \
  --review.owner=octocat \
  --review.repo=Hello-World \
  --review.pr-number=1
```

---

## Example Output

```
============================================================
CODE REVIEW RESULT
============================================================
Review posted successfully with ID: 1987654321

Reviewed PR #42 — "Add user authentication middleware" in acme/backend.
The review covered 4 changed files (+312/-47 lines) and was posted as a
COMMENT review on the pull request.

Key findings:
- 🔴 Critical: SQL query in UserRepository.java is vulnerable to injection (line 87)
- 🟡 Warning:  Missing input validation on /login endpoint body parameters
- 🔵 Suggestion: Extract magic string "Bearer " into a constant
============================================================
```

**Review posted on GitHub:**

> ## Code Review
>
> ### Summary
> This PR adds JWT-based authentication middleware to the Express API.
>
> ### Strengths
> - Clean separation between auth logic and route handlers
> - Token expiry is correctly checked
>
> ### Issues
>
> #### 🔴 Critical
> - **`UserRepository.java:87`** — Raw string concatenation in SQL query is vulnerable to injection.
>   Use parameterised queries or a prepared statement.
>
> #### 🟡 Warning
> - **`AuthController.java:34`** — Request body is used without validation.
>   Add `@Valid` + a DTO with constraints.
>
> #### 🔵 Suggestion
> - **`JwtUtil.java:12`** — `"Bearer "` appears 4 times. Extract to a constant.
>
> ### Overall Assessment
> The feature direction is good. Address the SQL injection before merging.

---

## Project Structure

```
src/main/java/com/codereviewer/
├── CodeReviewAgentApplication.java   # @SpringBootApplication + CommandLineRunner demo
├── agent/
│   └── CodeReviewAgent.java          # orchestrates the review via ChatClient
├── config/
│   └── AgentConfig.java              # ChatClient and RestClient beans
└── tools/
    ├── GitHubTools.java               # @Tool methods exposed to the model
    └── GitHubClient.java              # low-level GitHub REST API calls
src/main/resources/
└── application.yml                    # configuration
```
