package ru.hse.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.hse.pos.adapter.csv.CsvResultsExporter;
import ru.hse.pos.adapter.memory.InMemoryGradebookRepository;
import ru.hse.pos.application.PosFacade;
import ru.hse.pos.application.ResultsExporter;
import ru.hse.pos.domain.GradebookRepository;
import ru.hse.pos.domain.ResultCalculator;
import ru.hse.pos.domain.ScoringStrategy;
import ru.hse.pos.domain.WeightedScoring;

@Configuration(proxyBeanMethods = false)
public class AppConfig {
    @Bean
    GradebookRepository gradebookRepository() {
        return new InMemoryGradebookRepository();
    }

    @Bean
    ResultsExporter resultsExporter() {
        return new CsvResultsExporter();
    }

    @Bean
    ScoringStrategy scoringStrategy() {
        return new WeightedScoring();
    }

    @Bean
    ResultCalculator resultCalculator(ScoringStrategy strategy) {
        return new ResultCalculator(strategy);
    }

    @Bean
    PosFacade posFacade(GradebookRepository repository, ResultCalculator calculator, ResultsExporter exporter) {
        return new PosFacade(repository, calculator, exporter);
    }
}
