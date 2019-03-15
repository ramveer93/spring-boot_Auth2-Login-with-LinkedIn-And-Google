package com.programmer.gate;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Controller
public class HomeController {

	/**
	 * okHttpClient :
	 */
	OkHttpClient okHttpClient;

	
	@RequestMapping("/")
	public String login() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println(auth.getPrincipal());
		System.out.println("login endpoint called88888888");
		return "/index";
	}
	
	
	

	@RequestMapping("/authr")
	public String authorize() {
		//Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println("calling authorize******************");
		getCall();
		return "/index";
	}
	
	
	
	
	
	@RequestMapping("/sigin-linkedin")
	public String linkedincb(@RequestParam("code") String code,@RequestParam("state") String state,ModelMap map ) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    System.out.println(auth.getPrincipal());
		System.out.println("login with linkedIn");
		System.out.println("code is : "+code+" and  state is : "+state);
		map.addAttribute("name", postCallForLinkedin(code).toString());
		return "/linkedin";
		
	}

	@RequestMapping("/callback")
	public String callback(@RequestParam("code") String code,ModelMap map) {
		System.out.println("redirecting to home page");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		System.out.println(auth.getPrincipal());
		System.out.println(auth.getDetails());
		System.out.println(auth.getName());
		System.out.println(code);
		map.addAttribute("name", postCall(code).toString());
		return "/home";
	}
	
	
	private String getCall() {
		String authUrl = "https://www.linkedin.com/oauth/v2/authorization?response_type=code&client_id=81xmu2e7ej9tav&redirect_uri=http://localhost:9090/sigin-linkedin&scope=r_liteprofile";
		Request rq = new Request.Builder().url(authUrl).get().build();
		Response rps = makeHttpCall(rq);
		String trr = null;
		try {
			trr = rps.body().string();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("error doing get call: "+e.getMessage());
		}
		System.out.println("request to profile: -----"+trr);
		return trr;
	}
	
	

	/**
	 * getHttpClient
	 *
	 * @return : OkHttpClient
	 */
	public OkHttpClient getHttpClient() {
		OkHttpClient client = null;
		if (null != this.okHttpClient) {
			return this.okHttpClient;
		}
		try {
			final TrustManager[] trustAllCerts = getTrustManager();
			final SSLContext sslContext = SSLContext.getInstance("SSL"); //$NON-NLS-1$
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
			final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
			OkHttpClient.Builder builder = new OkHttpClient.Builder();
			builder.sslSocketFactory(sslSocketFactory);
			builder.hostnameVerifier(getHostnameVerifier());
			builder.connectTimeout(200, TimeUnit.SECONDS);
			builder.writeTimeout(200, TimeUnit.SECONDS);
			builder.readTimeout(200, TimeUnit.SECONDS);
			//builder.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("sjc1intproxy01.crd.ge.com", 8080))); //$NON-NLS-1$
			client = builder.build();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return client;
	}

	private HostnameVerifier getHostnameVerifier() {
		HostnameVerifier hostnameVerifier = (hostname, session) -> true;
		return hostnameVerifier;
	}

	private TrustManager[] getTrustManager() {
		TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// accept all clients
			}

			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType)
					throws CertificateException {
				// accept all clients
			}

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new java.security.cert.X509Certificate[] {};
			}
		} };
		return trustManager;
	}
	
	/**
	 * This method makes call to external API
	 * 
	 * @param request
	 *            will be the okhttp request object
	 * @return the okhttp request
	 * @throws IOException
	 *             will be thrown if exception occurred while calling the rest
	 *             API
	 * @throws HydroPerfException 
	 */
	public Response makeHttpCall(Request request) {
		OkHttpClient client = getHttpClient();
		try {
			return client == null ? null : client.newCall(request).execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public Map<String,String> postCall(String code) {
		String content = "code="+code+"&client_id=142487995044-c8g3onptb926rvv5kuol1bu6njopi58d.apps.googleusercontent.com&client_secret=u4-NYlRU44qR0Iaqgbb1cOW8&grant_type=authorization_code&redirect_uri=http://localhost:9090/callback";
		String url = "https://www.googleapis.com/oauth2/v4/token";
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type","application/x-www-form-urlencoded");
		Headers headersbuild = Headers.of(headers);
		MediaType json = MediaType.parse("application/x-www-form-urlencoded"); 
		RequestBody body = RequestBody.create(json, content);
		Request req = new Request.Builder().url(url).post(body).headers(headersbuild).build();
		Response res = makeHttpCall(req);
		String rr = null;
		Map<String,String> map = new HashMap<>();
		try {
			rr = res.body().string();
			if(res.code()==200) {
				System.out.println("success response for token : "+rr);
				JSONObject obj = new JSONObject(rr);
				String token = obj.getString("access_token");
				String profileUrl = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json&access_token="+token;
				Map<String,String> hd = new HashMap<>();
				hd.put("Authorization", token);
				Headers hdr = Headers.of(hd);
				Request rq = new Request.Builder().url(profileUrl).get().headers(hdr).build();
				Response rps = makeHttpCall(rq);
				String trr = rps.body().string();
				System.out.println("request to profile: -----"+trr);
				map.put("object", trr);
			}else {
				System.out.println("error response:  "+rr);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return map;
		
	}
	
	public Map<String,String> postCallForLinkedin(String code) {
		String content = "grant_type=authorization_code&code="+code+"&redirect_uri=http://localhost:9090/sigin-linkedin&client_id=81xmu2e7ej9tav&client_secret=WvcjNnbBqOVYr36J";
		String url = "https://www.linkedin.com/oauth/v2/accessToken";
		Map<String, String> headers = new HashMap<>();
		headers.put("Content-Type","application/x-www-form-urlencoded");
		Headers headersbuild = Headers.of(headers);
		MediaType json = MediaType.parse("application/x-www-form-urlencoded"); 
		RequestBody body = RequestBody.create(json, content);
		Request req = new Request.Builder().url(url).post(body).headers(headersbuild).build();
		Response res = makeHttpCall(req);
		String rr = null;
		Map<String,String> map = new HashMap<>();
		try {
			rr = res.body().string();
			if(res.code()==200) {
				System.out.println("success response for token : "+rr);
				JSONObject obj = new JSONObject(rr);
				String token = obj.getString("access_token");
				String profileUrl = "https://api.linkedin.com/v2/me";
				Map<String,String> hd = new HashMap<>();
				hd.put("Authorization", "Bearer "+ token);
				Headers hdr = Headers.of(hd);
				Request rq = new Request.Builder().url(profileUrl).get().headers(hdr).build();
				Response rps = makeHttpCall(rq);
				String trr = rps.body().string();
				System.out.println("request to profile: -----"+trr);
				
				map.put("object", trr);
			}else {
				System.out.println("error response:  "+rr);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return map;
		
	}
}