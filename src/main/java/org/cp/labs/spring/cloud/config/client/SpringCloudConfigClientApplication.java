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
package org.cp.labs.spring.cloud.config.client;

import java.util.Properties;

import org.cp.elements.util.PropertiesBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;

import lombok.extern.slf4j.Slf4j;

/**
 * @author John Blum
 */
@Slf4j
@SpringBootApplication
@SuppressWarnings("unused")
public class SpringCloudConfigClientApplication {

	public static void main(String[] args) {

		Properties properties = PropertiesBuilder.newInstance()
			.set("spring.application.name", "MyConfigClient")
			.set("spring.config.import", "configserver:http://localhost:8888")
			.build();

		new SpringApplicationBuilder(SpringCloudConfigClientApplication.class)
			.web(WebApplicationType.NONE)
			.properties(properties)
			.profiles("dev")
			.build()
			.run(args);
	}

	@Bean
	ApplicationRunner runner(
			@Value("${foo:mock}") String myPropertyValue,
			@Value("${my.prop:test}") String myProp) {

		return args -> {
			log.info("Foo is [{}]", myPropertyValue);
			log.info("My.Prop is [{}]", myProp);
		};
	}
}
