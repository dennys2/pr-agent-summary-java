package com.codereviewer;

import com.codereviewer.agent.CodeReviewAgent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CodeReviewAgentApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CodeReviewAgentApplication.class);

    private final CodeReviewAgent codeReviewAgent;

    @Value("${review.owner}")
    private String owner;

    @Value("${review.repo}")
    private String repo;

    @Value("${review.pr-number}")
    private int prNumber;

    public CodeReviewAgentApplication(CodeReviewAgent codeReviewAgent) {
        this.codeReviewAgent = codeReviewAgent;
    }

    public static void main(String[] args) {
        SpringApplication.run(CodeReviewAgentApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("Starting code review for {}/{} PR#{}", owner, repo, prNumber);

        String result = codeReviewAgent.reviewPullRequest(owner, repo, prNumber);

        System.out.println("\n" + "=".repeat(60));
        System.out.println("CODE REVIEW RESULT");
        System.out.println("=".repeat(60));
        System.out.println(result);
        System.out.println("=".repeat(60) + "\n");
    }
}
