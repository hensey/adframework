package com.astel.security.config;

import com.astel.security.services.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import nl.ctrlaltdev.harbinger.DefaultHarbingerContext;
import nl.ctrlaltdev.harbinger.HarbingerContext;
import nl.ctrlaltdev.harbinger.evidence.EvidenceCollector;
import nl.ctrlaltdev.harbinger.filter.BlacklistFilter;
import nl.ctrlaltdev.harbinger.filter.HttpEvidenceFilter;
import nl.ctrlaltdev.harbinger.response.ResponseDecider;
import nl.ctrlaltdev.harbinger.response.SimpleResponseDecider;
import nl.ctrlaltdev.harbinger.rule.DetectionRule;
import nl.ctrlaltdev.harbinger.rule.DetectionRuleLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;

import java.util.Set;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    @ConditionalOnMissingBean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @ConditionalOnMissingBean
    public HarbingerContext harbingerContext() {
        EvidenceCollector collector = new EvidenceCollector();
        ResponseDecider decider = new SimpleResponseDecider(collector);
        Set<DetectionRule> rules = new DetectionRuleLoader().load();
        return new DefaultHarbingerContext(rules, collector, decider);
    }

    @Bean
    @ConditionalOnMissingBean
    public BlacklistFilter blacklistFilter(HarbingerContext ctx) {
        return new BlacklistFilter(ctx);
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpEvidenceFilter httpEvidenceFilter(HarbingerContext ctx) {
        return new HttpEvidenceFilter(ctx);
    }

    @Bean
    @ConditionalOnMissingBean
    public DaoAuthenticationProvider createDaoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        return provider;
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
                .ignoring()
                .antMatchers("/resources/**"); // #3
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        super.configure(http);
        http
                //filter
                .addFilterBefore(httpEvidenceFilter(harbingerContext()), ExceptionTranslationFilter.class)
                .addFilterBefore(blacklistFilter(harbingerContext()), ChannelProcessingFilter.class);
    }
}
