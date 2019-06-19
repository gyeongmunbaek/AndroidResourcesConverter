package kr.gyeongmunbaek;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class ResourceConverter {

    private JFrame mMainFrame = null;
    private JTextArea mEditor = null;
    private DropTarget mTarget = null;
    private JComboBox<String> mTypeList = null;
    private JComboBox<String> mResolutionList = null;

    private ResolutionManager mResolutionManager = null;
    private DimenConverter mXMLFileManager = null;
    private ImageResizer mImageResizer = null;

    private ResourceType mResourceType = ResourceType.DIMEN;
    private ArrayList<File> mAvailableFileList = new ArrayList<File>();

    public static void main(String[] args) {
        new ResourceConverter();
    }

    public ResourceConverter() {
        mResolutionManager = new ResolutionManager();
        mResolutionManager.setResourceType(mResourceType);
        mXMLFileManager = new DimenConverter();
        mImageResizer = new ImageResizer();

        mMainFrame = new JFrame("Resources Conveter v1.0");
        JPanel lJPanel = new JPanel();
        JLabel lTypeLabel = new JLabel("Resources Type : ", JLabel.RIGHT);
        lJPanel.add(lTypeLabel);

        String[] lTypeArray = new String[ResourceType.values().length];
        for (int index = 0; index < lTypeArray.length; index++) {
            lTypeArray[index] = ResourceType.values()[index].getString();
        }

        mTypeList = new JComboBox<String>(lTypeArray);
        mTypeList.setSelectedIndex(mResourceType.ordinal());
        mTypeList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> cb = (JComboBox) e.getSource();
                String lItem = (String) cb.getSelectedItem();
                setStandartChoice(lItem);
            }
        });
        lJPanel.add(mTypeList);

        mResolutionList = new JComboBox<String>(
                mResolutionManager.getResolutionArray());
        mResolutionList.setSelectedIndex(mResolutionManager.getDefaultIndex());
        mResolutionList.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox<String> cb = (JComboBox) e.getSource();
                String lItem = (String) cb.getSelectedItem();
                mResolutionManager.setStandardResolution(lItem);
            }
        });

        setStandartChoice(mResourceType.getString());

        JLabel lResolutionLabel = new JLabel(
                "Input Resolution of Resources : ", JLabel.RIGHT);
        lJPanel.add(lResolutionLabel);

        lJPanel.add(mResolutionList);
        lJPanel.setLayout(new GridLayout(2, 2, 5, 5));
        mMainFrame.getContentPane().add(lJPanel, BorderLayout.NORTH);

        mEditor = new JTextArea();
        mEditor.setText("Drag and Drop : File or Directory");

        mTarget = new DropTarget(mEditor, DnDConstants.ACTION_COPY_OR_MOVE,
                new DropTargetListener() {
                    public void dropActionChanged(DropTargetDragEvent dtde) {
                        // do nothing.
                    }

                    public void dragEnter(DropTargetDragEvent dtde) {
                        // do nothing.
                    }

                    public void dragExit(DropTargetEvent dtde) {
                        // do nothing.
                    }

                    public void dragOver(DropTargetDragEvent dtde) {
                        // do nothing.
                    }

                    public void drop(DropTargetDropEvent dtde) {
                        if ((dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) != 0) {
                            dtde.acceptDrop(dtde.getDropAction());
                            Transferable tr = dtde.getTransferable();
                            try {
                                java.util.List list = (java.util.List) tr
                                        .getTransferData(DataFlavor.javaFileListFlavor);

                                mAvailableFileList.clear();

                                mEditor.setText("Path : \n");
                                for (int index = 0; index < list.size(); index++) {
                                    File file = (File) list.get(index);
                                    mEditor.append("-" + file.getAbsolutePath() + "\n");
                                    setAvailableResourceFile(file.getAbsolutePath());
                                }
                                mEditor.append("\n");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }, true, null);

        mMainFrame.getContentPane().add(new JScrollPane(mEditor),
                BorderLayout.CENTER);

        JButton lConvertBtn = new JButton("Convert Resources");
        lConvertBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                boolean lResult = convertResources();
                showDialog(lResult);
            }
        });

        mMainFrame.add(lConvertBtn, BorderLayout.SOUTH);

        mMainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mMainFrame.setPreferredSize(new Dimension(500, 250));
        mMainFrame.setLocation(100, 100);
        mMainFrame.pack();
        mMainFrame.setVisible(true);
    }

    private void setStandartChoice(String pString) {
        if (pString.equalsIgnoreCase(ResourceType.DIMEN.getString())) {
            mResourceType = ResourceType.DIMEN;
        } else if (pString.equalsIgnoreCase(ResourceType.IMAGE.getString())) {
            mResourceType = ResourceType.IMAGE;
        }
        mResolutionManager.setResourceType(mResourceType);
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<String>(
                mResolutionManager.getResolutionArray());
        mResolutionList.setModel(model);
        mResolutionList.setSelectedIndex(mResolutionManager.getDefaultIndex());
    }

    static int fileCnt = 0, folderCnt = 0;
    static long sizeSum = 0;

    private void setAvailableResourceFile(String pPath) {
        File lDir = new File(pPath);
        File[] list = lDir.listFiles();

        if (list == null) {
            list = new File[1];
            list[0] = lDir;
        }

        for (File lFile : list) {
            if (lFile.isDirectory()) {
                setAvailableResourceFile((lDir.getAbsoluteFile()) + "");
            } else if (lFile.isFile()) {
                if (mResourceType.equals(ResourceType.DIMEN)
                        && lFile.getName().equalsIgnoreCase("dimens.xml")) {
                    mAvailableFileList.add(lFile);
                } else if (mResourceType.equals(ResourceType.IMAGE)
                        && isImageFile(lFile)) {
                    mAvailableFileList.add(lFile);
                }
            }
        }

        return;
    }

    private boolean isImageFile(File pFile) {
        String lFileName = pFile.getName();
        String lFormat = lFileName.substring(lFileName.lastIndexOf(".") + 1);
        if (lFormat.equalsIgnoreCase("jpg") || lFormat.equalsIgnoreCase("png")) {
            return true;
        }
        return false;
    }

    private boolean convertResources() {
        if (mAvailableFileList.size() == 0) {
            return false;
        }
        for (int index = 0; index < mAvailableFileList.size(); index++) {
            try {
                File lResourceFile = mAvailableFileList.get(index);
                if (lResourceFile != null) {
                    if (mResourceType.equals(ResourceType.DIMEN)) {
                        char buf[] = new char[1024];
                        BufferedReader in = new BufferedReader(new FileReader(
                                lResourceFile));
                        int n = -1;
                        while ((n = in.read(buf, 0, 1024)) != -1) {
                            mEditor.append(new String(buf, 0, n));
                        }
                        in.close();

                        makeDimenFiles(lResourceFile);
                    } else if (mResourceType.equals(ResourceType.IMAGE)) {
                        makeImageFiles(lResourceFile);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void makeDimenFiles(File pFile) {
        for (int index = 0; index < mResolutionManager.getResolutionArray().length; index++) {
            float lRatio = mResolutionManager.getRatio(index);
            mXMLFileManager
                    .convertDimenXMLFile(pFile.getAbsolutePath(), lRatio, mResolutionManager.getFeatureText());
            String lPath = getRootDirectory(pFile.getAbsolutePath());
            lPath += ("-" + mResolutionManager.getResolutionArray()[index]);
            mXMLFileManager.mergeDimenXMLFile(lPath + "/" + pFile.getName(), mResolutionManager.getFeatureText());
            mXMLFileManager.createXMLFile(lPath, pFile.getName());
        }
        /* mXMLFileManager
                .convertDimenXMLFile(pFile.getAbsolutePath(), 1.0f, mResolutionManager.getFeatureText());
        String lValuesPath = (getRootDirectory(pFile.getAbsolutePath()) + "\\" + pFile.getName());
        mEditor.append("\n\n\n" + lValuesPath + "\n");
        mXMLFileManager.mergeDimenXMLFile(lValuesPath, mResolutionManager.getFeatureText());
        mXMLFileManager.createXMLFile(getRootDirectory(pFile.getAbsolutePath()), pFile.getName()); */
    }

    private void makeImageFiles(File pFile) {
        for (int index = 0; index < mResolutionManager.getResolutionArray().length; index++) {
            float lRatio = mResolutionManager.getRatio(index);

            try {
                mImageResizer.process(pFile, lRatio);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String lPath = getRootDirectory(pFile.getAbsolutePath());
            lPath += ("-" + mResolutionManager.getResolutionArray()[index]);
            String lFileName = pFile.getName();
            if (Pattern.matches("^[0-9]+$", lFileName.substring(0, 1))) {
                lFileName = "light_" + lFileName;
            }
            mImageResizer.createImageFile(lPath, lFileName);
        }
    }

    private String getRootDirectory(String pPath) {
        String OS = System.getProperty("os.name").toLowerCase();
        String phase = "\\";
        if (OS.indexOf("mac") >= 0) {
            phase = "/";
        }

        int lLastIndex = pPath.lastIndexOf(phase);
        if (lLastIndex != -1) {
            pPath = pPath.substring(0, lLastIndex);
        }
        lLastIndex = pPath.lastIndexOf("-w");
        if (lLastIndex != -1) {
            pPath = pPath.substring(0, lLastIndex);
        }
        if (pPath.endsWith("dpi")) {
            lLastIndex = pPath.lastIndexOf("-");
            if (lLastIndex != -1) {
                pPath = pPath.substring(0, lLastIndex);
            }
        }
        return pPath;
    }

    private void showDialog(boolean pIsSuccess) {
        final JDialog lInfoDialog = new JDialog(mMainFrame, "Information", true);
        lInfoDialog.setSize(140, 90);
        lInfoDialog.setLocation(
                mMainFrame.getLocation().x + (mMainFrame.getWidth() / 2) - 70,
                mMainFrame.getLocation().y + (mMainFrame.getHeight() / 2) - 45);
        lInfoDialog.setLayout(new GridLayout(2, 1));

        String lText = "";
        if (pIsSuccess) {
            lText = "Converted";
        } else {
            lText = "Failed";
        }

        JLabel lMsg = new JLabel(lText, JLabel.CENTER);
        JButton lOk = new JButton("OK");

        lOk.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                lInfoDialog.setVisible(false);
                lInfoDialog.dispose();
            }
        });
        lInfoDialog.add(lMsg);
        lInfoDialog.add(lOk);
        lInfoDialog.setVisible(true);
        mEditor.setText("Drag and Drop : File or Directory");
    }
}