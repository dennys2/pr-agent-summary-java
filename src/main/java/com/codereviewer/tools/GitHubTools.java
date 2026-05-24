package com.codereviewer.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GitHubTools {

    private final GitHubClient gitHubClient;

    public GitHubTools(GitHubClient gitHubClient) {
        this.gitHubClient = gitHubClient;
    }

    @Tool(description = "Fetches the list of changed files in a GitHub Pull Request, including the diff patch for each file")
    public String getPullRequestDiff(String owner, String repo, int prNumber) {
        List<Map<String, Object>> files = gitHubClient.getPullRequestFiles(owner, repo, prNumber);

        if (files == null || files.isEmpty()) {
            return "No changed files found in this pull request.";
        }

        return files.stream()
                .map(file -> {
                    String filename = (String) file.get("filename");
                    String status = (String) file.get("status");
                    String patch = (String) file.getOrDefault("patch", "(binary or no diff available)");
                    int additions = file.get("additions") instanceof Number n ? n.intValue() : 0;
                    int deletions = file.get("deletions") instanceof Number n ? n.intValue() : 0;
                    return "File: %s [%s] (+%d/-%d)\n%s".formatted(filename, status, additions, deletions, patch);
                })
                .collect(Collectors.joining("\n\n---\n\n"));
    }

    @Tool(description = "Fetches metadata about a GitHub Pull Request: title, description, author, base branch, and head branch")
    public String getPullRequestInfo(String owner, String repo, int prNumber) {
        Map<String, Object> pr = gitHubClient.getPullRequestInfo(owner, repo, prNumber);

        String title = (String) pr.get("title");
        String body = (String) pr.getOrDefault("body", "No description provided.");

        @SuppressWarnings("unchecked")
        Map<String, Object> user = (Map<String, Object>) pr.get("user");
        String author = user != null ? (String) user.get("login") : "unknown";

        @SuppressWarnings("unchecked")
        Map<String, Object> base = (Map<String, Object>) pr.get("base");
        String baseBranch = base != null ? (String) base.get("ref") : "unknown";

        @SuppressWarnings("unchecked")
        Map<String, Object> head = (Map<String, Object>) pr.get("head");
        String headBranch = head != null ? (String) head.get("ref") : "unknown";

        Object changedFiles = pr.get("changed_files");
        Object commits = pr.get("commits");
        String state = (String) pr.getOrDefault("state", "unknown");

        return """
                Title:         %s
                Author:        %s
                State:         %s
                Base Branch:   %s
                Head Branch:   %s
                Changed Files: %s
                Commits:       %s
                Description:
                %s
                """.formatted(title, author, state, baseBranch, headBranch, changedFiles, commits, body);
    }

    @Tool(description = "Posts a general review comment on a GitHub Pull Request with the code review feedback")
    public String postReviewComment(String owner, String repo, int prNumber, String body) {
        Map<String, Object> response = gitHubClient.postReview(owner, repo, prNumber, body);
        Object reviewId = response.get("id");
        return "Review posted successfully with ID: " + reviewId;
    }
}
