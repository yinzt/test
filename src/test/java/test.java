import lombok.val;
import sun.misc.BASE64Encoder;
import javax.crypto.Cipher;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
/**
 * 测试类
 */
public class test {
    
    public static void main(String[] args) {

    }
    /**
     * 设置计划任务
     * 
     * @param imgfilename 映像文件
     * @param type 类型
     * @param option 属性
     * @param password 密码
     * @return 返回映像字符串
     */
    public String setRecgnPlainParam(String imgfilename, String type, String option, String password) {
        String imgBase64Str = GetBase64StrFromImage(imgfilename);
        return String.valueOf(imgBase64Str) + "==##" + type + "==##" + option + "==##" + password;
    }
    /**
     * 获取映像base64
     * @param imagePath 映像地址
     * @return 返回bas64字符串
     */
    public static String GetBase64StrFromImage(String imagePath) {
        String imgFile = imagePath;
        InputStream in = null;
        byte[] data = (byte[])null;
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }
}
