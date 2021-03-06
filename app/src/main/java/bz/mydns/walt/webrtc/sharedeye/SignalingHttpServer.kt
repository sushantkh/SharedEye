package bz.mydns.walt.webrtc.sharedeye

import android.util.Log
import fi.iki.elonen.NanoHTTPD


/**
 * Created by u-ryo on 16/10/14.
 */

class SignalingHttpServer(port: Int, private val handler: WebHandler) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession) : Response {
        Log.d("session.uri:", session.uri)
        when (session.uri) {
            "/" -> {
                handler.getOffer()
                return newFixedLengthResponse(handler.getFile("www/client.html"))
            }
            "/offer" -> return NanoHTTPD.newFixedLengthResponse(
                    Response.Status.OK, "application/json",
                    handler.getOffer())
            "/answer" -> try {
                handler.setAnswer(readBody(session))
            } catch (e : Exception) {
                Log.e("SignalingHttpServer", "In /answer", e)
                NanoHTTPD.newFixedLengthResponse(e.toString())
            }
            "/image" -> handler.setImage(readBody(session))
            "/startLocalVideo" -> handler.startLocalVideo()
            "/stopLocalVideo" -> handler.stopLocalVideo()
        }
        Log.d("SignalingHttpServer", "return no_content:")
        return NanoHTTPD.newChunkedResponse(
                Response.Status.NO_CONTENT,
                "text/plain", null)
    }

    private fun readBody(session: IHTTPSession) : String {
        var contentLength =
                session.headers["content-length"]?.toInt()
        if (contentLength == null) contentLength = 0
        val b = ByteArray(10240)
        var readBytes = session.inputStream.read(b, 0,
                Math.min(b.size, contentLength))
        var result = ""
        while (readBytes > 0) {
            result += String(b, 0, readBytes)
            contentLength -= readBytes
            readBytes = session.inputStream.read(
                    b, 0, Math.min(b.size, contentLength))
        }
        result += String(b, 0, readBytes)
        return result
    }
}
