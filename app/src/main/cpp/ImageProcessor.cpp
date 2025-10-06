#include "ImageProcessor.h"
#include <android/log.h>

#define TAG "ImageProcessor"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

cv::Mat ImageProcessor::processFrame(const cv::Mat& input, bool applyEdgeDetection) {
    if (input.empty()) {
        LOGD("Input frame is empty");
        return cv::Mat();
    }

    if (applyEdgeDetection) {
        return cannyEdgeDetection(input);
    } else {
        return input.clone();
    }
}

cv::Mat ImageProcessor::cannyEdgeDetection(const cv::Mat& input) {
    cv::Mat gray, edges, result;

    if (input.channels() == 4) {
        cv::cvtColor(input, gray, cv::COLOR_RGBA2GRAY);
    } else if (input.channels() == 3) {
        cv::cvtColor(input, gray, cv::COLOR_RGB2GRAY);
    } else {
        gray = input.clone();
    }

    cv::GaussianBlur(gray, gray, cv::Size(5, 5), 1.5);

    cv::Canny(gray, edges, 50, 150);

    cv::cvtColor(edges, result, cv::COLOR_GRAY2RGBA);

    return result;
}

cv::Mat ImageProcessor::toGrayscale(const cv::Mat& input) {
    cv::Mat gray, result;

    if (input.channels() == 4) {
        cv::cvtColor(input, gray, cv::COLOR_RGBA2GRAY);
    } else if (input.channels() == 3) {
        cv::cvtColor(input, gray, cv::COLOR_RGB2GRAY);
    } else {
        return input.clone();
    }

    cv::cvtColor(gray, result, cv::COLOR_GRAY2RGBA);
    return result;
}