package com.colonelhedgehog.lavacontrol.core;

import com.colonelhedgehog.lavacontrol.core.components.JCheckBoxList;
import com.colonelhedgehog.lavacontrol.core.components.RoundedCornerBorder;
import com.colonelhedgehog.lavacontrol.core.system.mac.MacSetup;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

public class Main
{

    private static ImageIcon icon;
    public static PrefsGUI prefsGUI;
    public static JDialog prefsFrame;

    public static void main(String[] args)
    {
        settings = new Settings();
        settings.reloadSettings();
        System.out.println("[Lava Control] Loading...");

        if(isMac())
        {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Lava Control");
        }


        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e)
        {
            System.out.println("[Lava Control] Wasn't able to set look-and-feel. Your system may not be fully supported. ");
            //e.printStackTrace();
        }

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                settings.saveSettings();
                if (mainGUI.getProcess() != null && consoleGUI.getConsoleThread().isAlive())
                {
                    mainGUI.getProcess().destroy();
                }
                else if (consoleGUI != null && consoleGUI.getConsoleThread().isAlive())
                {
                    consoleGUI.getConsoleThread().interrupt();
                }
            }
        });

        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                createMainGUI();
            }
        });

        icon = new ImageIcon(Main.class.getResource("/media/logo.png"));

        if(System.getProperty("os.name").toLowerCase().toLowerCase().contains("mac"))
        {
            MacSetup.createMacSettings();
        }
    }

    public static JFrame menuFrame;
    public static JFrame consoleFrame;
    public static JDialog searchDialog;

    public static MainGUI mainGUI = null;
    public static FileGUI fileGUI = null;
    public static SearchGUI searchGUI = null;
    public static ConsoleGUI consoleGUI = null;
    public static Settings settings;

    public static void createConsoleGUI()
    {
        JButton launchJar = Main.mainGUI.getLaunchJar();
        launchJar.setEnabled(false);
        launchJar.setToolTipText("You can't have more than one console open per instance of Lava Control. If you want to use Lava Control on multiple servers, simply launch this Lava Control Jar again along side this one.");

        consoleFrame = new JFrame("Lava Control | Console");
        System.out.println("[Lava Control] Creating Lava Control's Console GUI...");

        if(isMac())
        {
            MacSetup.setCanFullscreenWindow(consoleFrame, true);
        }

        consoleGUI = new ConsoleGUI();


        final ImageIcon scaled = new ImageIcon(getIcon().getImage().getScaledInstance(96, 96, java.awt.Image.SCALE_SMOOTH));

        consoleFrame.setContentPane(consoleGUI.getConsolePanel());
        consoleFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        consoleFrame.setBounds(0, 0, 900, 600);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        consoleFrame.setLocation(dim.width / 2 - consoleFrame.getSize().width / 2, dim.height / 2 - consoleFrame.getSize().height / 2);
        consoleFrame.setVisible(true);
        consoleFrame.addWindowListener(new WindowListener()
        {
            @Override
            public void windowOpened(WindowEvent e)
            {

            }

            @Override
            public void windowClosing(WindowEvent e)
            {
                if(settings.getAskExportLog())
                {
                    File cPath = new File(mainGUI.getJarPath().getText());
                    int result = JOptionPane.showConfirmDialog(consoleFrame, "Do you want to save your Lava Control log before exiting?" +
                            "\nThe contents of the console will be cleared if you do not." +
                            "\n(Your server-generated logs were automatically saved to: " +
                            "\n\"" + cPath.getParentFile().getAbsolutePath() + "/logs\")", "Do you want to save your Lava Control log before exiting?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, scaled);

                    if (result == JOptionPane.YES_OPTION)
                    {
                        consoleGUI.getExportLogButton().doClick();
                    }
                    else
                    {
                        e.getWindow().setVisible(false);
                    }
                }
            }

            @Override
            public void windowClosed(WindowEvent e)
            {
            }

            @Override
            public void windowIconified(WindowEvent e)
            {

            }

            @Override
            public void windowDeiconified(WindowEvent e)
            {

            }

            @Override
            public void windowActivated(WindowEvent e)
            {

            }

            @Override
            public void windowDeactivated(WindowEvent e)
            {

            }
        });

        MessageConsole mc = new MessageConsole(consoleGUI.getConsoleText());
        mc.redirectOut();
        mc.redirectErr(Color.RED, null);

        int ml = Main.getSettings().getMaxConsoleLines();
        mc.setMessageLines(ml);
    }

    public static ImageIcon getIcon()
    {
        return icon;
    }
    private static void createMainGUI()
    {
        Thread uithread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {

                menuFrame = new JFrame("Lava Control | Main");

                if(isMac())
                {
                    MacSetup.setCanFullscreenWindow(menuFrame, true);
                }

                menuFrame.setIconImage(icon.getImage());

                if (mainGUI == null)
                {
                    mainGUI = new MainGUI();
                }

                JPanel mainPanel = mainGUI.getMainPanel();
                menuFrame.setContentPane(mainPanel);
                menuFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                menuFrame.setBounds(0, 0, 900, 600);
                Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                menuFrame.setLocation(dim.width / 2 - menuFrame.getSize().width / 2 - 10, dim.height / 2 - menuFrame.getSize().height / 2 - 10);
                menuFrame.setVisible(true);
                System.out.println("[Lava Control] Created Lava Control's GUI.");

                mainGUI.createUIComponents();
                listJars(new File(new File(Main.getSettings().getLastPath()).getParent() + "/plugins"));
                mainGUI.getPluginList().scrollRectToVisible(new Rectangle(mainGUI.getPluginList().getSize()));
                mainGUI.getPluginList().setMinimumSize(mainGUI.getPluginList().getSize());
                mainGUI.getPluginList().setPreferredSize(mainGUI.getPluginList().getSize());
                mainGUI.getPluginList().setMaximumSize(mainGUI.getPluginList().getSize());
                mainGUI.getPluginList().getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);

                mainGUI.getJarPath().setBorder(new RoundedCornerBorder(6));
                mainGUI.getPluginList().setBorder(new RoundedCornerBorder(6));


            }
        });
        uithread.start();
    }

    public static void listJars(File f)
    {
        if (Main.getSettings().getSSHEnabled())
        {
            return;
        }

        JCheckBox firstBox = null;
        DefaultListModel<JCheckBox> model = new DefaultListModel<>();
        if (mainGUI.getCheckList() != null)
        {
            //System.out.println("Already exists lol: " + mainGUI.checkList.getName());
            mainGUI.getPluginList().remove(mainGUI.getCheckList());
        }
        //mainGUI.getPluginList().repaint();

        File[] files = new File(f.getPath()).listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.getName().endsWith(".jar") || file.getName().endsWith("._jar"))
                {
                    JCheckBox cb = new JCheckBox(file.getName());

                    if (firstBox == null)
                    {
                        firstBox = cb;
                    }

                    cb.setSelected(file.getName().endsWith(".jar"));
                    cb.setVisible(true);
                    cb.setText(file.getName());
                    model.addElement(cb);
                    mainGUI.getPluginList().validate();
                    mainGUI.getPluginList().repaint();
                }
            }
        }

        JCheckBoxList jCheckBoxList = new JCheckBoxList(model, mainGUI.getJarPath().getText());
        jCheckBoxList.setName("pluginCheckboxList");
        jCheckBoxList.setSize(mainGUI.getPluginList().getSize());
        mainGUI.getPluginList().add(jCheckBoxList);
        mainGUI.setChecklist(jCheckBoxList);
        mainGUI.getPluginList().setViewportView(mainGUI.getCheckList());
        //jCheckBoxList.setVisible(true);
    }

    public static void createPrefsGUI()
    {
        settings.reloadSettings();

        prefsGUI = new PrefsGUI();
        prefsFrame = new JDialog(menuFrame, "Lava Control | Options");
        prefsFrame.setContentPane(prefsGUI.getPrefsPanel());
        prefsFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        prefsFrame.setResizable(false);
        prefsFrame.setBounds(0, 0, 680, 300);
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        prefsFrame.setLocation(dim.width / 2 - prefsFrame.getSize().width / 2 - 10, dim.height / 2 - prefsFrame.getSize().height / 2 - 10);
        prefsFrame.setVisible(true);
    }

    public static void createFileGUI()
    {
        System.out.println("[Lava Control] Creating Lava Control's File Chooser GUI...");
        fileGUI = new FileGUI();
    }

    public static boolean isMac()
    {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    public static Settings getSettings()
    {
        return settings;
    }
}
