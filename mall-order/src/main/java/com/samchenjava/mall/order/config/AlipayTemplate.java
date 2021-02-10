package com.samchenjava.mall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.samchenjava.mall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2016102400749624";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private static String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCJ116Sq6OD+/2VAUNQ9Q6/EebpKKAra90JizpQ9yOfAwKO53/f3kAkdxX2hZu6RlwMifu4hIs5tNIbhNj0Xzi71JUkEkFB8wdYCdFxIvolbPbNKN29m3qLp11TCA+1752aQAqqz3xNSjPqtGBKMqI3HCKRPXqZqkxQov0ZWCpIg81oTs6c5QQX56Ps1N0sVBQHnNhpAr0WLphlydaaX44N4XjNBHfpaiNx74cNYIjoCjQWFa1CEl1QEJezw4pyFftxhPEbi2JVYAw7/lKYqkbsoHMi8fxlZKkyKDuuG3SinCpQAkyUB8xjWrBweDFGbFIbQUStqKPRZkFePFpdxc/FAgMBAAECggEBAIF9yQOiqM8x6IyVjia8wKiw6as6FhYUk6cGm7c6JMWZ70pOomut/7YCBaj85Un+FTVWXxp9scl5XDSr/aC837BcyesEFzgcVlbXimFtiW+lSMl9Tap1KI+Pn/WDyjaRrw4cgajrmIuHXPRtlVO+83MwRC0Lg2Refqj2JS0tDTPm88/7YrS5qPVM6iyqyCEM2lMV5n7AhcJ7jr2ji6GY1S41uO9O3fa9edPzaRFPTNi2hx04eig8JslbhJMK1xmx5Mi+UqHTE+WVq9HfiFEwtO66jEFxm0jyTD1fg58PE4gtKgTE7hUrt04jyKsLiEZYTu9zVAWVCcLa0KAZmcUcB4ECgYEAzKZb8obC4A04jgJ7Ng3deHY23gSc5EDvkPe0NjzgVXbvASIsP8VNwd68oMbFAGgSQn+uPxPByHaD6yMFggdrbZx3Hz/t0lK2lvb1yUWbL3Xu4j/jYKw+dU9udGrCmyo3hNkrtxuAFRt0svL6JNmxOHvoYXAYkxLTIzaVZs6QAiUCgYEArG2Tj+oOaGvgDrHRNwBfFXueEFxX9Ph8CyvlJXbpz1tjJ520t3AaGaSKiPL1RaV25x9o96Ge+CWHV/32/mOrEEBta6siMZcej2ewfQv8JI9MkQQQfSaamDrHLgtH6DSXyBBVafsrVYZyqafnZFbfpOs6Dbg/FNUPI3tS95s+lSECgYBxrJp4PZizgUR/xSsRXGQ/jFJvwlovg05O1Ph6BBiMAHNDyuqPRZYqcQGPwLkev7Ac1fVAvkWuhv5BtwNRNbPbf8S/S/BbGmynsGfYM9y+YSCe9ePkB+jtmoB9E+wi6jFKjqL0o8bicozA9awymeURvenXbb+IujUjYydborNlLQKBgG9Za9uaGKfSUiTrK+JK+RxUciS6nYpzFRfZxubEvgTMTWH99AAqApDgsx3xV/cb/YcPv6d61cAF7DskpGZvUr7x/Dd1kmVX48JUAaMfJDsv1xAnAcbwMRFxk8LmQg9wNzxZX+9K5hca9d0dss4XU+Gajijh/MqlNFuDfnar6GRBAoGAApQUNbksyqjLxoMijylFCQd7/be3UKSdDoPQPTS3K5CGohjfBGBUpCqEhvf3hcTjCJf9u03v2et9eMtj/oIvrTXU05E3lShWXnJCLsFjRDc0ei1HbVoAKCHDl6EGVU3uHDAuZsDUnmN2Yw7xeHCJbbGMMYBFe1KgMkjHPRdpmxI=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public static String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwg3RyjciJgMnjjWZRSdAauWcePMWc18GY8RvidfI9165gV06FsJR2lTP302ZRppMgM63uzHg1eVe4x0r8mLI8N+9Y+e0NTn/T3ObKSoT8+NoezVMOClijVwhdeX3LdIaDCFWR5nXynqZmcJcPkzxKiQNQnIH0T+eC6J6j5+nupYtRPBHw8lCl2IlVlqvGK7rXW0Zuo43LTHmpHUB47vY6PS/CNHPZcuF8APECUeqyCVMRq6RV8SoiZFFqp1yPBl4e62aWAihKqAvAEwHecjYRpfpZLovKjWq+nmdSroJ5VRBbOiv1hP+3MZ7wO4ubri98CaNe5e5fWQXCP6pWCuj0wIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    //private String notify_url = "http://rmjufucemb.54http.tech/paid/notify";
    private String notify_url = "http://samchenjava.hopto.org:9000/paid/notify";
    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    private String timeout = "30m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\","
                + "\"total_amount\":\"" + total_amount + "\","
                + "\"subject\":\"" + subject + "\","
                + "\"body\":\"" + body + "\","
                + "\"timeout_express\":\"" + timeout + "\","//video 309
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应：" + result);

        return result;

    }
}
