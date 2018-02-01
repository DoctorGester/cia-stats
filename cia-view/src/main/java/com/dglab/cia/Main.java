package com.dglab.cia;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;

import java.io.File;
import java.io.IOException;

/**
 * @author doc
 */
public class Main {
	public static void main(String[] args) {
        try {
            System.setProperty("server.ssl.key-store-password", FileUtils.readFileToString(new File("private.key")));
        } catch (IOException e) {
            e.printStackTrace();
        }

        SpringApplication.run(ViewApplication.class, args);
	}
}
