package org.em.web.handlers;

import java.util.List;
import java.util.Map;
import io.netty.handler.codec.http.multipart.FileUpload;

//import java.util.Map.Entry;

public class user {
	public static void login(Map<String, List<String>> paramap, List<String> r) {
		r.clear();

		r.add("text/plain");
		r.add("fuck someb");

	}

	/**
	 * 
	 * @param qs
	 *            query strings
	 * @param fd
	 *            form data
	 * @param fs
	 *            files
	 * @param r
	 *            return
	 */

	public static void register(Map<String, List<String>> qs,
			Map<String, List<String>> fd, List<FileUpload> fs, List<String> r) {
		r.clear();

		r.add("text/plain");
		r.add("hello body someb");
		if (fs.size() > 0) {
			FileUpload f = fs.get(0);
			
			System.out.println(f.length());
		}
		

		

	}

}
