package com.codereviewer.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Component
public class GitHubClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubClient.class);

    private final RestClient restClient;

    public GitHubClient(RestClient gitHubRestClient) {
        this.restClient = gitHubRestClient;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getPullRequestFiles(String owner, String repo, int prNumber) {
        try {
            return restClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{prNumber}/files", owner, repo, prNumber)
                    .retrieve()
                    .body(List.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch PR files for {}/{} PR#{}", owner, repo, prNumber, e);
            throw new RuntimeException("Failed to fetch PR files: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getPullRequestInfo(String owner, String repo, int prNumber) {
        try {
            return restClient.get()
                    .uri("/repos/{owner}/{repo}/pulls/{prNumber}", owner, repo, prNumber)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            log.error("Failed to fetch PR info for {}/{} PR#{}", owner, repo, prNumber, e);
            throw new RuntimeException("Failed to fetch PR info: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> postReview(String owner, String repo, int prNumber, String body) {
        try {
            Map<String, String> requestBody = Map.of(
                    "body", body,
                    "event", "COMMENT"
            );
            return restClient.post()
                    .uri("/repos/{owner}/{repo}/pulls/{prNumber}/reviews", owner, repo, prNumber)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);
        } catch (RestClientException e) {
            log.error("Failed to post review for {}/{} PR#{}", owner, repo, prNumber, e);
            throw new RuntimeException("Failed to post review: " + e.getMessage(), e);
        }
    }
}
