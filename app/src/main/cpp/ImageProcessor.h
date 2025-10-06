#ifndef EDGEVIEWER_IMAGEPROCESSOR_H
#define EDGEVIEWER_IMAGEPROCESSOR_H

#include <opencv2/opencv.hpp>
#include <jni.h>

class ImageProcessor {
public:
    static cv::Mat processFrame(const cv::Mat& input, bool applyEdgeDetection);
    static cv::Mat cannyEdgeDetection(const cv::Mat& input);
    static cv::Mat toGrayscale(const cv::Mat& input);
    static cv::Mat rotateImage(const cv::Mat& input, int rotationDegrees);
};

#endif