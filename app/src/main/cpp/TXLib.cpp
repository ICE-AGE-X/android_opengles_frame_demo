#include <jni.h>
#include "include/turbojpeg.h"

extern "C" JNIEXPORT jbyteArray Java_com_x_selfchat_MainActivity_00024Companion_nativeTest(
        JNIEnv *env,
        jobject thiz,
        jbyteArray byte_array,
        jint width,
        jint height)
{
    tjhandle handle=tjInitCompress();
    jbyte *data=env->GetByteArrayElements(byte_array, 0);

    int flags = 0;
    int subsamp = TJSAMP_422;
    int pixelfmt = TJPF_RGBA;
    if (NULL == handle) {
        return nullptr;
    }
    /*将rgb图或灰度图等压缩成jpeg格式图片*/
    unsigned char *outjpg_buffer=NULL;
    unsigned long outjpg_size;
    int ret = tjCompress2(handle, reinterpret_cast<const unsigned char *>(data), width, 0, height, pixelfmt, &outjpg_buffer, &outjpg_size, subsamp, 50, flags);
    if (0 != ret) {
        tjDestroy(handle);
        return nullptr;
    }

    tjDestroy(handle);
    env->ReleaseByteArrayElements(byte_array,data,0);
    jbyteArray retba=env->NewByteArray(outjpg_size);
    env->SetByteArrayRegion(retba, 0, outjpg_size, reinterpret_cast<const jbyte *>(outjpg_buffer));
    tjFree(outjpg_buffer);
    return retba;
}