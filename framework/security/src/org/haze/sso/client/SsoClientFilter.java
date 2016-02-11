package org.haze.sso.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.haze.base.util.UtilCookie;
import org.haze.sso.SsoConsts;
import org.springframework.util.StringUtils;

public class SsoClientFilter implements Filter {

	private SsoClientConfig ssoConfig;

	Logger log = Logger.getLogger(this.getClass());

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		log.debug("SsoClientFilter.doFilter() is called!");

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		Boolean verifyResult = false;
		boolean hasTokenParamInUrl = false;

		// 1.如果request url参数中有_token，则存入本地cookie
		String _token = request.getParameter(SsoConsts.TOKEN_NAME);
		log.info("url:" + httpRequest.getRequestURL() +" param:" + httpRequest.getQueryString() + "  _token: " + _token);

		if (!StringUtils.isEmpty(_token)) {
			hasTokenParamInUrl = true;
			// 1.1
			// 代码走到这里，正常情况下，应该是sso登录成功后，重定向过来的（但也不排除，人为在url上_token=xxx，伪造登录返回）
			UtilCookie.writeCookie(httpResponse, SsoConsts.TOKEN_NAME, _token);
			// 1.2 校验_token合法性
			verifyResult = verifyToken(httpRequest, httpResponse, _token);
		} else {
			_token = UtilCookie.getCookieValue(httpRequest, SsoConsts.TOKEN_NAME);
			if (StringUtils.isEmpty(_token) || _token.equals("null")) {
				// 2.如果本机Cookie没有_token,直接重定向到sso登录页
				httpResponse.sendRedirect(getSsoUrlWithReturnUrl(httpRequest));
				return;
			} else {
				// 3.校验_token合法性
				verifyResult = verifyToken(httpRequest, httpResponse, _token);
			}
		}

		if (verifyResult) {
			if (hasTokenParamInUrl) {
				// 如果原始页面有_token参数，去掉该参数后，重定向，以排除_token的干扰
				String url = httpRequest.getRequestURL().toString();
				String param = deleteTokenParameter(httpRequest
						.getQueryString());
				String redirectUrl = url
						+ (StringUtils.isEmpty(param) ? "" : ("?" + param));
				httpResponse.sendRedirect(redirectUrl);
				return;
			} else {
				// 校验通过，则正常执行后续处理
				chain.doFilter(request, response);
			}
		} else {
			// 校验失败，重定向到登录页
			httpResponse.sendRedirect(getSsoUrlWithReturnUrl(httpRequest));
			return;
		}

	}

	public void init(FilterConfig arg0) throws ServletException {

	}

	/**
	 * 校验_token的合法性
	 * 
	 * @param request
	 * @param response
	 * @param _token
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	private boolean verifyToken(HttpServletRequest request,
			HttpServletResponse response, String _token)
			throws UnsupportedEncodingException, IOException {
		// 将_token，发送到sso-verrify地址校验
		String verifyResult = httpRequestMethodGet(ssoConfig.getVerifyUrl() + "?"
				+ SsoConsts.TOKEN_NAME + "=" + _token);

		if (verifyResult.startsWith("1,")) {
			// 校验通过，将返回值中的_token，更新本地Cookie
			// 注：如“子应用”需要保存登录名，以供其它页面使用，可在此处将userName（即：verStrings[1]）
			// 保存到Session或“子应用”自己的缓存中,但考虑到安全因素，不建议保存到客户端Cookie中
			String[] verStrings = verifyResult.split(",");
			UtilCookie.writeCookie(response, SsoConsts.TOKEN_NAME, verStrings[2]);
			return true;
		}

		return false;
	}

	private String getSsoUrlWithReturnUrl(HttpServletRequest request)
			throws UnsupportedEncodingException {

		//return "http://www.baidu.com/";

		String url = request.getRequestURL().toString();
		String param = request.getQueryString();
		String returnUrl = "";
		if (StringUtils.isEmpty(param)) {
			returnUrl = url;
		} else {
			// 去掉param中的_token参数，否则会干扰sso正常返回的_token参数
			param = deleteTokenParameter(param);
			returnUrl = url + (StringUtils.isEmpty(param) ? "" : ("?" + param));
		}
		returnUrl = URLEncoder.encode(url, "UTF-8");
		log.debug("returnUrl:" + returnUrl);
		return ssoConfig.getLoginUrl() + "?" + SsoConsts.RETURN_URL_NAME + "="
				+ returnUrl;
	}

	private String deleteTokenParameter(String params) {
		String url = params
				.replaceAll(
						"(" + SsoConsts.TOKEN_NAME + ")(=)(\\w{8}-(\\w{4}-){3}\\w{12})",
						"").replaceAll("\\?\\&", "?").replaceAll("\\&\\&", "&");
		if (url.endsWith("&") || url.endsWith("?")) {
			url = url.substring(0, url.length() - 1);
		}

		return url;

	}

	public SsoClientConfig getSsoConfig() {
		return ssoConfig;
	}

	public void setSsoConfig(SsoClientConfig ssoConfig) {
		this.ssoConfig = ssoConfig;
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}
	

    
	public  String httpRequestMethodGet(String url, String proxyHost, Integer proxyPort) {
		String response = null;
		org.apache.http.client.HttpClient client = new DefaultHttpClient();

		if (!StringUtils.isEmpty(proxyHost) && proxyHost != null) {

			HttpHost host = new HttpHost(proxyHost, proxyPort);
			client.getParams()
					.setParameter(ConnRoutePNames.DEFAULT_PROXY, host);

		}
		HttpGet httpGet = new HttpGet(url);
		try {
			HttpResponse httpResponse = client.execute(httpGet);
			log.info(httpResponse.getStatusLine().getStatusCode() + " " + url);
			response = EntityUtils.toString(httpResponse.getEntity());
			log.info(response);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("call http server fail ：" + url + "，exception："
					+ e.getClass() + "，message：" + e.getMessage());
		}
		return response;

	}

	public  String httpRequestMethodGet(String url) {
		return httpRequestMethodGet(url, null, null);
	}

}
