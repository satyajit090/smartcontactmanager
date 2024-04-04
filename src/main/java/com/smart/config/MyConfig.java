package com.smart.config;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.WebInvocationPrivilegeEvaluator;

import jakarta.servlet.Filter;

@Configuration
@EnableWebSecurity
public class MyConfig {
	
    @Bean
	public UserDetailsService getUserDetailsService() {
		return new UserDetailsServiceImp();
	}

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
    	return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
    	DaoAuthenticationProvider daoAuthenticationProvider=new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(this.getUserDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        
        return  daoAuthenticationProvider;
    }
    
    
    
    //Configuation menthods..................
    
    protected void configure(AuthenticationManagerBuilder auth) throws Exception{
    	
    	auth.authenticationProvider(authenticationProvider());
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    	http.authorizeRequests(authorize->authorize.requestMatchers("/admin/**").hasRole("ADMIN")
    	.requestMatchers("/user/**").hasRole("USER")
    	.requestMatchers("/**").permitAll())
    	.formLogin().
    	loginPage("/signin")
    	.loginProcessingUrl("/dologin")
    	.defaultSuccessUrl("/user/index")
    	.failureUrl("/signin")
    	.and().csrf().disable();
   	 return http.build();
   }

	
}
