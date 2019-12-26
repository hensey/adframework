package com.astel.security.main;

import com.astel.security.dao.UserRepository;
import com.astel.security.services.UserDetailsServiceImpl;
import nl.ctrlaltdev.harbinger.DefaultHarbingerContext;
import nl.ctrlaltdev.harbinger.HarbingerContext;
import nl.ctrlaltdev.harbinger.evidence.EvidenceCollector;
import nl.ctrlaltdev.harbinger.filter.BlacklistFilter;
import nl.ctrlaltdev.harbinger.filter.HttpEvidenceFilter;
import nl.ctrlaltdev.harbinger.response.ResponseDecider;
import nl.ctrlaltdev.harbinger.response.SimpleResponseDecider;
import nl.ctrlaltdev.harbinger.rule.DetectionRule;
import nl.ctrlaltdev.harbinger.rule.DetectionRuleLoader;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.sql.DataSource;
import java.util.Set;

//@Configuration
//@ConditionalOnClass(WebSecurityConfigurerAdapterImpl.class)
public class SecurityFrameworkDefaultContext {
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

        //provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(bCryptPasswordEncoder());
        return provider;
    }

//    @Bean
//    @ConditionalOnMissingBean
//    public UserDetailsServiceImpl userDetailsService(){
//        return new UserDetailsServiceImpl(userRepository());
//    }
//
//    @Bean
//    @ConditionalOnMissingBean
//    public UserDetailsServiceImpl userRepository(){
//        return new UserDetailsServiceImpl(userRepository());
//    }

}
