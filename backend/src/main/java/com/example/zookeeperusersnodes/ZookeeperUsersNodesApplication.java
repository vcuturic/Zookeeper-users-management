package com.example.zookeeperusersnodes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.example.zookeeperusersnodes")
@EnableScheduling
public class ZookeeperUsersNodesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZookeeperUsersNodesApplication.class, args);
	}

}
