import java.io.*;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by liurongchan on 15/7/27.
 */
public class RunShell implements ScriptBase.HandleFileListener{

    private  ExecutorService cachedThreadPool;

    private  File backupFile;

    private  boolean isRoot = true;

    private  String p;

    private File whiteNameFile;
    private File logFile;

    private CompressListener compressListener;
    private int threadCount;

    private ArrayList<File> compressFiles = new ArrayList<>();
    private ArrayList<File> backupFiles = new ArrayList<>();
    private ArrayList<String> whiteNameFiles = new ArrayList<>();

    private String rootName;
    private String toolName;

    private int fileGotHandled = 0;

    public RunShell(String rootName, String toolName, CompressListener compressListener, int threadCount) {
        this.rootName = rootName;
        this.toolName = toolName;
        this.compressListener = compressListener;
        this.threadCount = threadCount;
    }

    public void doTask() {
        cachedThreadPool = Executors.newFixedThreadPool(threadCount);
        File rootFile = new File(rootName);
        File b = new File(rootName + "Backup");
        if (!b.exists()) {
            b.mkdirs();
        }
        whiteNameFile = new File(b, "whiteName.txt");
        logFile = new File(b, "compress_log.txt");
        if (!whiteNameFile.exists()) {
            try {
                whiteNameFile.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        readWhiteNameFiles();
        compressListener.onLogAndWhiteNameCreated(logFile.getAbsolutePath(), whiteNameFile.getAbsolutePath());
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writeLog("\n\n\n=========== new test start !!! ========= currnetTimeis:  " + df.format(new Date()) + "==========");
        traverseFolder1(rootFile);
    }

    public void traverseFolder1(File file) {
        p = file.getAbsolutePath();
        backupFile = new File(file.getParentFile().getAbsolutePath() + "/" + file.getName() + "Backup");
        if (!backupFile.exists()) {
            backupFile.mkdirs();
        }
        if (file.exists()) {
            LinkedList<File> list = new LinkedList<>();
            File[] files = file.listFiles();
            for (File file2 : files) {
                if (file2.isDirectory()) {
                    list.add(file2);
                } else {
                    backup(file2, file);
                }
            }
            isRoot = false;
            File temp_file;
            while (!list.isEmpty()) {
                temp_file = list.removeFirst();
                files = temp_file.listFiles();
                for (File file2 : files) {
                    if (file2.isDirectory()) {
                        list.add(file2);
                    } else {
                        backup(file2, temp_file);
                    }
                }
            }
        } else {
            System.out.println("file not found");
        }
        if (backupFiles.size() != 0) {
            compress();
        } else {
            compressListener.onFinished(0, 0);
        }
    }

    private void compress() {
        for (int i = 0; i < backupFiles.size(); i++) {
            int type;
            if (compressFiles.get(i).getAbsolutePath().endsWith(".png")) {
                type = 0;
            } else {
                type = 1;
            }
            if (!whiteNameFiles.contains(compressFiles.get(i).getAbsolutePath())) {
                CompressRunnable runnable = new CompressRunnable(compressFiles.get(i), whiteNameFile, toolName, backupFiles.get(i), logFile, this, type);
                cachedThreadPool.execute(runnable);
            } else {
                fileGotHandled++;
                compressListener.onProcess(backupFiles.size(), fileGotHandled);
            }
        }
    }


    private class CompressRunnable implements Runnable {
        public File pngFile;
        public File whiteNameFile;
        public String toolName;
        public File backupFile;
        public File logFile;
        public ScriptPNG.HandleFileListener handleFileListener;
        public int pngOrJpeg; // 0 png   1 jpeg

        public CompressRunnable(File pngFile, File whiteNameFile,
                                String toolName, File backupFile,
                                File logFile, ScriptPNG.HandleFileListener handleFileListener, int pngOrJpeg) {
            this.pngFile = pngFile;
            this.whiteNameFile = whiteNameFile;
            this.toolName = toolName;
            this.backupFile = backupFile;
            this.logFile = logFile;
            this.handleFileListener = handleFileListener;
            this.pngOrJpeg = pngOrJpeg;
        }
        @Override
        public void run() {
            ScriptBase scriptBase = null;
            switch (pngOrJpeg) {
                case 0:
                    scriptBase = new ScriptPNG(pngFile, whiteNameFile, toolName, backupFile, logFile, handleFileListener);
                    break;
                case 1:
                    scriptBase = new ScriptJPEG(pngFile, whiteNameFile, toolName, backupFile, logFile, handleFileListener);
            }
            scriptBase.startCompress();
        }
    }


    private void backup(final File pngFile, File currentDirectory) {
        String fileName = pngFile.getName();
        File pngBackup;
        if ((fileName.endsWith(".png") && !fileName.endsWith(".9.png")) || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            if (!isRoot) {
                String currentDirectoryPath = currentDirectory.getAbsolutePath();
                String xxx = currentDirectoryPath.replace(p, backupFile.getAbsolutePath());
                File pngBackupDirectory = new File(xxx);
                if (!pngBackupDirectory.exists()) {
                    pngBackupDirectory.mkdirs();
                }
                pngBackup = new File(xxx + "/" + pngFile.getName());
                System.out.println(pngBackup.getAbsolutePath());
            } else {
                pngBackup = new File(backupFile.getAbsolutePath() + "/" + pngFile.getName());
                System.err.println(pngBackup.getAbsolutePath());
            }
            backupFiles.add(pngBackup);
            compressFiles.add(pngFile);
            nioTransferCopy(pngFile, pngBackup);
        }
    }

    private void nioTransferCopy(File source, File target) {
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

    private void writeLog(String content) {
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

    @Override
    public void onFinished() {
        fileGotHandled++;
        compressListener.onProcess(backupFiles.size(), fileGotHandled);
        if (fileGotHandled >= backupFiles.size()) {
            compressListener.onFinished(backupFiles.size(), fileGotHandled);
            backupFiles.clear();
            compressFiles.clear();
        }
    }

    public interface CompressListener {
        void onProcess(long total, long done);
        void onFinished(long total, long done);
        void onLogAndWhiteNameCreated(String log, String whiteName);
    }

    private void readWhiteNameFiles() {
        try {
            FileReader reader = new FileReader(whiteNameFile);
            BufferedReader br = new BufferedReader(reader);
            String fileName = null;
            while((fileName = br.readLine()) != null) {
                if (!fileName.equals("")) {
                    whiteNameFiles.add(fileName);
                }
            }
            br.close();
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
