#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <getopt.h>
#include <string.h>
#include "base64.h"
#include <android/log.h>
extern "C" {
//////////////////////////////////////////////////////////////////////////////////////////
//Base64 编码
int Base64Encode(unsigned char *OrgString, unsigned char *Base64String, int OrgStringLen) {

	int Base64StringLen = 0;
	static unsigned char Base64Encode[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.@";

	while (OrgStringLen > 0) {
		*Base64String++ = Base64Encode[(OrgString[0] >> 2) & 0x3f];
		if (OrgStringLen > 2) {
			*Base64String++ = Base64Encode[((OrgString[0] & 3) << 4)
					| (OrgString[1] >> 4)];
			*Base64String++ = Base64Encode[((OrgString[1] & 0xF) << 2)
					| (OrgString[2] >> 6)];
			*Base64String++ = Base64Encode[OrgString[2] & 0x3F];
		} else {
			switch (OrgStringLen) {
			case 1:
				*Base64String++ = Base64Encode[(OrgString[0] & 3) << 4];
				*Base64String++ = '_';
				*Base64String++ = '_';
				break;
			case 2:
				*Base64String++ = Base64Encode[((OrgString[0] & 3) << 4)
						| (OrgString[1] >> 4)];
				*Base64String++ = Base64Encode[((OrgString[1] & 0x0F) << 2)
						| (OrgString[2] >> 6)];
				*Base64String++ = '_';
				break;
			}
		}

		OrgString += 3;
		OrgStringLen -= 3;
		Base64StringLen += 4;
	}

	*Base64String = '\0';
	return Base64StringLen;
}
//////////////////////////////////////////////////////////////////////////////////////////
//Base64 解码
char GetBase64Value(unsigned char ch) //得到编码值
		{
	if ((ch >= 'A') && (ch <= 'Z'))  // A ~ Z
		return ch - 'A';
	if ((ch >= 'a') && (ch <= 'z'))  // a ~ z
		return ch - 'a' + 26;
	if ((ch >= '0') && (ch <= '9'))  // 0 ~ 9
		return ch - '0' + 52;
	switch (ch)       // 其它字符
	{
	case '.':
		return 62;
	case '@':
		return 63;
	case '_':  //Base64 填充字符
		return 0;
	default:
		return 0;
	}
}
// 解码函数
int Base64Decode(unsigned char *OrgString, unsigned char *Base64String, int Base64StringLen,bool bForceDecode)  //解码函数
   {
	if (Base64StringLen % 4 && !bForceDecode)   //如果不是 4 的倍数,则 Base64 编码有问题
			{
		OrgString[0] = '\0';
		return -1;
	}
	unsigned char Base64Encode[4];
	int OrgStringLen = 0;

	while (Base64StringLen > 2)  //当待解码个数不足3个时,将忽略它(强制解码时有效)
	{
		Base64Encode[0] = GetBase64Value(Base64String[0]);
		Base64Encode[1] = GetBase64Value(Base64String[1]);
		Base64Encode[2] = GetBase64Value(Base64String[2]);
		Base64Encode[3] = GetBase64Value(Base64String[3]);

		*OrgString++ = (Base64Encode[0] << 2) | (Base64Encode[1] >> 4);
		*OrgString++ = (Base64Encode[1] << 4) | (Base64Encode[2] >> 2);
		*OrgString++ = (Base64Encode[2] << 6) | (Base64Encode[3]);

		Base64String += 4;
		Base64StringLen -= 4;
		OrgStringLen += 3;
	}

	return OrgStringLen;
}
//Base64 函数完毕
}

