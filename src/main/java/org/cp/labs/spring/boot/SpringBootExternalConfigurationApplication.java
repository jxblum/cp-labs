/*
 * Copyright 2017-Present Author or Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cp.labs.spring.boot;

import java.util.Properties;

import org.cp.elements.util.PropertiesBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

import lombok.extern.slf4j.Slf4j;

/**
 * @author John Blum
 */
@Slf4j
@SpringBootApplication
@Profile("external-configuration-test")
@SuppressWarnings("unused")
public class SpringBootExternalConfigurationApplication {

	public static void main(String[] args) {

		Properties programProperties = PropertiesBuilder.newInstance()
			//.set("spring.config.name", "my")
			//.set("spring.config.location", "file:./conf/")
			//.set("spring.config.location", "optional:file:./conf/")
			//.set("spring.config.additional-location", "file:./conf/")
			//.set("spring.config.additional-location", "file:./conf/,file:./config/my.properties")
			.set("program.property", "PROGRAM_PROPERTIES")
			.set("classpath.app.property", "PROGRAM_PROPERTIES")
			.set("classpath.app.profile.property", "PROGRAM_PROPERTIES")
			.set("classpath.config.app.property", "PROGRAM_PROPERTIES")
			.set("add.app.property", "PROGRAM_PROPERTIES")
			.set("imported.add.app.property", "PROGRAM_PROPERTIES")
			.set("environment.app.property", "PROGRAM_PROPERTIES")
			.set("system.app.property", "PROGRAM_PROPERTIES")
			.set("my.external.property", "PROGRAM_PROPERTIES")
			.build();

		new SpringApplicationBuilder(SpringBootExternalConfigurationApplication.class)
			.profiles("external-configuration-test")
			.properties(programProperties)
			.web(WebApplicationType.NONE)
			.build()
			.run(args);
	}

	@Bean
	ApplicationRunner programRunner(
			@Value("${default.property:DEFAULT_VALUE}") String defaultProperty,
			@Value("${program.property:DEFAULT_VALUE}") String programProperty,
			@Value("${classpath.app.property:DEFAULT_VALUE}") String classpathAppProperty,
			@Value("${classpath.app.profile.property:DEFAULT_VALUE}") String classpathAppProfileProperty,
			@Value("${classpath.config.app.property:DEFAULT_VALUE}") String classpathConfigAppProperty,
			@Value("${add.app.property:DEFAULT_VALUE}") String addAppProperty,
			@Value("${imported.add.app.property:DEFAULT_VALUE}") String importedAddAppProperty,
			@Value("${environment.app.property:DEFAULT_VALUE}") String environmentAppProperty,
			@Value("${system.app.property:DEFAULT_VALUE}") String systemAppProperty,
			@Value("${my.external.property:DEFAULT_VALUE}") String myExternalProperty) {

		return args -> {
			log.info("default.property is [{}]", defaultProperty);
			log.info("program.property is [{}]", programProperty);
			log.info("classpath.app.property is [{}]", classpathAppProperty);
			log.info("classpath.config.app.property is [{}]", classpathConfigAppProperty);
			log.info("classpath.app.profile.property is [{}]", classpathAppProfileProperty);
			log.info("add.app.property is [{}]", addAppProperty);
			log.info("imported.add.app.property is [{}]", importedAddAppProperty);
			log.info("environment.app.property is [{}]", environmentAppProperty);
			log.info("system.app.property is [{}]", systemAppProperty);
			log.info("my.external.property is [{}]", myExternalProperty);
		};
	}
}
