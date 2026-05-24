package com.codereviewer.agent;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
public class CodeReviewAgent {

    private static final String SYSTEM_PROMPT = """
            You are an expert software engineer performing a thorough code review on a GitHub Pull Request.

            Your review must cover these dimensions:
            1. **Correctness** — logic errors, off-by-one bugs, wrong conditionals, edge cases not handled
            2. **Security** — SQL/command injection, exposed secrets, unsafe deserialization, OWASP Top 10
            3. **Performance** — inefficient algorithms, N+1 queries, unnecessary object creation, blocking I/O
            4. **Readability** — naming conventions, overly complex expressions, magic numbers/strings
            5. **Best Practices** — SOLID principles, DRY violations, proper error handling, resource leaks
            6. **Maintainability** — missing docs on complex logic, high coupling, fragile tests

            Workflow you MUST follow:
            1. Call getPullRequestInfo to understand the purpose, author, and branches involved
            2. Call getPullRequestDiff to read every changed file and its patch
            3. Analyse all changes carefully before writing anything
            4. Compose a detailed markdown review:
               - Start with a short **Summary** of what the PR does
               - List **Strengths** (what was done well)
               - List **Issues** grouped by severity: 🔴 Critical, 🟡 Warning, 🔵 Suggestion
               - End with an **Overall Assessment**
            5. Call postReviewComment to post the review to the PR
            6. Return a brief confirmation of what you posted

            Be specific and actionable. Reference file names and explain *why* something is an issue.
            Never post an empty or placeholder review.
            """;

    private final ChatClient chatClient;

    public CodeReviewAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public String reviewPullRequest(String owner, String repo, int prNumber) {
        String userMessage = """
                Please review the following GitHub Pull Request and post a detailed code review:

                - Owner:      %s
                - Repository: %s
                - PR Number:  #%d

                Fetch the PR info, analyse the diff, write a comprehensive review, post it as a comment, \
                then return a brief summary of what you reviewed and posted.
                """.formatted(owner, repo, prNumber);

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(userMessage)
                .call()
                .content();
    }
}
