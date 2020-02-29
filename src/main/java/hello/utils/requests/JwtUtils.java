package hello.utils.requests;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.CharsetUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.AES;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtUtils {

    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private static final String SECRET = "zbbIsSuperman";
    private static final String ISS = "zbb";

    // 过期时间  1小时
    private static final long EXPIRATION = 3600L;

    // 选择了记住我之后的过期
    private static final long EXPIRATION_REMEMBER = 604800L;

    // AES密钥
    @SuppressFBWarnings("MS_PKGPROTECT")
    protected static final byte[] key = "A1B2C3D4E5F6G789".getBytes(StandardCharsets.UTF_8);


    /* Header头部     { "alg": "Algorithm  加密方法：HS256", "cty": "Content Type ", "typ": "Type" , "kid": "Key Id" }
     * payload数据包
     * {  "iss": "Issuer JWT的签发者", 
     *  "aud": "Audience 接收JWT的一方",
     *   "sub": "Subject JWT的主题", 
     *  "exp": "Expiration Time JWT的过期时间",
     *   "nbf": "Not Before 在xxx之间，该JWT都是可用的",
     *   "iat": "Issued At 该JWT签发的时间", 
     *  "jti": "JWT ID JWT的唯一身份标识",
     *   "xxx": "自定义属性"}
     */

    /**
     * @param userDetails 存放username和权限信息，不存隐私
     * @return token
     */
    public static String createToken(UserDetails userDetails, boolean isRememberMe, int second) {
        try {
            String authorities = userDetails.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(":"));

            long expiration;
            if (second > 0) {
                expiration = second;
            } else {
                expiration = isRememberMe ? EXPIRATION_REMEMBER : EXPIRATION;
            }
            Algorithm algorithm = Algorithm.HMAC256(SECRET);
            return JWT.create()
                    .withSubject(userDetails.getUsername())
                    // 签名者,签名防止数据被篡改
                    .withIssuer(ISS)
                    // 设置过期时间
                    .withExpiresAt(DateUtil.offsetMillisecond(new Date(), (int) (expiration * 1000)))
                    // 自定义参数，暂时不用
                    .withClaim("authorities", authorities)
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            System.out.println(exception.getMessage());
            return null;
        }
    }

    /**
     * 验证token 有效性
     *
     * @param token token
     * @return boolean
     */
    public static DecodedJWT verifyToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(SECRET);
        JWTVerifier verifier = JWT.require(algorithm)
                // 验证签发人
                .withIssuer(ISS)
                .build();
        /*
         *   校验：
         * 格式校验： header.payload.signature
         * 加密方式校验 Header中的alg
         * 签名信息校验，防篡改
         * 载体payload 公有声明字段校验
         */
        return verifier.verify(token);
    }

    /**
     * 解码
     */
    public enum TOKEN_PARAMETER {
        ISSUER, SUBJECT, EXPIRATION, CUSTOM
    }

    public static Object decodeToken(String token, TOKEN_PARAMETER parameter) throws JWTDecodeException {
        DecodedJWT jwt = JWT.decode(token);
        Map<String, Claim> claims = jwt.getClaims();
        Claim authorities = claims.get("authorities");
        String issuer = jwt.getIssuer();
        String subject = jwt.getSubject();
        Date expiresAt = jwt.getExpiresAt();
        switch (parameter) {
            case ISSUER:
                return issuer;
            case SUBJECT:
                return subject;
            case EXPIRATION:
                return expiresAt;
            case CUSTOM:
                return authorities.asString();
            default:
                return "";
        }
    }

    // ase加密解密

    public enum AES_METHOD {
        ENCRYPT, DECRYPT
    }

    public static String cryptUseAES(String content, AES_METHOD method) {
        // 构建
        AES aes = new AES(Mode.CBC, Padding.PKCS5Padding, key, key);
        switch (method) {
            case ENCRYPT:
                // 加密为16进制表示
                return aes.encryptHex(content);
            case DECRYPT:
                // 解密为字符串
                return aes.decryptStr(content, CharsetUtil.CHARSET_UTF_8);
            default:
                return "";
        }
    }
}