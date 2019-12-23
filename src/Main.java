import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }

    private static void createAndShowGUI() {
        JFrame jframe = new JFrame("Showdoc Parser");   //窗口
        final JPanel jpanel1 = new JPanel();           //一个面板
        JButton jbutton1 = new JButton("Choose File");  //按钮，单击响应事件，打开文件选择器
        final JFileChooser jfilechooser1 = new JFileChooser("F:\\"); //文件选择器，设置初始路径为"F:\\"

        class myactionlistener implements ActionListener  //创建动作监听者者
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                //		int a = jfilechooser1.showOpenDialog(null); //选择文件，选择路径
                int a = jfilechooser1.showSaveDialog(null);  //保存文件，指定路径
                if (a == JFileChooser.APPROVE_OPTION){
                    String path = jfilechooser1.getSelectedFile().getPath();
                    try {
                        String result = SL.run(path);
                        File file = new File(path+".txt");
                        if(file.exists()){
                            file.delete();
                        }
                        try {
                            file.createNewFile();
                            FileOutputStream outputStream = new FileOutputStream(file);
                            outputStream.write(result.getBytes());
                            outputStream.flush();
                            outputStream.close();
                            Desktop.getDesktop().open(file);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } catch (Exception e1){
                        JOptionPane.showMessageDialog(jpanel1, e1.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }



        //添加
        jbutton1.addActionListener(new myactionlistener());

        jpanel1.setLayout(new BorderLayout(10,10));

        jpanel1.add(jbutton1, BorderLayout.CENTER);   //面板添加按钮


        jframe.setResizable(false);
        jframe.add(jpanel1);    //窗口添加面板
        jframe.setBounds(400, 200, 400, 200);  //设置窗口大小
        jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jframe.setVisible(true);
    }

}
