package com.taozi.utils;

import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiGetJsapiTicketRequest;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGetJsapiTicketResponse;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.taobao.api.ApiException;
import com.taozi.entity.DingConfig;
import com.taozi.entity.SignResultVO;
import com.taozi.entity.SignVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

/**
 * 钉钉utils
 *
 * @author taozi - 2021年8月9日, 009 - 11:10:35
 */
@Slf4j
public class DingUtils {

	/**
	 * 获取access_token
	 *
	 * @return String access_token
	 */
	public static String getAccessToken() {
		DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/gettoken");
		OapiGettokenRequest request = new OapiGettokenRequest();
		request.setAppkey(DingConfig.APP_KEY);
		request.setAppsecret(DingConfig.APP_SECREY);
		request.setHttpMethod("GET");
		OapiGettokenResponse response = null;
		try {
			response = client.execute(request);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		String body = response.getBody();
		JSONObject jsonObject = JSONObject.parseObject(new String(body));
		String accessToken = jsonObject.getString("access_token");
		log.info("access_token： " + accessToken);
		return accessToken;
	}

	/**
	 * 获取ticket
	 * @param accessToken
	 * @return String ticket
	 */
	public static String getTicket(String accessToken) {
		DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/get_jsapi_ticket");
		OapiGetJsapiTicketRequest req = new OapiGetJsapiTicketRequest();
		req.setHttpMethod("GET");
		OapiGetJsapiTicketResponse rsp = null;
		try {
			rsp = client.execute(req, accessToken);
		} catch (ApiException e) {
			e.printStackTrace();
		}
		String body = rsp.getBody();
		JSONObject obj = JSONObject.parseObject(body);
		String ticket = "";
		if (obj != null) {
			ticket = obj.getString("ticket");
		}
		log.info(ticket);
		return ticket;
	}

	/**
	 * 获取签名
	 * @param data
	 * @return sign
	 */
	public static SignResultVO getSign(@RequestBody SignVO data) {
		SignResultVO signResultVO = new SignResultVO();
		try {
			String plain = "jsapi_ticket=" + data.getJsTicket() + "&noncestr=" + data.getNonceStr() + "&timestamp=" + String.valueOf(data.getTimeStamp())+ "&url=" + decodeUrl(data.getUrl());
			MessageDigest sha1 = MessageDigest.getInstance("SHA-256");
			sha1.reset();
			sha1.update(plain.getBytes("UTF-8"));
			String sign = byteToHex(sha1.digest());
			// 拷贝vo
			signResultVO.setAgentId(DingConfig.AGENT_ID);
			signResultVO.setCorpId(DingConfig.CORP_ID);
			signResultVO.setNonceStr(data.getNonceStr());
			signResultVO.setTimeStamp(DateUtils.current());
			signResultVO.setSignature(sign);
			log.info(signResultVO.toString());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return signResultVO;

		// return com.alibaba.fastjson.JSON.toJSONString(data);
	}

	private static String byteToHex(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		String result = formatter.toString();
		formatter.close();
		return result;
	}

	private static String decodeUrl(String url) throws Exception {
		URL hurler = new URL(url);
		StringBuilder urlBuffer = new StringBuilder();
		urlBuffer.append(hurler.getProtocol());
		urlBuffer.append(":");
		if (hurler.getAuthority() != null && hurler.getAuthority().length() > 0) {
			urlBuffer.append("//");
			urlBuffer.append(hurler.getAuthority());
		}
		if (hurler.getPath() != null) {
			urlBuffer.append(hurler.getPath());
		}
		if (hurler.getQuery() != null) {
			urlBuffer.append('?');
			urlBuffer.append(URLDecoder.decode(hurler.getQuery(), "utf-8"));
		}
		return urlBuffer.toString();
	}
}
