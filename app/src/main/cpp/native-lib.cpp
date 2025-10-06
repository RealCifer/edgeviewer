#include <jni.h>
#include <string>
#include <android/log.h>
#include <opencv2/opencv.hpp>
#include "ImageProcessor.h"

#define TAG "NativeLib"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

extern "C" JNIEXPORT jlong JNICALL
Java_com_example_edgeviewer_NativeProcessor_createMat(
        JNIEnv* env,
        jobject thiz,
        jint width,
        jint height) {

    cv::Mat* mat = new cv::Mat(height, width, CV_8UC4);
    return reinterpret_cast<jlong>(mat);
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgeviewer_NativeProcessor_deleteMat(
        JNIEnv* env,
jobject thiz,
        jlong matAddr) {

if (matAddr != 0) {
cv::Mat* mat = reinterpret_cast<cv::Mat*>(matAddr);
delete mat;
}
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgeviewer_NativeProcessor_processFrame(
        JNIEnv* env,
jobject thiz,
        jlong inputAddr,
jlong outputAddr,
        jboolean applyEdgeDetection) {

cv::Mat* input = reinterpret_cast<cv::Mat*>(inputAddr);
cv::Mat* output = reinterpret_cast<cv::Mat*>(outputAddr);

if (!input || !output) {
LOGD("Invalid mat pointers");
return;
}

cv::Mat processed = ImageProcessor::processFrame(*input, applyEdgeDetection);

if (!processed.empty()) {
processed.copyTo(*output);
}
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_edgeviewer_NativeProcessor_convertYUVtoRGBA(
        JNIEnv* env,
jobject thiz,
        jbyteArray yuvData,
jint width,
        jint height,
jlong outputAddr) {

jbyte* yuv = env->GetByteArrayElements(yuvData, nullptr);
cv::Mat* output = reinterpret_cast<cv::Mat*>(outputAddr);

if (!yuv || !output) {
if (yuv) env->ReleaseByteArrayElements(yuvData, yuv, JNI_ABORT);
return;
}

cv::Mat yuvMat(height + height/2, width, CV_8UC1, yuv);
cv::cvtColor(yuvMat, *output, cv::COLOR_YUV2RGBA_NV21);

env->ReleaseByteArrayElements(yuvData, yuv, JNI_ABORT);
}


extern "C" JNIEXPORT jbyteArray JNICALL
        Java_com_example_edgeviewer_NativeProcessor_getMatData(
        JNIEnv* env,
        jobject thiz,
jlong matAddr) {

cv::Mat* mat = reinterpret_cast<cv::Mat*>(matAddr);

if (!mat || mat->empty()) {
return nullptr;
}

int dataSize = mat->rows * mat->cols * mat->channels();
jbyteArray result = env->NewByteArray(dataSize);

if (result) {
env->SetByteArrayRegion(result, 0, dataSize,
reinterpret_cast<jbyte*>(mat->data));
}

return result;
}