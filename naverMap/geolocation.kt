

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac
import java.util.Base64
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.net.URLDecoder
import java.io.DataOutputStream
import java.io.DataInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset


/*
네이버 geolocation api 코틀린 방식 구현 
*/
fun main(args: Array<String>){  
    
    // 사용자가 알아먹기 힘든 네이버 설명서를 대신함
  
    // api Key: https://www.ncloud.com/mypage/manage/authkey 에 있는 키를 복사해서 사용하면 된다.
    // 설명서: https://api.ncloud-docs.com/docs/ko/ai-application-service-geolocation
    // 서비스키 샘플이 필요하면 https://github.com/jikjoo/Bangul_webOS/issues/25 이런데도 참조 
    val clientAccessKey = "" // 엑세스키
    val clientSecretKey = "" //암호화 키 
    val ip  = "211.51.234.134" // 샘플 

    val url = "https://geolocation.apigw.ntruss.com/geolocation/v2/geoLocation?ip=" + ip + "&enc=utf8&ext=t&responseFormatType=json"
    val urlSub = "/geolocation/v2/geoLocation?ip=" + ip + "&enc=utf8&ext=t&responseFormatType=json"

    val timestamp = System.currentTimeMillis().toString() // 밀리세컨드 
    val sig = makeSignature(urlSub, timestamp, ip,  clientAccessKey, clientSecretKey)// urlSub 주소를이용해서 암호화를 해야 한다. 
    

    val conn = URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "GET"
    conn.setRequestProperty("x-ncp-apigw-timestamp", timestamp)  // 5초안에 만들어진 키 아니면 deny됩니다.
    conn.setRequestProperty("x-ncp-iam-access-key", clientAccessKey)
    conn.setRequestProperty("x-ncp-apigw-signature-v2", sig)



    if ((conn.responseCode) == (401)){
        println("401 error")
    } else{
        val data = conn.inputStream.bufferedReader().readText()
        println(data)
    }
  
    /*
    결과:
    {"returnCode": 0,"requestId": "ce16864c-15db-4849-926f-a9358383a02c",
    "geoLocation":
    {"country": "KR","code": "1111058000","r1": "서울특별시","r2": "종로구","r3": "교남동","lat": 37.571939,"long": 126.961936,"net": "Korea Telecom"}}
    */
   

}   

// 시크릿키 만드는 암호화 
fun makeSignature(url: String?, timestamp: String?, userIp: String?, clientAccessKey:String, clientSecretKey:String): String? {
    val space = " " // one space
    val newLine = "\n" // new line
    val method = "GET" // method

    val message: String = StringBuilder()
        .append(method)
        .append(space)
        .append(url)
        .append(newLine)
        .append(timestamp)
        .append(newLine)
        .append(clientAccessKey)
        .toString()
    val signingKey: SecretKeySpec
    var encodeBase64String: String? = ""

        signingKey = SecretKeySpec(clientSecretKey.toByteArray(charset("UTF-8")), "HmacSHA256")
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(signingKey)
        val rawHmac = mac.doFinal(message.toByteArray(charset("UTF-8")))
        val encoder: Base64.Encoder = Base64.getEncoder()
        encodeBase64String = String(encoder.encode(rawHmac))

    return encodeBase64String
}
