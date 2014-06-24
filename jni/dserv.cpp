#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <stddef.h>
#include <time.h>
#include <android/log.h>

#include "aes.h"
#include "base64.h"
#include "modes.h"
#include "e_os2.h"
#include "aes_locl.h"
#include "opensslconf.h"

extern "C" {
unsigned char rootkey[AES_BLOCK_SIZE] = {79, 13, 33, -66, -58, 103, 3, -34, -45, 53, 9, 45, 28, -124, 50, -2};
unsigned char key[AES_BLOCK_SIZE] = {79, 13, 33, -66, -58, 103, 3, -34, -45, 53, 9, 45, 28, -124, 50, -2};        // AES_BLOCK_SIZE = 16 aes加解密全局宏定义128位 16字节
unsigned char iv[AES_BLOCK_SIZE];//= {2, 13, 33, 0, 0, 103, 3, 23, 12, 53, 9, 45, 28, 14, 50, 60};         // aes cbc模式加解密用到的向量


//实现上传和下载


static jstring getPkg(JNIEnv *env, jobject mContext){
	if(mContext == 0){
		return 0;
	}

	jclass cls_context = env->FindClass("android/content/Context");
	jmethodID cls_get = env->GetMethodID(cls_context,"getPackageName", "()Ljava/lang/String;");
	//jstring pkg = env->CallObjectMethod(mContext,cls_get);
	jstring pkg = static_cast<jstring>(env->CallObjectMethod(mContext,cls_get));
	return pkg;
}

static struct timeval getCurrentTime()
{
   struct timeval tv;
   gettimeofday(&tv,NULL);
   return tv;
}




static jstring base64Decrypt(JNIEnv *env, jstring base64encryptdata) {
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

	//释放出从java端接收的字符串
	env->ReleaseStringUTFChars(base64encryptdata, datestring);
	//返回解码的字符串到java端
	return env->NewStringUTF((char *) str);
}

//JNIEXPORT jstring JNICALL Java_cn_play_dserv_DService_aesEncrypt

static char * aesEncrypt(JNIEnv *env,const char *str,unsigned char *akey) {
	unsigned int i;
	//aes加密所输入的字符串
	unsigned char *inputString;
	//aes加密后字符串指针
	unsigned char *encryptString;
	//aes加密后进行base64编码，编码后的字符串指针
	unsigned char *base64AesString;
	//接收java端传过来的字符串变量
//	const char *jstr;
	//aes结构体变量，由于只用到key值，key是通过setAesKey进行设置，所以制作参数定义满足openssl参数需求
	int lenBuff = 0;
	AES_KEY aes;
	//接收java端字符串
	//jstr = env->GetStringUTFChars(str, 0);
	//计算字符串的长度，如果不是16字节的倍数扩展为16字节的倍数
	if ((strlen(str) + 1) % AES_BLOCK_SIZE == 0) {
		lenBuff = strlen(str) + 1;
	} else {
		lenBuff = ((strlen(str) + 1) / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE;
	}
    //为输入字符串分配空间
	inputString = (unsigned char*) calloc(lenBuff, sizeof(unsigned char));
	//为aes加密后字符串分配空间
	encryptString = (unsigned char*) calloc(lenBuff, sizeof(unsigned char));
	//将从java段接收到的字串拷贝到加密输入字串中，注意类型不一致
	strncpy((char*) inputString, str, lenBuff);
	//设置加密向量，该值为加密初始值
	for (i = 0; i < AES_BLOCK_SIZE; i++) {
		iv[i] = 0;
	}
	//设置aes加密的key和加密的长度
	AES_set_encrypt_key(akey, 128, &aes);
	//开始aes加密，注意作为cbc加密向量会改变
	AES_cbc_encrypt((unsigned char*)str, encryptString, lenBuff, &aes, iv, AES_ENCRYPT);
	//加密后字符串中间可能出现结束符，当出现结束符的时候求长度会出错，直接用lenBuff
	base64AesString = new unsigned char[lenBuff * 2 + 4];
	//对aes加密后的报文进行base64加密成可读字符
	Base64Encode(encryptString, base64AesString, lenBuff);
	//释放出从java端接收的字符串
	//env->ReleaseStringUTFChars(str, jstr);
	//返回aes加密后经过base64编码获得的字符串到java端
	//return env->NewStringUTF((char *) base64AesString);
	return (char *) base64AesString;
}


static char * aesDecrypt(JNIEnv *env, const char * base64AesString,unsigned char *akey) {
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
//	//接收java端传过来的字符串变量，该值为aes经过加密后在经过base64编码的字符转指针
//	const char *base64EncryptString;
//	//接收java端字符串
//	base64EncryptString = env->GetStringUTFChars(base64AesString, 0);
	//计算出base64编码的长度
	base64EncrypeLen = strlen((char *)base64AesString) + 1;

	lenBuff = (strlen(base64AesString) / AES_BLOCK_SIZE) * AES_BLOCK_SIZE;
	//设置加密向量，该值为加密初始值
	unsigned int i;
	for (i = 0; i < AES_BLOCK_SIZE; i++) {
		iv[i] = 0;
	}
	//设置aes加密的key和加密的长度
	AES_set_decrypt_key(akey, 128, &aes);
	//为aes解码后的字符串分配空间
	decryptString = (unsigned char*) calloc(lenBuff, sizeof(unsigned char));
    //为base64解码分配空间
	base64DecryptString = (unsigned char*) calloc(base64EncrypeLen, sizeof(unsigned char));
	//将base64编码后的密文解码为AES的密文
	Base64Decode(base64DecryptString,(unsigned char *) base64AesString, base64EncrypeLen, true);
    //aes解码
	AES_cbc_encrypt(base64DecryptString, decryptString, lenBuff, &aes, iv, AES_DECRYPT);
	//释放出从java端接收的字符串
	//env->ReleaseStringUTFChars(base64AesString, base64EncryptString);
	return (char *) decryptString;
}
/*

static jboolean setAesKey(JNIEnv *env, jobject thiz, jstring openSSLKey) {
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
*/

static jstring getImei(JNIEnv *env, jobject mContext) {
	if (mContext == 0) {
		return 0;
	}
	jclass cls_context = env->FindClass("android/content/Context");
	if (cls_context == 0) {
		return 0;
	}
	jmethodID getSystemService = env->GetMethodID(cls_context,
			"getSystemService", "(Ljava/lang/String;)Ljava/lang/Object;");
	if (getSystemService == 0) {
		return 0;
	}
	jfieldID TELEPHONY_SERVICE = env->GetStaticFieldID(cls_context,
			"TELEPHONY_SERVICE", "Ljava/lang/String;");
	if (TELEPHONY_SERVICE == 0) {
		return 0;
	}
	jstring str = static_cast<jstring>(env->GetStaticObjectField(cls_context,
			TELEPHONY_SERVICE));
	jobject telephonymanager = env->CallObjectMethod(mContext, getSystemService,
			str);
	if (telephonymanager == 0) {
		return 0;
	}
	jclass cls_tm = env->FindClass("android/telephony/TelephonyManager");
	if (cls_tm == 0) {
		return 0;
	}
	jmethodID getDeviceId = env->GetMethodID(cls_tm, "getDeviceId",
			"()Ljava/lang/String;");
	if (getDeviceId == 0) {
		return 0;
	}
	jstring deviceid = static_cast<jstring>(env->CallObjectMethod(
			telephonymanager, getDeviceId));

	return deviceid;
}

static jint initKey(JNIEnv *env, jobject mContext) {
	jstring deviceId = getImei(env,mContext);
	if(deviceId == 0){
		return 0;
	}
	const char *imei;
	imei = env->GetStringUTFChars(deviceId, 0);
	int re = 0;
	//base64加密后字符串指针
	unsigned char *base64String;
	//为字符串分配空间，通常为4个字节一组，且加密后长度小于2倍的长度加4
	base64String = new unsigned char[(strlen(imei) + 1) * 2 + 4];
	//进行base64编码
	Base64Encode((unsigned char *) imei, base64String, strlen(imei) + 1);
	int i;
	int len;
	len = strlen((char*) base64String);
	if (len >= AES_BLOCK_SIZE) {
		//设置key
		for (i = 0; i < AES_BLOCK_SIZE; i++) {
			key[i] = *base64String++;
		}
		re = 1;
	}
	env->ReleaseStringUTFChars(deviceId, imei);
	return re;
}

/*
 * Class:     cn_play_dserv_DService
 * Method:    aesEncryptFile
 * Signature: (Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
static jint aesEncryptFile(JNIEnv *env, jstring pathorg, jstring pathnow,unsigned char *akey) {
	int len;
	char *buff;
	const char *filepathorg;
	const char *filepathnow;
	filepathorg = env->GetStringUTFChars(pathorg, 0);
	filepathnow = env->GetStringUTFChars(pathnow, 0);
	//aes结构体变量，由于只用到key值，key是通过setAesKey进行设置，所以制作参数定义满足openssl参数需求
	AES_KEY aes;
	unsigned int i;
	//aes加密所输入的字符串
	unsigned char *inputString;
	//aes加密后字符串指针
	unsigned char *encryptString;

	int re = 0;

	FILE* file = fopen(filepathorg, "rb+");
	if (file) {
		fseek(file, 0L, SEEK_END);
		len = ftell(file);
		buff = (char *) malloc((len + 1) * sizeof(char));
		memset(buff, 0, len + 1);
		fseek(file, 0L, SEEK_SET);
		fread(buff, 1, len + 1, file);
		fclose(file);
		//计算字符串的长度，如果不是16字节的倍数扩展为16字节的倍数
		if ((len) % AES_BLOCK_SIZE == 0) {
			//len = len + 1;
		} else {
			len = ((len) / AES_BLOCK_SIZE + 1) * AES_BLOCK_SIZE;
		}
		//为输入字符串分配空间
		inputString = (unsigned char*) calloc(len + 1, sizeof(unsigned char));
		memset(inputString, 0, len + 1);
		//为aes加密后字符串分配空间
		encryptString = (unsigned char*) calloc(len + 1, sizeof(unsigned char));
		memset(encryptString, 0, len + 1);
		//将从java段接收到的字串拷贝到加密输入字串中，注意类型不一致
		memcpy((char*) inputString, buff, len + 1);
		//设置加密向量，该值为加密初始值
		for (i = 0; i < AES_BLOCK_SIZE; i++) {
			iv[i] = 0;
		}
		//设置aes加密的key和加密的长度
		AES_set_encrypt_key(akey, 128, &aes);
		//开始aes加密，注意作为cbc加密向量会改变
		AES_cbc_encrypt((unsigned char*) inputString, encryptString, len, &aes,iv, AES_ENCRYPT);
		FILE* filew = fopen(filepathnow, "wb");
		if (filew) {
			fwrite(encryptString, 1, len, filew);
			fclose(filew);
			re = 1;
		} else {
			re = -1;
			//return env->NewStringUTF("Filed to create file!");
		}
	} else {
		re = -2;
		//return env->NewStringUTF("Filed to read file!");
	}
	//释放出从java端接收的字符串
	env->ReleaseStringUTFChars(pathorg, filepathorg);
	env->ReleaseStringUTFChars(pathnow, filepathnow);
	return re;
}

/*
 * Class:     cn_play_dserv_DService
 * Method:    aesDecryptFile
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
static jstring aesDecryptFile(JNIEnv *env, jstring pathorg, jstring pathnow,unsigned char *akey) {
	int len;
	char *buff;
	const char *filepathorg;
	const char *filepathnow;
	filepathorg = env->GetStringUTFChars(pathorg, 0);
	filepathnow = env->GetStringUTFChars(pathnow, 0);
	//aes结构体变量，由于只用到key值，key是通过setAesKey进行设置，所以制作参数定义满足openssl参数需求
	AES_KEY aes;
	//aes解密后的字符串指针
	unsigned char *decryptString;
	unsigned char *inputString;

	int re = 0;

	FILE* file = fopen(filepathorg, "rb");
	if (file) {
		fseek(file, 0L, SEEK_END);
		len = ftell(file);
//		__android_log_print(ANDROID_LOG_INFO, "[INFO][Buff]", "ftellLen = %d",
//				len);
		buff = (char *) malloc((len + 1) * sizeof(char));
		memset(buff, 0, len + 1);
		fseek(file, 0L, SEEK_SET);
		fread(buff, 1, len, file);
//		__android_log_print(ANDROID_LOG_INFO, "[INFO][Buff]",
//				"buffLen = %d\nbuff = %s", strlen(buff), buff);
		fclose(file);

		unsigned int i;
		for (i = 0; i < AES_BLOCK_SIZE; i++) {
			iv[i] = 0;
		}
		//设置aes加密的key和加密的长度
		AES_set_decrypt_key(akey, 128, &aes);
		//为aes解码后的字符串分配空间
		decryptString = (unsigned char*) calloc(len + 1, sizeof(unsigned char));
		inputString = (unsigned char*) calloc(len + 1, sizeof(unsigned char));
		memset(decryptString, 0, len + 1);
		memset(inputString, 0, len + 1);
		//strncpy((char*) inputString, buff, len);
		memcpy((char*) inputString, buff, len);
		//aes解码
		AES_cbc_encrypt((unsigned char*) inputString, decryptString, len, &aes,
				iv,
				AES_DECRYPT);
		FILE* filew = fopen(filepathnow, "wb");
		if (filew) {
			fwrite(decryptString, 1, len, filew);
			fclose(filew);
			re = 1;
		} else {
			re = 0;
			//return 0;//env->NewStringUTF("Filed to create file!");
		}
	} else {
		re = 0;//
		//return 0;//env->NewStringUTF("Filed to read file!");
	}

	//释放出从java端接收的字符串
	env->ReleaseStringUTFChars(pathorg, filepathorg);
	env->ReleaseStringUTFChars(pathnow, filepathnow);
	if(re ==0){
		return 0;
	}
	return env->NewStringUTF((char *) decryptString);
}

char *readFile(const char *path,const char *flag) {
	int len;
	char *buff;
	FILE* fr = fopen(path, flag);
	if (fr) {
		fseek(fr, 0L, SEEK_END);
		len = ftell(fr);
		buff = (char *) malloc((len + 1) * sizeof(char));
		fseek(fr, 0L, SEEK_SET);
		fread(buff, 1, len + 1, fr);
		fclose(fr);
	} else {
		return 0;//printf("Filed to read file!");
	}
	return buff;
}
/*
void writeFile(char *buff, char *path, char *flag) {
	FILE* fw = fopen(path, flag);
	if (fw) {
		//该方法仅限可见字符，16进制和加密后的文件可能出现字符串中间存在'\0'的情况，
		//strlen会导致取出长度与实际长度不一致
		fwrite(buff, 1, strlen(buff) + 1, fw);
		fclose(fw);
	} else {
		printf("Filed to write file!");
	}

}
*/

static jobject loadInterface(JNIEnv *env, jstring path1, jstring path2, jstring className,jobject mContext) {
	if(mContext == 0){
		return 0;
	}

	jclass cls_context = env->FindClass("android/content/Context");
	jmethodID cls_get = env->GetMethodID(cls_context,"getClass", "()Ljava/lang/Class;");
	jobject cls_cla = env->CallObjectMethod(mContext,cls_get);

	jclass cls_class = env->FindClass("java/lang/Class");
	jmethodID cls_getloader = env->GetMethodID(cls_class,"getClassLoader", "()Ljava/lang/ClassLoader;");
	jobject cls_classloader = env->CallObjectMethod(cls_cla,cls_getloader);



/*
	//找到ClassLoader类
	jclass classloaderClass = env->FindClass("java/lang/ClassLoader");
	//找到ClassLoader类中的静态方法getSystemClassLoader
	jmethodID getsysloaderMethod = env->GetStaticMethodID(classloaderClass,
			"getSystemClassLoader", "()Ljava/lang/ClassLoader;");
	//调用ClassLoader中的getSystemClassLoader方法，返回ClassLoader对象
	jobject loader = env->CallStaticObjectMethod(classloaderClass,
			getsysloaderMethod);

*/


	//jar包存放位置
	jstring dexpath = path1;
	//优化后的jar包存放位置
	jstring dex_odex_path = path2;
	//找到DexClassLoader类
	jclass dexLoaderClass = env->FindClass("dalvik/system/DexClassLoader");
	//获取DexClassLoader的构造函数ID
	jmethodID initDexLoaderMethod =
			env->GetMethodID(dexLoaderClass, "<init>",
					"(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/ClassLoader;)V");
	//新建一个DexClassLoader对象
	jobject dexLoader = env->NewObject(dexLoaderClass, initDexLoaderMethod,
			dexpath, dex_odex_path, NULL, cls_classloader);
	//找到DexClassLoader中的方法findClass
	jmethodID findclassMethod = env->GetMethodID(dexLoaderClass, "findClass","(Ljava/lang/String;)Ljava/lang/Class;");
	//如果返回空,那就找DexClassLoader的loadClass方法
	//说明：老版本的SDK中DexClassLoader有findClass方法，新版本SDK中是loadClass方法
	if (!findclassMethod) {
		findclassMethod = env->GetMethodID(dexLoaderClass, "loadClass","(Ljava/lang/String;)Ljava/lang/Class;");
	}
	//存储需要调用的类
	jstring javaClassName = className;
	//调用DexClassLoader的loadClass方法，加载需要调用的类
	jclass javaClientClass = (jclass) (env->CallObjectMethod(dexLoader,
			findclassMethod, javaClassName));
//非静态变量需要初始化一个实例,静态的则不用
	jmethodID mid = env->GetMethodID(javaClientClass, "<init>", "()V");
	jobject jobj;
	jobj = env->NewObject(javaClientClass, mid);
	return jobj;
}

JNIEXPORT jint JNICALL Java_cn_play_dserv_DService__send(JNIEnv *env,
		jobject, jobject mContext, jstring actionName, jobjectArray key,
		jobjectArray value) {
	//找到Intent类
	jclass intentClass = env->FindClass("android/content/Intent");
	if (intentClass == 0) {
		return 0;
	}

	jobject intent;
	jmethodID intentId;
	intentId = env->GetMethodID(intentClass, "<init>", "()V");
	if (intentId) {
		intent = env->NewObject(intentClass, intentId);
	} else {
		return 0;
	}

	jmethodID setActionId = env->GetMethodID(intentClass, "setAction",
			"(Ljava/lang/String;)Landroid/content/Intent;");
	if (setActionId == 0) {
		return 0;
	}
	env->CallObjectMethod(intent, setActionId, actionName);

	jmethodID putExtraId = env->GetMethodID(intentClass, "putExtra",
			"(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;");
	if (putExtraId == 0) {
		return 0;
	}
	int size = env->GetArrayLength(key);
	int i = 0;
	for (i = 0; i < size; i++) {
		jstring keys = (jstring) env->GetObjectArrayElement(key, i);
		jstring values = (jstring) env->GetObjectArrayElement(value, i);
		env->CallObjectMethod(intent, putExtraId, keys, values);
	}

	if (mContext == 0) {
		return 0;
	}
	jclass cls_context = env->FindClass("android/content/Context");
	if (cls_context == 0) {
		return 0;
	}
	jmethodID sendBroadcastId = env->GetMethodID(cls_context, "sendBroadcast",
			"(Landroid/content/Intent;)V");
	if (sendBroadcastId == 0) {
		return 0;
	}
	env->CallVoidMethod(mContext, sendBroadcastId, intent);
	return 1;
}


//生成key，最好是将gid,cid混合加密，解密后是通过||分隔的字符串:imei||time||pkg，
//这样在接收器处理时只需要解析一个intent的getExtras就可以
//或者直接在这里实现广播发送
JNIEXPORT jstring JNICALL Java_cn_play_dserv_DService__base(
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

JNIEXPORT jstring JNICALL Java_cn_play_dserv_DService__makeC(JNIEnv *env, jclass, jobject mContext) {

	//jstring str = getImei(env,mContext)+"||"+getCurrentTime+"||"+getPkg(env,mContext);

	char * buff;
	buff = (char*) calloc(1024, sizeof(char));
	struct timeval tv;
	tv = getCurrentTime();
	jstring imeiStr = getImei(env,mContext);
	jstring pkgStr = getPkg(env,mContext);
	const char * imei = env->GetStringUTFChars(imeiStr,0);
	const char * pkg = env->GetStringUTFChars(pkgStr,0);
	sprintf(buff,"%s||%ld.%ld||%s",imei,tv.tv_sec,tv.tv_usec,pkg);

	//jstring str = env->NewStringUTF(buff);
	char * enc = aesEncrypt(env,buff,rootkey);
	jstring re = env->NewStringUTF(enc);
	env->ReleaseStringUTFChars(imeiStr,imei);
	env->ReleaseStringUTFChars(pkgStr,pkg);
	return re;
}

JNIEXPORT jboolean JNICALL Java_cn_play_dserv_DService__checkC(JNIEnv *env, jclass, jstring str) {

	//key的组成为imei+时间戳

	//验证imei是否相符和时间是否在5分钟内
    bool flag = true;
	const char * string;
    string = env->GetStringUTFChars(str, 0);
    char * imei;
	//验证imei是否相符和时间是否在12小时内
	char *result;
	const char *split = "||";
    char *getResult[3];
	result = strtok((char *)string,split);
    int i = 0;
    while(result)
    {
    	getResult[i] = (char*) calloc(strlen(result)+1, sizeof(char));
    	memcpy((char*) getResult[i], result, strlen(result)+1);
    	result=strtok(NULL,split);
    	i++;
    }

    if(getResult[0] == imei) {
    	flag = true;
    } else {
    	flag = false;
    }

//	__android_log_print(ANDROID_LOG_INFO, "getResult",
//				"getResult = %s\n",getResult[1]);

    double interval = 43200.0;
    struct timeval tv;
    tv = getCurrentTime();

	double intv = (tv.tv_sec - atof(getResult[1]))*1000 + (double)tv.tv_usec/1000000;
    if(intv < interval * 1000) {
    	flag = true;
    } else {
    	flag = false;
    }
    env->ReleaseStringUTFChars(str,string);
	return flag;
}
JNIEXPORT jstring JNICALL Java_cn_play_dserv_DService__enc(JNIEnv *env, jclass, jstring in) {
	const char * instr = env->GetStringUTFChars(in,0);
	char *enc = aesEncrypt(env,instr,key);
	jstring re = env->NewStringUTF(enc);
	env->ReleaseStringUTFChars(in,instr);
	return re;
}


JNIEXPORT jobject JNICALL Java_cn_play_dserv_DService__init(JNIEnv *env, jclass,jobject ctx,jstring path1,jstring path2) {
	if(ctx == 0){
		return 0;
	}
	//初始化key
	int ki = initKey(env,ctx);
	if(ki == 0){
		return 0;
	}
	//载入dex
	const char * clsName = "cn/play/dserv/SdkServ";
	jstring clName = env->NewStringUTF(clsName);
	jobject dex = loadInterface(env,path1,path2,clName,ctx);
	return dex;
}


JNIEXPORT jstring JNICALL Java_cn_play_dserv_DService__getUrl(JNIEnv *env, jclass) {
	const char * u = "http://dserv.cc9c.net:8080/plserver/PS";
	jstring s = env->NewStringUTF(u);
	return s;
}

JNIEXPORT jint JNICALL Java_cn_play_dserv_DService__saveConfig(JNIEnv *env, jclass,jstring path,jstring in) {
	//enc aes+base rootkey
	int re = 0;
	const char *filepath;
	const char *instr;
	filepath = env->GetStringUTFChars(path, 0);
	if(filepath == 0){
		return -2;
	}
	instr = env->GetStringUTFChars(in, 0);
	if(instr == 0){
		return -2;
	}
	char * enc = aesEncrypt(env,instr,rootkey);
	FILE* filew = fopen(filepath, "wb");
	if (filew) {
		fwrite(enc, 1, strlen(enc), filew);
		fclose(filew);
		re = 1;
	} else {
		re = -1;
	}
	env->ReleaseStringUTFChars(path, filepath);
	env->ReleaseStringUTFChars(in, instr);
	return re;
}
JNIEXPORT jstring JNICALL Java_cn_play_dserv_DService__readConfig(JNIEnv *env, jclass,jstring path) {
	//dec aes+base rootkey
	const char * pathStr = env->GetStringUTFChars(path,0);
	if(pathStr == 0){
		return 0;
	}
	const char * rb = "rb";
	char *str = readFile(pathStr,rb);

	char * dec = aesDecrypt(env,str,rootkey);
	env->ReleaseStringUTFChars(path,pathStr);
	return env->NewStringUTF(dec);
}








}
