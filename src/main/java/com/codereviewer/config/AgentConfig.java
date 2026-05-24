package com.codereviewer.config;

import com.codereviewer.tools.GitHubTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

@Configuration
public class AgentConfig {

    @Bean
    public RestClient gitHubRestClient(@Value("${github.token}") String githubToken) {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + githubToken)
                .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
                .defaultHeader("X-GitHub-Api-Version", "2022-11-28")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder, GitHubTools githubTools) {
        return builder
                .defaultTools(githubTools)
                .build();
    }
}
