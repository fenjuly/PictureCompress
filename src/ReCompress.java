import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.file.Paths;

/**
 * Created by Administrator on 2015/8/20.
 */
public class ReCompress extends JFrame implements ActionListener, RunShell.CompressListener {

    // 定义面板组件
    JPanel jp[] = new JPanel[8];
    // 定义标签组件
    JLabel jlb[] = new JLabel[7];
    // 定义按钮组件
    JButton jb[] = new JButton[6];
    // 定义文本框组件
    JTextField jtf;
    // 定义密码框组件
    JTextField jpf;

    JLabel progressLabel;
    JPanel progressP;

    JPanel threadP;
    JLabel threadLabel;
    JTextField threadT;

    String log;
    String whiteName;

    long startTime = 0;
    long endTime = 0;

    private boolean isStarted = false;

    public static void main(String[] args) {
        ReCompress aa = new ReCompress();
        aa.setToolPath();
    }

    private void setToolPath() {

        for (int i = 0; i < 8; i++) {
            jp[i] = new JPanel();
        }
        jlb[0] = new JLabel("这里是顶部");
        jlb[1] = new JLabel("这里是左边");
        jlb[2] = new JLabel("这里是右部");
        jlb[3] = new JLabel("这里是底部");
        jlb[4] = new JLabel("这里是中部");
        jlb[5] = new JLabel("Picture Path(png or jpg)");
        jlb[6] = new JLabel("Tool Path");
        jtf = new JTextField(20);
        jpf = new JTextField(20);
        jb[0] = new JButton("confirm");
        jb[1] = new JButton("cancle");

        jb[4] = new JButton("open log file");
        jb[4].setActionCommand("log");
        jb[4].addActionListener(this);

        jb[5] = new JButton("open whitename file");
        jb[5].setActionCommand("whitename");
        jb[5].addActionListener(this);

        jb[2] = new JButton("...");
        jb[2].setActionCommand("pngpath");
        jb[2].addActionListener(this);

        jb[3] = new JButton("...");
        jb[3].setActionCommand("toolpath");
        jb[3].addActionListener(this);

        // 添加组件到JPanel
        jp[0].add(jlb[0]);
        jp[1].add(jlb[1]);
        jp[2].add(jlb[2]);
        jp[3].add(jlb[3]);
        jp[4].add(jlb[4]);
        jp[5].add(jlb[5]);
        jp[5].add(jtf);
        jp[5].add(jb[2]);
        jp[6].add(jlb[6]);
        jp[6].add(jpf);
        jp[6].add(jb[3]);
        jp[7].add(jb[0]);
        jp[7].add(jb[1]);
        jp[7].add(jb[4]);
        jp[7].add(jb[5]);

        this.setLayout(new GridLayout(5, 1, 15, 15));
        threadP = new JPanel();
        threadLabel = new JLabel("set the thread number(default is 10)");
        threadT = new JTextField(3);
        threadP.add(threadLabel);
        threadP.add(threadT);
        this.add(threadP);

        this.add(jp[5]);
        this.add(jp[6]);

        progressP = new JPanel();
        progressLabel = new JLabel("detail:");
        progressP.add(progressLabel);
        progressP.setVisible(false);
        this.add(progressP);

        this.add(jp[7]);

        jb[0].addActionListener(this);
        jb[1].addActionListener(this);

        jb[0].setActionCommand("confirm");
        jb[1].setActionCommand("cancle");

        this.setTitle("Picture Compress");// 标题
        this.setSize(500, 300);// 大小
        this.setLocation(300, 300);// 设置初始位置
        this.setResizable(false);// 固定窗体大小
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);// 关闭窗口时退出jvm
        this.setVisible(true);// 显示窗体

        jpf.setText(Paths.get("").toAbsolutePath().toString());
    }

    public ReCompress() {
        //创建组件
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("confirm")) {
            int thread = 10;
            boolean isContinue = true;
            String str_thread = threadT.getText();
            if (str_thread.equals("")) {
            } else if (!isNumeric(str_thread)) {
                isContinue = false;
                JOptionPane.showMessageDialog(null, "please input Numeric into thread textfield!!!");
            } else {
                thread = Integer.valueOf(str_thread);
            }

            if (isContinue) {
                if (jtf.getText().equals("") || jpf.getText().equals("")) {
                    JOptionPane.showMessageDialog(null, "please choose file folder: target or tool!");
                } else {
                    jb[0].setVisible(false);
                    progressP.setVisible(true);
                    progressLabel.setText("backuping files...");
                    startTime = System.currentTimeMillis();
                    isStarted = true;
                    new Thread(new CompressRunnable(jtf.getText(), jpf.getText(), this, thread)).start();
                }
            }
        } else if(e.getActionCommand().equals("cancle")){
            System.exit(0);
        } else if (e.getActionCommand().equals("pngpath")) {
            jtf.setText(chooseFile());
        } else if (e.getActionCommand().equals("toolpath")) {
            jpf.setText(chooseFile());
        } else if (e.getActionCommand().equals("log")) {
            if (isStarted) {
                String[] cmd = {"cmd", "/c", log};
                excute(cmd);
            }
        } else if (e.getActionCommand().equals("whitename")) {
            if (isStarted) {
                String[] cmd = {"cmd", "/c", whiteName};
                excute(cmd);
            }
        }
    }

    private String chooseFile() {
        //文件选择器
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        //获取用户的操作结果，是确认了，还是取消了
        int choose = chooser.showOpenDialog(null);
        //判断选择的结果
        if(choose == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return "";
    }

    @Override
    public void onProcess(long total, long done) {
        progressLabel.setText("processing-------done:" + done + "  total:" + total);
    }

    @Override
    public void onFinished(long total, long done) {
        String content;
        endTime = System.currentTimeMillis();
        long cost = endTime - startTime;
        if (cost < 1000) {
            content = "processing-------done:" + done + "  total:" + total
                      + "   "
                      + "cost time: < 1s";
        } else if (cost < 1000 * 60){
            content = "processing-------done:" + done + "  total:" + total
                    + "   "
                    + "cost time: " + (float)cost / 1000 + "s";
        } else {
            long min = cost / 60000;
            content = "processing-------done:" + done + "  total:" + total
                    + "   "
                    + "cost time: " + min + "min" + (cost - min*1000)/ 1000 + "s";
        }
        progressLabel.setText(content);
        jb[0].setVisible(true);
        JOptionPane.showMessageDialog(this, "Completed!!!");
    }

    @Override
    public void onLogAndWhiteNameCreated(String log, String whiteName) {
        this.log = log;
        this.whiteName = whiteName;
    }

    public static boolean isNumeric(String str){
        for (int i = str.length();--i>=0;){
            if (!Character.isDigit(str.charAt(i))){
                return false;
            }
        }
        return true;
    }

    class CompressRunnable implements Runnable {
        String path1;
        String path2;
        RunShell.CompressListener compressListener;
        int thread;

        public CompressRunnable(String path1, String path2, RunShell.CompressListener compressListener, int thread) {
            this.path1 = path1;
            this.path2 = path2;
            this.compressListener = compressListener;
            this.thread = thread;
        }

        @Override
        public void run() {
            RunShell runShell = new RunShell(path1, path2, compressListener, thread);
            runShell.doTask();
        }
    }

    private void excute(String[] cmd) {
        try {
            Process ps = Runtime.getRuntime().exec(cmd);
            ps.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


