package com.doc.search;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.doc.search.mapper")
public class DocSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocSearchApplication.class, args);
	}

}
