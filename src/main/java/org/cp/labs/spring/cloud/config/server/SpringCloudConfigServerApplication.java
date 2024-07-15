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
package org.cp.labs.spring.cloud.config.server;

import java.util.Properties;

import org.cp.elements.util.PropertiesBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * @author John Blum
 */
@SpringBootApplication
@EnableConfigServer
public class SpringCloudConfigServerApplication {

	public static final int CONFIG_SERVER_PORT = 8888;

	public static void main(String[] args) {

		Properties properties = PropertiesBuilder.newInstance()
			.set("spring.application.name", "MyConfigServer")
			.set("server.port", String.valueOf(CONFIG_SERVER_PORT))
			//.set("spring.cloud.config.server.git.uri", "https://github.com/spring-cloud-samples/config-repo")
			.set("spring.cloud.config.server.native.searchLocations", "file:./conf/")
			.set("my.prop", "PROGRAM")
			.build();

		//SpringApplication.run(SpringCloudConfigClientApplication.class, args);

		new SpringApplicationBuilder(SpringCloudConfigServerApplication.class)
			.profiles("native")
			.properties(properties)
			.build()
			.run(args);
	}
}
