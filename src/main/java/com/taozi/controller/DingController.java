package com.taozi.controller;

import com.taozi.entity.SignResultVO;
import com.taozi.entity.SignVO;
import com.taozi.utils.DateUtils;
import com.taozi.utils.DingUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * 钉钉鉴权接口
 *
 * @author TAOZI
 */
@Api(tags = "对接钉钉接口")
@RestController
public class DingController {

	/**
	 * 一键获取签名
	 *
	 * @return String sign
	 */
	@ApiOperation("鉴权（一键式）：获取签名")
	@GetMapping("/directlySign")
	public SignResultVO directlySign() {
		String accessToken = getAccessToken();
		String ticket = getTicket(accessToken);
		SignVO data = new SignVO();
		data.setJsTicket(ticket);
		data.setTimeStamp(DateUtils.current());
		data.setNonceStr(UUID.randomUUID().toString().replace("-", ""));
		// 需要鉴权的url
		data.setUrl("http://172.16.7.217:8080/#/textdd");
		return getSign(data);
	}

	/**
	 * 获取access_token
	 *
	 * @return String access_token
	 */
	@ApiOperation("鉴权（分步）1：获取access_token")
	@GetMapping("/getAccessToken")
	public String getAccessToken() {
		return DingUtils.getAccessToken();
	}

	/**
	 * 获取ticket
	 *
	 * @param accessToken
	 * @return String ticket
	 */
	@ApiOperation("鉴权（分步）2：获取ticket")
	@GetMapping("/getTicket")
	public String getTicket(String accessToken) {
		return DingUtils.getTicket(accessToken);
	}

	/**
	 * 获取签名
	 *
	 * @param data
	 * @return sign
	 */
	@ApiOperation("鉴权（分布）3：获取签名")
	@PostMapping(value = "/getSign", produces = "text/html;charset=UTF-8")
	@ResponseBody
	public SignResultVO getSign(@RequestBody SignVO data) {
		return DingUtils.getSign(data);
	}
}
