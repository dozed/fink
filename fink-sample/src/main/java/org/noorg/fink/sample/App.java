package org.noorg.fink.sample;

import org.noorg.fink.data.repository.PageRepository;

/**
 * Hello world!
 * 
 */
public class App {
	public static void main(String[] args) {
		System.out.println("Hello World!");
		PageRepository r = null;
		r.findPageByUuid("asd");
	}
}
