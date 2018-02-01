package com.dglab.cia;

import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;

import java.io.File;
import java.io.IOException;

/**
 * @author doc
 */
public class Main {
    public static boolean isBar() {
        System.out.println("isbar");

        return true;
    }

    public static boolean isFoo() {
        System.out.println("isfoo");

        return true;
    }

    public static boolean isBar2() {
        System.out.println("isbar2");

        return true;
    }

    public static boolean isFoo2() {
        System.out.println("isfoo2");

        return true;
    }

    public static void main(String[] args) {
        if (isBar() || isFoo()) {
            System.out.println("isdog");
        }

        SpringApplication.run(ViewApplication.class, args);
	}
}
