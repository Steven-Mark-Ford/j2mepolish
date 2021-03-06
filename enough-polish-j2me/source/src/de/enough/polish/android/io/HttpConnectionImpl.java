//#condition polish.android
package de.enough.polish.android.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Implements a HttpConnection, not for usage outside of this package unless you want to specify a timeout.
 * 
 * Copyright Enough Software 2008 - 2011 
 * @author Richard Nkrumah
 */
public class HttpConnectionImpl implements HttpConnection {
	
	private static final int STATE_SETUP = 0;
	
	private static final int STATE_CONNECTED = 1;
	
	private int state = STATE_SETUP;
		
	private String urlString = null;
	private String requestMethod = GET;
		
	private URL url;
	private HttpURLConnection connection = null;
	private InputStream input = null;
	private OutputStream output = null;

	private int timeout;

		
	protected HttpConnectionImpl(String url) {
		if(url == null) {
			throw new NullPointerException();
		}
		checkIsValidUrl(url);
		this.urlString = url;
		try {
			this.url = new URL(url);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("invalid URL");
		} 
	}
	
	HttpConnectionImpl(String url, int mode) {
		this(url);
	}

	
	protected void checkIsValidUrl(String checkUrl) {
		if (checkUrl.indexOf(Connector.HTTP_PREFIX) != 0 
				|| (checkUrl.length() <= Connector.HTTP_PREFIX.length()) ) 
		{
			throw new IllegalArgumentException("invalid URL " + checkUrl);
		}
	}
	
	public void setTimeout( int timeout ) {
		this.timeout = timeout;
	}
	
	public void close() throws IOException {
		Exception exception = null;
		if (this.connection != null) {
			try {
				this.connection.disconnect();
			} catch (Exception e) {
				exception = e;
				//#debug error
				exception.printStackTrace();
			}
		}
		if(this.input != null) {
			try {
				this.input.close();
			} catch (Exception e) {
				exception = e;
				//#debug error
				exception.printStackTrace();
			}
		}
		if(this.output != null) {
			try {
				this.output.close();
			} catch (Exception e) {
				exception = e;
				//#debug error
				exception.printStackTrace();
			}
		}
		if (exception instanceof IOException) {
			throw (IOException) exception;
		} else if (exception != null) {
			//#debug error
			exception.printStackTrace();
			throw new IOException(exception.toString());
		}
	}

	public DataOutputStream openDataOutputStream() throws IOException {
		return new DataOutputStream(openOutputStream());
	}

	public OutputStream openOutputStream() throws IOException {
		if(this.output != null) {
			throw new IOException("already opened");
		}
		connect();
		this.connection.setDoOutput(true);
		this.output = this.connection.getOutputStream();
		return this.output;
	}
	
	
	//only in setup state
	public void setRequestMethod(String requestMethod) throws IOException {
		if (this.state == STATE_SETUP) {
			if (requestMethod.equals(GET)) {
				this.requestMethod = requestMethod;
			} else if (requestMethod.equals(POST)) {
				this.requestMethod = requestMethod;
			} else {
				throw new IllegalArgumentException("illegal request method " + requestMethod);
			}
		} else {
			throw new IOException("already connected");
		}
	}
	
	public void setRequestProperty(String key, String value) throws IOException {
		connect();
		this.connection.setRequestProperty(key, value);
	}
	
	//invoke at any time
	public String getRequestMethod() {
		return this.requestMethod;
	}
	
	public String getRequestProperty(String key) {
		try {
			connect();
		} catch (IOException e) {
			return "";
		}
		return this.connection.getRequestProperty(key);
	}
	
	public String getURL() {
		return this.urlString;
	}
	
	public String getQuery() {
		return this.url.getQuery();
	}
	
	public int getPort() {
		return this.url.getPort();
	}
	
	public String getHost() {
		return this.url.getHost();
	}
	
	public String getProtocol() {
		return this.url.getProtocol();
	}
		
	public String getFile() {
		return this.url.getFile();
	}
		
	public String getRef() {
		return this.url.getRef();
	}
	
	//these calls force transition to connected state
	public DataInputStream openDataInputStream() throws IOException {
		return new DataInputStream(openInputStream());
	}

	public InputStream openInputStream() throws IOException {
		connect();
		if (this.connection.getResponseCode() >= 400) {
			this.input = this.connection.getErrorStream();
		} else {
			this.input = this.connection.getInputStream();
		}
		return this.input;
	}
	
	public long getLength() {
		try {
			connect();
		} catch (IOException ex) {
			return 0;
		}
		return this.connection.getContentLength();
	}
	
	public String getType() {
		try {
			connect();
		} catch (IOException ex) {
			return "";
		}
		return this.connection.getContentType();
	}
	
	public String getEncoding() {
		try {
			connect();
		} catch (IOException ex) {
			return "";
		}
		return this.connection.getContentEncoding();
	}
	
	public String getHeaderField(String name) throws IOException {
		connect();
		return this.connection.getHeaderField(name);
	}
	
	public String getHeaderField(int n) throws IOException {
		connect();
		return this.connection.getHeaderField(n);
	}
	
	public int getResponseCode()  throws IOException {
		connect();
		int responseCode = this.connection.getResponseCode();
		return responseCode;
	}
	
	public String getResponseMessage() throws IOException {
		connect();
		return this.connection.getResponseMessage();
	}
	
	public int getHeaderFieldInt(String name, int def) throws IOException {
		connect();
		return this.connection.getHeaderFieldInt(name, def);
	}

	public long getHeaderFieldDate(String name,	long def) throws IOException {
		connect();
		return this.connection.getHeaderFieldDate(name, def);
	}

	public String getHeaderFieldKey(int n) throws IOException {
		connect();
		return this.connection.getHeaderFieldKey(n);
	}
	
	public long getDate() throws IOException {
		connect();
		return this.connection.getDate();
	}
	
	public long getExpiration() throws IOException {
		connect();
		return this.connection.getExpiration();
	}
	
	public long getLastModified() throws IOException {
		connect();
		return this.connection.getLastModified();
	}
	
	protected synchronized void connect() throws IOException {
		if (this.state == STATE_CONNECTED) {
			if(this.connection == null) {
				throw new IOException("Invalid State. No connection in state STATE_CONNECTED");
			}
			return;
		} else {
			this.state = STATE_CONNECTED;
		}
	//	//#if polish.android.http.keepAlive != true
	//		//#= System.setProperty("http.keepAlive", "false");
	//	//#endif
		this.connection = (HttpURLConnection)this.url.openConnection();
		if (this.timeout > 0) {
			this.connection.setConnectTimeout(this.timeout);
			this.connection.setReadTimeout(this.timeout);
		}
		this.connection.setRequestMethod(this.requestMethod);
	}
	
}	
