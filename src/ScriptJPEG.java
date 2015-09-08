import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by Administrator on 2015/8/21.
 */
public class ScriptJPEG extends ScriptBase{

    public ScriptJPEG(File file, File whiteNameFile, String rootPath, File bFile, File logFile,
                     HandleFileListener handleFileListener, String path) {
        this.pngFile = file;
        this.whiteNameFile = whiteNameFile;
        this.rootPath = rootPath;
        this.bFile = bFile;
        this.logFile = logFile;
        this.handleFileListener = handleFileListener;
        this.path = path;
    }

    public ScriptJPEG() {
    }

    @Override
    public void startCompress() {
        long originalFileSize = pngFile.length();
        jpegTran(pngFile);
        long currentFileSize = pngFile.length();
        float ratio = (float) currentFileSize / originalFileSize;
        if (ratio >= 1 || ratio <= 0) {
            ratio = 1;
            restore();
        }

        if (isExecuteError) {
            writeLog("--- cannot resolve this jpegFile " + pngFile.getAbsolutePath() + " ---\n");
            System.out.println("--- cannot resolve this jpegFile " + pngFile.getAbsolutePath() + " ---");
        } else {
            writeLog("currentFile " + pngFile.getAbsolutePath() + " is ---" + (int) (ratio * 100) + "%" + "---  relative to original\n");
            System.out.println("currentFile " + pngFile.getAbsolutePath() + " is ---" + (int) (ratio * 100) + "%" + "---  relative to original");
        }
        handleFileListener.onFinished();
    }

    private String excute(String[] cmd) {
        String result = "";
        try {
            Process ps = Runtime.getRuntime().exec(cmd);
            ps.waitFor();

            BufferedReader br = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            result = sb.toString();
            if (ps.exitValue() < 0) {
                isExecuteError = true;
                restore();
//                writeWhiteName(path, pngFile.getAbsolutePath());
                writeWhiteName(pngFile.getName());
                handleFileListener.onFinished();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void jpegTran(File file) {
        String filePath = file.getAbsolutePath();
        String[] cmd = {"cmd","/c" , rootPath + "\\bin\\jpegtran.bat", filePath, filePath + ".temp"};
        excute(cmd);
        File fileTemp = new File(filePath + ".temp");
        file.delete();
        nioTransferCopy(fileTemp, new File(filePath));
        fileTemp.delete();
    }
}
