import java.io.*;
import java.nio.channels.FileChannel;

/**
 * Created by Administrator on 2015/8/18.
 */

public class ScriptPNG extends ScriptBase {

    public static final long MAX_SIZE = 65536;
    public static final long MIN_SIZE = 16384;

    private String zlibMemlevel;
    private String zlibStrategy;
    private int colorType;
    private int zipCompression;


    public ScriptPNG(File file, File whiteNameFile, String rootPath, File bFile, File logFile,
                     HandleFileListener handleFileListener) {
        this.pngFile = file;
        this.whiteNameFile = whiteNameFile;
        this.rootPath = rootPath;
        this.bFile = bFile;
        this.logFile = logFile;
        this.handleFileListener = handleFileListener;
    }

    public ScriptPNG() {
    }

    @Override
    public void startCompress() {
        long originalFileSize = pngFile.length();
        if (truePng(pngFile)) {
            if(checkColorType(pngFile)) {
                cryoPng(pngFile);
                pngWolf(pngFile);
                deflopt(pngFile);
                long currentFileSize = pngFile.length();
                float ratio = (float) currentFileSize / originalFileSize;
                if (ratio >= 1) {
                    restore();
                }
                if (isExecuteError) {
                    writeLog("--- cannot resolve this pngFile " + pngFile.getAbsolutePath() + " ---\n");
                    System.out.println("--- cannot resolve this pngFile " + pngFile.getAbsolutePath() + " ---");
                } else {
                    writeLog("currentFile " + pngFile.getAbsolutePath() + " is ---" + (int) (ratio * 100) + "%" + "---  relative to original\n");
                    System.out.println("currentFile " + pngFile.getAbsolutePath() + " is ---" + (int) (ratio * 100) + "%" + "---  relative to original");
                }
            } else {
                writeLog("--- cannot resolve this pngFile " + pngFile.getAbsolutePath() + " ---\n");
                System.out.println("--- cannot resolve this pngFile " + pngFile.getAbsolutePath() + " ---");
            }
        } else {
            writeLog("--- cannot resolve this pngFile " + pngFile.getAbsolutePath() + " ---\n");
            System.out.println("--- cannot resolve this pngFile " + pngFile.getAbsolutePath() + " ---");
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
                writeWhiteName(pngFile.getAbsolutePath());
                handleFileListener.onFinished();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private boolean truePng(File file) {
        String[] cmd = {"cmd","/c" , rootPath + "\\bin\\truepng.bat", file.getAbsolutePath()};
        String result = excute(cmd);
        String temp;
        try {
            temp = result.substring(result.indexOf("best:"));
            zlibMemlevel = temp.substring(temp.indexOf("zm") + 3, temp.indexOf("zs")).replace("\t", "");
            zlibStrategy = temp.substring(temp.indexOf("zs") + 3, temp.indexOf("fs")).replace("\t", "");
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            restore();
            writeLog(e.toString());
            writeWhiteName(file.getAbsolutePath());
            return false;
        }
        return true;
    }

    private boolean checkColorType(File file) {
        String[] cmd = {"cmd","/c" , rootPath + "\\bin\\pngout.bat", file.getAbsolutePath()};
        String result = excute(cmd);
        try {
            colorType = Integer.valueOf(result.substring(result.indexOf("/c") + 2, result.indexOf("/f")).replace(" ", ""));
        } catch (StringIndexOutOfBoundsException e) {
            e.printStackTrace();
            restore();
            writeLog(e.toString());
            writeWhiteName(file.getAbsolutePath());
            return false;
        }
        return true;
    }

    private void cryoPng(File file) {
        long fileSize = file.length();
        String[] cmd = {"cmd","/c" , rootPath + "\\bin\\cryopng.bat", file.getAbsolutePath()};
        if (colorType == 6) {
            excute(cmd);
        }
        zipCompression = 2;
        if (fileSize > MAX_SIZE) {
            zipCompression = 1;
        } else if (fileSize < MIN_SIZE) {
            zipCompression = 4;
        }
    }

    private void pngWolf(File file) {
        String[] cmd = {"cmd","/c" , rootPath + "\\bin\\pngwolf.bat", file.getAbsolutePath(), zlibStrategy, zlibMemlevel, String.valueOf(zipCompression)};
        excute(cmd);
    }

    private void deflopt(File file) {
        String[] cmd = {"cmd","/c" , rootPath + "\\bin\\deflopt.bat", file.getAbsolutePath()};
        excute(cmd);
    }

}