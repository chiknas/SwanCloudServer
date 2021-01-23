package com.chiknas.swancloudserver;

import com.chiknas.swancloudserver.services.DirectoryWatcherService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SwancloudserverApplication {

	public static void main(String[] args) {
		ConfigurableApplicationContext run = SpringApplication.run(SwancloudserverApplication.class, args);
		new Thread(run.getBean(DirectoryWatcherService.class), "watcher-service").start();
	}

}
