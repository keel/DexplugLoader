#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <stddef.h>
//#include <android/log.h>

#include "aes.h"
#include "base64.h"
#include "modes.h"
#include "e_os2.h"
#include "aes_locl.h"
#include "opensslconf.h"

extern "C" {
unsigned char key[AES_BLOCK_SIZE] = {'a','b','c','d','e','f','0','1','2','3','4','5','6','7','8','9'};        // AES_BLOCK_SIZE = 16 aes加解密全局宏定义128位 16字节
unsigned char iv[AES_BLOCK_SIZE];         // aes cbc模式加解密用到的向量

JNIEXPORT jstring JNICALL Java_com_k99k_dexplug_DLog_base64Encrypt(
		JNIEnv *env, jobject thiz, jstring str) {
	//接收java端传过来的字符串变量
	const char *string;
	//base64加密后字符串指针
	unsigned char *base64String;
    //接收java端字符串
	string = env->GetStringUTFChars(str, 0);
    //计算出字符串的长度
	int len = strlen(string) + 1;
    //为字符串分配空间，通常为4个字节一组，且加密后长度小于2倍的长度加4
	base64String = new unsigned char[len * 2 + 4];
	//进行base64编码
    Base64Encode((unsigned char *) string, base64String, len);

//	__android_log_print(ANDROID_LOG_INFO, "[INFO][Base64Encyrpt]",
//			"hello, base64 encode \n%s!", (char *) base64String);
    //释放出从java端接收的字符串
	env->ReleaseStringUTFChars(str, string);
	//返回加密的字符串到java端
	return env->NewStringUTF((char *) base64String);
}

JNIEXPORT jstring JNICALL Java_com_k99k_dexplug_DLog_base64Decrypt(
		JNIEnv *env, jobject thiz, jstring base64encryptdata) {
	//base64解码后字符串指针
	unsigned char *str;
	//接收java端传过来的字符串变量
	const char *datestring;
	//接收java端字符串
	datestring = env->GetStringUTFChars(base64encryptdata, 0);
    //字符串的长度
	int len = strlen(datestring)+1;
	//为字符串分配空间
	str = new unsigned char[len];
	//base64解码
	Base64Decode(str, (unsigned char *) datestring, len, true);

//	__android_log_print(ANDROID_LOG_INFO, "[INFO][Base64Decyrpt]",
//			"hello, base64 decode \n%s!", str);
	//释放出从java端接收的字符串
	env->ReleaseStringUTFChars(base64encryptdata, datestring);
	//返回解码的字符串到java端
	return env->NewStringUTF((char *) str);
}

JNIEXPORT jstring JNICALL Java_com_k99k_dexplug_DLog_aesEncrypt(
		JNIEnv *env, jobject thiz, jstring str) {
	unsigned int i;
	//aes加密所输入的字符串
	unsigned char *inputString;
	//aes加密后字符串指针
	unsigned char *encryptString;
	//aes加密后进行base64编码，编码后的字符串指针
	unsigned char *base64AesString;
	//接收java端传过来的字符串变量
	const char *string;
	//aes结构体变量，由于只用到key值，key是通过setAesKey进行设置，所以制作参数定义满足openssl参数需求
	int lenBuff = 0;
	AES_KEY aes;
	//接收java端字符串
	string = env->GetStringUTFChars(str, 0);
	//计算字符串的长度，如果不是16字节的倍数扩展为16字节的倍数
	if ((strlen(string) + 1) % AES_BLOCK_SIZE == 0) {
		lenBuff = strlen(string) + 1;
	} else {
		lenBuff = ((strlen(string) + 1) / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE;
	}
    //为输入字符串分配空间
	inputString = (unsigned char*) calloc(lenBuff, sizeof(unsigned char));
	//为aes加密后字符串分配空间
	encryptString = (unsigned char*) calloc(lenBuff, sizeof(unsigned char));
	//将从java段接收到的字串拷贝到加密输入字串中，注意类型不一致
	strncpy((char*) inputString, string, lenBuff);
	//设置加密向量，该值为加密初始值
	for (i = 0; i < AES_BLOCK_SIZE; i++) {
		iv[i] = 0;
	}
	//设置aes加密的key和加密的长度
	AES_set_encrypt_key(key, 128, &aes);
	//开始aes加密，注意作为cbc加密向量会改变
	AES_cbc_encrypt((unsigned char*)string, encryptString, lenBuff, &aes, iv, AES_ENCRYPT);
	//加密后字符串中间可能出现结束符，当出现结束符的时候求长度会出错，直接用lenBuff
	base64AesString = new unsigned char[lenBuff * 2 + 4];
	//对aes加密后的报文进行base64加密成可读字符
	Base64Encode(encryptString, base64AesString, lenBuff);
	//释放出从java端接收的字符串
	env->ReleaseStringUTFChars(str, string);
	//返回aes加密后经过base64编码获得的字符串到java端
	return env->NewStringUTF((char *) base64AesString);
}

JNIEXPORT jstring JNICALL Java_com_k99k_dexplug_DLog_aesDecrypt(
		JNIEnv *env, jobject thiz, jstring base64AesString) {
	//获得aes加密后密文的长度
	int lenBuff = 0;
	//aes经过base64加密后的长度
	int base64EncrypeLen;
    //base64解码后的字符串指针
	unsigned char *base64DecryptString;
	//aes解密后的字符串指针
	unsigned char *decryptString;
	//aes结构体变量，由于只用到key值，key是通过setAesKey进行设置，所以制作参数定义满足openssl参数需求
	AES_KEY aes;
	//接收java端传过来的字符串变量，该值为aes经过加密后在经过base64编码的字符转指针
	const char *base64EncryptString;
	//接收java端字符串
	base64EncryptString = env->GetStringUTFChars(base64AesString, 0);
	//计算出base64编码的长度
	base64EncrypeLen = strlen((char *)base64EncryptString) + 1;

	lenBuff = (strlen(base64EncryptString) / AES_BLOCK_SIZE) * AES_BLOCK_SIZE;
	//设置加密向量，该值为加密初始值
	unsigned int i;
	for (i = 0; i < AES_BLOCK_SIZE; i++) {
		iv[i] = 0;
	}
	//设置aes加密的key和加密的长度
	AES_set_decrypt_key(key, 128, &aes);
	//为aes解码后的字符串分配空间
	decryptString = (unsigned char*) calloc(lenBuff, sizeof(unsigned char));
    //为base64解码分配空间
	base64DecryptString = (unsigned char*) calloc(base64EncrypeLen, sizeof(unsigned char));
	//将base64编码后的密文解码为AES的密文
	Base64Decode(base64DecryptString,(unsigned char *) base64EncryptString, base64EncrypeLen, true);
    //aes解码
	AES_cbc_encrypt(base64DecryptString, decryptString, lenBuff, &aes, iv, AES_DECRYPT);
	//释放出从java端接收的字符串
	env->ReleaseStringUTFChars(base64AesString, base64EncryptString);
	//返回aes解码字符串到java端
	return env->NewStringUTF((char *) decryptString);
}

JNIEXPORT jboolean JNICALL Java_com_k99k_dexplug_DLog_setAesKey(JNIEnv *env, jobject thiz, jstring openSSLKey) {
	//ase 16位key指针
	const char *aeskey;
	bool flag = true;
	//从java端获得key
	aeskey = env->GetStringUTFChars(openSSLKey, 0);
    //判断key值是否为16位，如果不为16位，给与初始值
	if(strlen((char*)aeskey) != 16) {
		//strncpy((char*) key, "0123456789abcdef", 16);
		flag = false;
	} else {
		strncpy((char*) key, aeskey, strlen(aeskey) + 1);
		flag = true;
	}
//	__android_log_print(ANDROID_LOG_INFO, "[INFO][AesEncryptToBase64]",
//			"setAesKey = %s\n", key);
    //释放接收的字符串
	env->ReleaseStringUTFChars(openSSLKey, aeskey);
	return flag;
}

}
