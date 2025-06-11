package com.swfinal.login;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.swfinal.login.mapper.RestLoginMapper;
import com.swfinal.util.EncryptUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RestLoginService {
	
	RestLoginMapper restLoginMapper;
	EncryptUtil encryptUtil;
	
	public RestLoginService(RestLoginMapper restLoginMapper, EncryptUtil encryptUtil) {
		this.restLoginMapper = restLoginMapper;
		this.encryptUtil = encryptUtil;
	}
	

	@Transactional(readOnly = true)
	public Map<String, Object> requestLogin(Map<String, Object> param) {
		
		Map<String, Object> resultMap = new HashMap<String, Object>();
		
		try {
			
			
			resultMap.put("REPL_CD", "0000");
			resultMap.put("REPL_MSG", "로그인 정상");
			resultMap.put("REPL_PAGE_MSG", "로그인이 완료되었습니다.");
			
			
			String userId = (String) param.get("userId");
			String userPw = (String) param.get("userPw");
			
			if (userId == null || userId.isEmpty()) {
				
				throw new RestLoginException("1001", "아이디 파라미터 누락", "아이디를 입력해주세요.");
			}
			log.info("아이디 유효성 체크 : {}", (String) param.get("userId"));
			
			
			if (userPw == null || userPw.isEmpty()) {
				
				throw new RestLoginException("1002", "비밀번호 파라미터 누락", "비밀번호를 입력해주세요.");
			}
			log.info("비밀번호 유효성 체크 : {}", (String) param.get("userPw"));
			
			// 회원 정보 조회
			Map<String, Object> memberInfo = restLoginMapper.selectMemberInfo(param);
			// log.info("회원정보 확인 : {}", memberInfo);
			
			// 아이디가 있는지 여부 체크
			if (memberInfo == null) {
				throw new RestLoginException("1003", "회원정보 없음", "회원정보가 없습니다.");
			}
			
			
			String dbPw = (String) memberInfo.get("PW"); 
			
			String encPw = (String) encryptUtil.encryptSha256(userPw); 
			if (!dbPw.equals(encPw)) {
				throw new RestLoginException("1004", "비밀번호 틀림", "비밀번호가 일치하지 않습니다.");
			}
			
		
			resultMap.put("memberInfo", memberInfo);
			
			log.info("로그인 처리 완료");
			
		}
		catch(RestLoginException rex) {
			resultMap.put("REPL_CD", rex.getReplCd());
			resultMap.put("REPL_MSG", rex.getReplMsg());
			resultMap.put("REPL_PAGE_MSG", rex.getReplPageMsg());
			log.error(rex.getReplMsg());
		}
		catch(Exception ex) {
			resultMap.put("REPL_CD", "9999");
			resultMap.put("REPL_MSG", "알 수 없는 에러");
			resultMap.put("REPL_PAGE_MSG", "알 수 없는 오류가 발생하였습니다.");
		}
		
		
		return resultMap;
		
	} 
}
