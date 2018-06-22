package kr.gyeongmunbaek.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class PatchInfo {
    public static final int BLACK_TICK = -16777216;
    public static final int RED_TICK = -65536;
    public final List<Rectangle> patches;
    public final List<Rectangle> fixed;
    public final List<Rectangle> horizontalPatches;
    public final List<Rectangle> verticalPatches;
    public final List<Pair<Integer>> horizontalPatchMarkers;
    public final List<Pair<Integer>> horizontalPaddingMarkers;
    public final List<Pair<Integer>> verticalPatchMarkers;
    public final List<Pair<Integer>> verticalPaddingMarkers;
    public final boolean verticalStartWithPatch;
    public final boolean horizontalStartWithPatch;
    public final Pair<Integer> horizontalPadding;
    public final Pair<Integer> verticalPadding;
    public BufferedImage image;

    public PatchInfo(BufferedImage image) {
        this.image = image;

        int width = image.getWidth();
        int height = image.getHeight();

        int[] row = GraphicsUtilities.getPixels(image, 0, 0, width, 1, null);
        int[] column = GraphicsUtilities
                .getPixels(image, 0, 0, 1, height, null);

        P left = getPatches(column);
        this.verticalStartWithPatch = left.startsWithPatch;
        this.verticalPatchMarkers = left.patches;

        P top = getPatches(row);
        this.horizontalStartWithPatch = top.startsWithPatch;
        this.horizontalPatchMarkers = top.patches;

        this.fixed = getRectangles(left.fixed, top.fixed);
        this.patches = getRectangles(left.patches, top.patches);
        if (!this.fixed.isEmpty()) {
            this.horizontalPatches = getRectangles(left.fixed, top.patches);
            this.verticalPatches = getRectangles(left.patches, top.fixed);
        } else if (!top.fixed.isEmpty()) {
            this.horizontalPatches = new ArrayList(0);
            this.verticalPatches = getVerticalRectangles(top.fixed);
        } else if (!left.fixed.isEmpty()) {
            this.horizontalPatches = getHorizontalRectangles(left.fixed);
            this.verticalPatches = new ArrayList(0);
        } else {
            this.horizontalPatches = (this.verticalPatches = new ArrayList(0));
        }
        row = GraphicsUtilities.getPixels(image, 0, height - 1, width, 1, row);
        column = GraphicsUtilities.getPixels(image, width - 1, 0, 1, height,
                column);

        top = getPatches(row);
        this.horizontalPaddingMarkers = top.patches;
        this.horizontalPadding = getPadding(top.fixed);

        left = getPatches(column);
        this.verticalPaddingMarkers = left.patches;
        this.verticalPadding = getPadding(left.fixed);
    }

    private List<Rectangle> getVerticalRectangles(List<Pair<Integer>> topPairs) {
        List<Rectangle> rectangles = new ArrayList();
        for (Pair<Integer> top : topPairs) {
            int x = ((Integer) top.first).intValue();
            int width = ((Integer) top.second).intValue()
                    - ((Integer) top.first).intValue();

            rectangles.add(new Rectangle(x, 1, width,
                    this.image.getHeight() - 2));
        }
        return rectangles;
    }

    private List<Rectangle> getHorizontalRectangles(
            List<Pair<Integer>> leftPairs) {
        List<Rectangle> rectangles = new ArrayList();
        for (Pair<Integer> left : leftPairs) {
            int y = ((Integer) left.first).intValue();
            int height = ((Integer) left.second).intValue()
                    - ((Integer) left.first).intValue();

            rectangles.add(new Rectangle(1, y, this.image.getWidth() - 2,
                    height));
        }
        return rectangles;
    }

    private Pair<Integer> getPadding(List<Pair<Integer>> pairs) {
        if (pairs.isEmpty()) {
            return new Pair(Integer.valueOf(0), Integer.valueOf(0));
        }
        if (pairs.size() == 1) {
            if (((Integer) ((Pair) pairs.get(0)).first).intValue() == 1) {
                return new Pair(
                        Integer.valueOf(((Integer) ((Pair) pairs.get(0)).second)
                                .intValue()
                                - ((Integer) ((Pair) pairs.get(0)).first)
                                        .intValue()), Integer.valueOf(0));
            }
            return new Pair(Integer.valueOf(0),
                    Integer.valueOf(((Integer) ((Pair) pairs.get(0)).second)
                            .intValue()
                            - ((Integer) ((Pair) pairs.get(0)).first)
                                    .intValue()));
        }
        int index = pairs.size() - 1;
        return new Pair(
                Integer.valueOf(((Integer) ((Pair) pairs.get(0)).second)
                        .intValue()
                        - ((Integer) ((Pair) pairs.get(0)).first).intValue()),
                Integer.valueOf(((Integer) ((Pair) pairs.get(index)).second)
                        .intValue()
                        - ((Integer) ((Pair) pairs.get(index)).first)
                                .intValue()));
    }

    private List<Rectangle> getRectangles(List<Pair<Integer>> leftPairs,
            List<Pair<Integer>> topPairs) {
        int y;
        int height;

        List<Rectangle> rectangles = new ArrayList();
        for (Pair<Integer> left : leftPairs) {

            y = ((Integer) left.first).intValue();
            height = ((Integer) left.second).intValue()
                    - ((Integer) left.first).intValue();
            for (Pair<Integer> top : topPairs) {
                int x = ((Integer) top.first).intValue();
                int width = ((Integer) top.second).intValue()
                        - ((Integer) top.first).intValue();

                rectangles.add(new Rectangle(x, y, width, height));
            }
        }
        return rectangles;
    }

    private static class P {
        public final List<Pair<Integer>> fixed;
        public final List<Pair<Integer>> patches;
        public final boolean startsWithPatch;

        private P(List<Pair<Integer>> f, List<Pair<Integer>> p, boolean s) {
            this.fixed = f;
            this.patches = p;
            this.startsWithPatch = s;
        }
    }

    private static P getPatches(int[] pixels) {
        int lastIndex = 1;

        boolean first = true;
        boolean startWithPatch = false;

        List<Pair<Integer>> fixed = new ArrayList();
        List<Pair<Integer>> patches = new ArrayList();

        assert (pixels.length > 2) : "Invalid 9-patch, cannot be less than 3 pixels in a dimension";

        int lastPixel = pixels[1] != -65536 ? pixels[1] : 0;
        for (int i = 1; i < pixels.length - 1; i++) {
            int pixel = pixels[i] != -65536 ? pixels[i] : 0;
            if (pixel != lastPixel) {
                if (lastPixel == -16777216) {
                    if (first) {
                        startWithPatch = true;
                    }
                    patches.add(new Pair(Integer.valueOf(lastIndex), Integer
                            .valueOf(i)));
                } else {
                    fixed.add(new Pair(Integer.valueOf(lastIndex), Integer
                            .valueOf(i)));
                }
                first = false;

                lastIndex = i;
                lastPixel = pixel;
            }
        }
        if (lastPixel == -16777216) {
            if (first) {
                startWithPatch = true;
            }
            patches.add(new Pair(Integer.valueOf(lastIndex), Integer
                    .valueOf(pixels.length - 1)));
        } else {
            fixed.add(new Pair(Integer.valueOf(lastIndex), Integer
                    .valueOf(pixels.length - 1)));
        }
        if (patches.isEmpty()) {
            patches.add(new Pair(Integer.valueOf(1), Integer
                    .valueOf(pixels.length - 1)));
            startWithPatch = true;
            fixed.clear();
        }
        return new P(fixed, patches, startWithPatch);
    }
}
