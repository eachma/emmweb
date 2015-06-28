package org.em.web.handlers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import io.netty.handler.codec.http.multipart.FileUpload;

import org.em.utils.*;



public class wx {
	final static String token = "Uxi95MXorla74K";

	public static void dispatch(Map<String, List<String>> paramap,
			List<String> r, String sessionID) {
		r.clear();
		if (!paramap.isEmpty()) {
			String signature = paramap.get("signature").get(0);

			String[] arr = new String[] { paramap.get("nonce").get(0),
					paramap.get("timestamp").get(0), token };
			String echostr = paramap.get("echostr").get(0);

			Arrays.sort(arr);
			
			String codes = Coding.encode(arr[0] + arr[1] + arr[2], "SHA1");
			r.add("text/plain");
			if(codes.equals(signature)){
				r.add(echostr);
				
			}else{
				r.add("fail");
			}
			
			

			return;
		}

		r.add("text/plain");
		r.add("fuck someb");

	}
	
	public static void dispatch(Map<String, List<String>> paramap,Map<String, List<String>> paramap2,
			List<FileUpload> fl,List<String> r, String sessionID,String msg) {
		r.clear();
		//final StringBuilder reply = new StringBuilder();
		//reply.append("")
		System.out.println("post in");
		System.out.println(msg);
		
		
		r.add("text/plain");
		r.add("fuck somedeb");

	}

}
