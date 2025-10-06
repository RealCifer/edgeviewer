package com.example.edgeviewer

class NativeProcessor {

    companion object {
        init {
            System.loadLibrary("edgeviewer")
        }
    }

    external fun createMat(width: Int, height: Int): Long
    external fun deleteMat(matAddr: Long)
    external fun processFrame(inputAddr: Long, outputAddr: Long, applyEdgeDetection: Boolean)
    external fun convertYUVtoRGBA(yuvData: ByteArray, width: Int, height: Int, outputAddr: Long)
    external fun getMatData(matAddr: Long): ByteArray
}