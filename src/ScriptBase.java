import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by Administrator on 2015/8/21.
 */
public abstract class ScriptBase {

    protected String rootPath;

    protected File pngFile;
    protected File whiteNameFile;
    protected File bFile;
    protected File logFile;

    protected HandleFileListener handleFileListener;

    protected boolean isExecuteError = false;

    public abstract void startCompress();

    protected void writeWhiteName(String content) {
        try {
            FileWriter fw = new FileWriter(whiteNameFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(content + "\n");
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void writeLog(String content) {
        try {
            FileWriter fw = new FileWriter(logFile, true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append(content + "\n\n");
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void restore() {
        String needToDeletePath = pngFile.getAbsolutePath();
        pngFile.delete();
        nioTransferCopy(bFile, new File(needToDeletePath));
    }

    protected void nioTransferCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                inStream.close();
                in.close();
                outStream.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public interface HandleFileListener {
        void onFinished();
    }
}
