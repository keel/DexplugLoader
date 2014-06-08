/*
 * base64
 */
extern "C" {
/*****************************************************
 * OrgString 待编码字符串指针
 * Base64String 保存编码结果字符串指针
 * OrgStringLen 待编码字符串长度
 *****************************************************/
int Base64Encode(unsigned char *OrgString, unsigned char *Base64String,
		int OrgStringLen);
/******************************************************
 * OrgString 保存解码结果字符串指针
 * Base64String 待解码字符串指针
 * Base64StringLen 待解码字符串长度
 * bForceDecode 当待解码字符串长度错误时,是否强制解码
 * true 强制解码
 * false 不强制解码
 ******************************************************/
int Base64Decode(unsigned char *OrgString, unsigned char *Base64String,
		int Base64StringLen, bool bForceDecode);

char GetBase64Value(unsigned char ch);
}

