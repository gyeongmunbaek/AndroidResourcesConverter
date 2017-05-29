package com.sm.part.android;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelGrabber;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.sm.part.android.image.GraphicsUtilities;
import com.sm.part.android.image.Pair;
import com.sm.part.android.image.PatchInfo;

public class ImageResizer {

    public class Patch {
        int mStart = 0;
        int mCount = 0;

        public Patch(int pStart, int pCount) {
            mStart = pStart;
            mCount = pCount;
        }
    }

    private BufferedImage mOriginalImage = null;
    private BufferedImage mNewImage = null;
    private PatchInfo mOriginalPatchInfo = null;

    public void process(File pSrcFile, float pRatio) throws Exception {
        if (mNewImage != null) {
            mNewImage = null;
        }
        mOriginalImage = ImageIO.read(pSrcFile);

        int srcWidth = mOriginalImage.getWidth();
        int srcHeight = mOriginalImage.getHeight();

        int type = getImgType();
        System.out.println("isNinePatchImage : " + pSrcFile.getName());
        if (isNinePatchImage(pSrcFile.getName())) {
            mOriginalPatchInfo = new PatchInfo(mOriginalImage);

            mOriginalImage = crop9Patch(mOriginalImage);

            srcWidth = mOriginalImage.getWidth();
            srcHeight = mOriginalImage.getHeight();

            mNewImage = resizeImageHighQuality(type, (int) (srcWidth * pRatio),
                    (int) (srcHeight * pRatio));

            convertTo9Patch();
        } else {
            mNewImage = resizeImageHighQuality(type, (int) (srcWidth * pRatio),
                    (int) (srcHeight * pRatio));
        }
    }

    public void createImageFile(String pDirectory, String pFileName) {
        File lDir = new File(pDirectory);
        if (lDir.exists() == false) {
            lDir.mkdir();
        } else {
            File[] destroy = lDir.listFiles();
            for (File des : destroy) {
                if (des.getName().equalsIgnoreCase(pFileName)) {
                    des.delete();
                }
            }
        }
        String outputFileName = pFileName.toLowerCase().trim();
        char[] outputFileNameArray = outputFileName.toCharArray();
        for (int index = 0; index < outputFileName.length(); index++) {
            System.out.println("outputFileNameArray[" + index + "] : " + outputFileNameArray[index]);
            System.out.println("outputFileNameArray[" + index + "] : " + (int)outputFileNameArray[index]);
            if ((int)outputFileNameArray[index] == 32) {
                outputFileNameArray[index] = '_';
            }
        }
        outputFileName = String.valueOf(outputFileNameArray);
        System.out.println("New Filename : " + outputFileName);
        File destFile = new File((pDirectory + "/" + outputFileName));
        try {
            if (mNewImage != null && destFile != null) {
                ImageIO.write(mNewImage, getImageFormat(pFileName), destFile);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getImageFormat(String pFileName) {
        return pFileName.substring(pFileName.lastIndexOf(".") + 1);
    }

    private boolean isNinePatchImage(String pFileName) {
        return pFileName.endsWith(".9.png");
    }

    private BufferedImage crop9Patch(BufferedImage image) {
        int lWidth = image.getWidth() - 2;
        int lHeight = image.getHeight() - 2;
        BufferedImage buffer = GraphicsUtilities
                .createTranslucentCompatibleImage(lWidth, lHeight);

        Graphics2D g2 = buffer.createGraphics();
        g2.drawImage(image, 0, 0, lWidth, lHeight, 1, 1, 1 + lWidth,
                1 + lHeight, null);
        g2.dispose();

        return buffer;
    }

    private BufferedImage convertTo9Patch() {
        BufferedImage buffer = GraphicsUtilities
                .createTranslucentCompatibleImage(mNewImage.getWidth() + 2,
                        mNewImage.getHeight() + 2);

        Graphics2D g2 = buffer.createGraphics();
        g2.drawImage(mNewImage, 1, 1, null);
        g2.dispose();

        mNewImage = buffer;

        make9Patch();

        return buffer;
    }

    private void make9Patch() {
        List<Patch> lNewVerPatches = new ArrayList();
        List<Patch> lNewHorPatches = new ArrayList();
        List<Patch> lNewVerPadding = new ArrayList();
        List<Patch> lNewHorPadding = new ArrayList();

        List<Pair<Integer>> lOriginalVerPatchPair = mOriginalPatchInfo.verticalPatchMarkers;
        List<Pair<Integer>> lOriginalHorPatchPair = mOriginalPatchInfo.horizontalPatchMarkers;
        List<Pair<Integer>> lOriginalVerPaddingPair = mOriginalPatchInfo.verticalPaddingMarkers;
        List<Pair<Integer>> lOriginalHorPaddingPair = mOriginalPatchInfo.horizontalPaddingMarkers;

        int lOriginalWidth = mOriginalPatchInfo.image.getWidth();
        int lOriginalHeight = mOriginalPatchInfo.image.getHeight();
        int lNewWidth = mNewImage.getWidth();
        int lNewHeight = mNewImage.getHeight();

        for (Pair<Integer> pair : lOriginalVerPatchPair) {
            int first = ((Integer) pair.first).intValue();
            int count = ((Integer) pair.second).intValue()
                    - ((Integer) pair.first).intValue();

            System.out.println("lOriginalVerPatchPair first : " + first
                    + " / count : " + count);
            first = getNewPatchValue(first, lNewHeight, lOriginalHeight, true);
            count = getNewPatchValue(count, lNewHeight, lOriginalHeight, false);
            count = checkPatchSize(first, count, lNewHeight);
            System.out.println("= lOriginalVerPatchPair first : " + first
                    + " / count : " + count);
            lNewVerPatches.add(new Patch(first, count));
        }

        for (Pair<Integer> pair : lOriginalHorPatchPair) {
            int first = ((Integer) pair.first).intValue();
            int count = ((Integer) pair.second).intValue()
                    - ((Integer) pair.first).intValue();

            System.out.println("lOriginalHorPatchPair first : " + first
                    + " / count : " + count);
            first = getNewPatchValue(first, lNewWidth, lOriginalWidth, true);
            count = getNewPatchValue(count, lNewWidth, lOriginalWidth, false);
            count = checkPatchSize(first, count, lNewWidth);
            System.out.println("= lOriginalHorPatchPair first : " + first
                    + " / count : " + count);
            lNewHorPatches.add(new Patch(first, count));
        }

        for (Pair<Integer> pair : lOriginalVerPaddingPair) {
            int first = ((Integer) pair.first).intValue();
            int count = ((Integer) pair.second).intValue()
                    - ((Integer) pair.first).intValue();

            System.out.println("lOriginalVerPaddingPair first : " + first
                    + " / count : " + count);
            first = getNewPatchValue(first, lNewHeight, lOriginalHeight, true);
            count = getNewPatchValue(count, lNewHeight, lOriginalHeight, false);
            count = checkPatchSize(first, count, lNewHeight);
            System.out.println("= lOriginalVerPaddingPair first : " + first
                    + " / count : " + count);
            lNewVerPadding.add(new Patch(first, count));
        }

        for (Pair<Integer> pair : lOriginalHorPaddingPair) {
            int first = ((Integer) pair.first).intValue();
            int count = ((Integer) pair.second).intValue()
                    - ((Integer) pair.first).intValue();

            System.out.println("lOriginalHorPaddingPair first : " + first
                    + " / count : " + count);
            first = getNewPatchValue(first, lNewWidth, lOriginalWidth, true);
            count = getNewPatchValue(count, lNewWidth, lOriginalWidth, false);
            count = checkPatchSize(first, count, lNewWidth);
            System.out.println("= lOriginalHorPaddingPair first : " + first
                    + " / count : " + count);
            lNewHorPadding.add(new Patch(first, count));
        }

        System.out.println("make9Patch lOriginalWidth : " + lOriginalWidth
                + " / lOriginalHeight : " + lOriginalHeight + " / lNewWidth : "
                + lNewWidth + " / lNewHeight : " + lNewHeight);

        ensure9Patch(lNewVerPatches, lNewHorPatches, lNewVerPadding, lNewHorPadding);

        return;
    }

    private int getNewPatchValue(int pValue, int pNewSize, int pOriginalSize,
            boolean pIsUp) {
        float result = (float) pValue * pNewSize / pOriginalSize;
        System.out.println("getNewPatchValue result : " + result);
        if (pIsUp && (result - (int) result) > 0.0) {
            result += 1;
        }
        if (((int)result) <= 0) {
            result = 1;
        }
        return (int) result;
    }
    
    private int checkPatchSize(int pFirst, int pCount, int pSize) {
        if ((pFirst + pCount) >= pSize) {
            pCount -= ((pFirst + pCount) - pSize + 1);
        }
        return pCount;
    }

    private void ensure9Patch(List<Patch> pVerPatches, List<Patch> pHorPatches,
            List<Patch> pVerPadding, List<Patch> pHorPadding) {
        int width = mNewImage.getWidth();
        int height = mNewImage.getHeight();
        System.out.println("ensure9Patch : " + width + " / height : " + height);

        for (Patch patch : pVerPatches) {
            int first = patch.mStart;
            int count = patch.mCount;

            for (int i = first; i < first + count; i++) {
                mNewImage.setRGB(0, i, -16777216);
            }
        }

        for (Patch patch : pHorPatches) {
            int first = patch.mStart;
            int count = patch.mCount;

            for (int i = first; i < first + count; i++) {
                mNewImage.setRGB(i, 0, -16777216);
            }
        }

        for (Patch patch : pVerPadding) {
            int first = patch.mStart;
            int count = patch.mCount;

            for (int i = first; i < first + count; i++) {
                mNewImage.setRGB(width - 1, i, -16777216);
            }
        }

        for (Patch patch : pHorPadding) {
            int first = patch.mStart;
            int count = patch.mCount;

            for (int i = first; i < first + count; i++) {
                mNewImage.setRGB(i, height - 1, -16777216);
            }
        }
    }

    private BufferedImage resizeImageHighQuality(int type, int width, int height)
            throws Exception {
        Image image = mOriginalImage.getScaledInstance(width, height,
                Image.SCALE_SMOOTH);
        int pixels[] = new int[width * height];
        PixelGrabber pixelGrabber = new PixelGrabber(image, 0, 0, width,
                height, pixels, 0, width);
        pixelGrabber.grabPixels();

        BufferedImage destImg = new BufferedImage(width, height, type);
        destImg.setRGB(0, 0, width, height, pixels, 0, width);

        return destImg;
    }

    public byte[] extractBytes(File ImageFile) throws IOException {
        BufferedImage bufferedImage = ImageIO.read(ImageFile);

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage.getRaster();
        DataBufferByte data = (DataBufferByte) raster.getDataBuffer();

        return (data.getData());
    }

    /*
     * private static final byte[] loadNinePatchChunk(String name) { InputStream
     * stream = name.getClass().getResourceAsStream(name); if (stream == null) {
     * return null; }
     * 
     * try { IntReader reader = new IntReader(stream, true); // check PNG
     * signature if (reader.readInt() != 0x89504e47 || reader.readInt() !=
     * 0x0D0A1A0A) { return null; } while (true) { int size = reader.readInt();
     * int type = reader.readInt(); // check for nine patch chunk type (npTc) if
     * (type != 0x6E705463) { reader.skip(size + 4 crc ); continue; } return
     * reader.readByteArray(size); } } catch (IOException e) { return null; } }
     */

    private int getImgType() {
        int imtType = 0;
        int originalImgType = mOriginalImage.getType();
        if (originalImgType == 0) {
            imtType = BufferedImage.TYPE_INT_ARGB;
        } else {
            imtType = originalImgType;
        }
        return imtType;
    }
}
