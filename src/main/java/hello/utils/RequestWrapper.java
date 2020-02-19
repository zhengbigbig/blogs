package hello.utils;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.io.IOUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RequestWrapper extends HttpServletRequestWrapper {
    //保存流中的数据
    private byte[] data;

    public RequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        //从流中获取数据
        data = IOUtils.toByteArray(request.getInputStream());
    }

    public ServletInputStream getInputStream() {
        //在调用getInputStream函数时，创建新的流，包含原先数据流中的信息，然后返回
        return new MyServletInputStream(new ByteArrayInputStream(data));
    }
    @SuppressFBWarnings("SIC_INNER_SHOULD_BE_STATIC")
    class MyServletInputStream extends ServletInputStream {
        private InputStream inputStream;

        MyServletInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }


        @Override
        public boolean isFinished() {
            return false;
        }

        @Override
        public boolean isReady() {
            return false;
        }

        @Override
        public void setReadListener(ReadListener readListener) {

        }
    }
}