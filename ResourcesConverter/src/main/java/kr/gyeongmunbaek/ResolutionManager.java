package kr.gyeongmunbaek;

public class ResolutionManager {
    private String[] mWidthArray = {
            "w320dp",
            "w360dp",
            "w384dp",
            "w411dp",
            "w480dp",
            "w540dp",
            "w560dp",
            "w600dp",
            "w800dp",
            "w960dp",
            "w1024dp",
            "w1280dp",
            };
    private String[] mDpiArray = {
            "mdpi",
            "hdpi",
            "xhdpi",
            "xxhdpi",
            "xxxhdpi",
            "tvdpi"};
    private String mStandardWidth = "w360dp";
    private String mStandardDpi = "xxxhdpi";
    
    private ResourceType mType = ResourceType.DIMEN;
    
    public void setResourceType(ResourceType pType) {
        mType = pType;
    }
    
    public String[] getResolutionArray() {
        if (mType == ResourceType.DIMEN) {
            return mWidthArray;
        } else if (mType == ResourceType.IMAGE) {
            return mDpiArray;
        }
        return null;
    }
    
    public void setStandardResolution(String pStandard) {
        if (mType == ResourceType.DIMEN) {
            mStandardWidth = pStandard;
        } else if (mType == ResourceType.IMAGE) {
            mStandardDpi = pStandard;
        }
    }
    
    private float getStandardWidth() {
        int lDpIndex = mStandardWidth.indexOf("dp");
        String lSubString = mStandardWidth.substring(1, lDpIndex);
        return Float.parseFloat(lSubString);
    }
    
    private float getWidth(int pArrayIndex) {
        int lDpIndex = mWidthArray[pArrayIndex].indexOf("dp");
        String lSubString = mWidthArray[pArrayIndex].substring(1, lDpIndex);
        return Float.parseFloat(lSubString);
    }
    
    public float getRatio(int pArrayIndex) {
        if (mType == ResourceType.DIMEN) {
            return getWidth(pArrayIndex) / getStandardWidth();
        } else if (mType == ResourceType.IMAGE) {
            return getDpiValue(getResolutionArray()[pArrayIndex]) / getDpiValue(mStandardDpi);
        }
        return 1.0f;
    }
    
    private float getDpiValue(String pDpi) {
        if (pDpi.equals("mdpi")) {
            return 1.0f;
        } else if (pDpi.equals("hdpi")) {
            return 1.5f;
        } else if (pDpi.equals("xhdpi")) {
            return 2.0f;
        } else if (pDpi.equals("xxhdpi")) {
            return 3.0f;
        } else if (pDpi.equals("xxxhdpi")) {
            return 4.0f;
        } else if (pDpi.equals("tvdpi")) {
            return 1.3f;
        }
        return 1.0f;
    }
    
    public int getDefaultIndex() {
        String lStandard = null;
        String[] lArray = null;
        if (mType == ResourceType.DIMEN) {
            lStandard = mStandardWidth;
            lArray = mWidthArray;
        } else if (mType == ResourceType.IMAGE) {
            lStandard = mStandardDpi;
            lArray = mDpiArray;
        }
        if (lStandard != null) {
            for (int index = 0; index < lArray.length; index++) {
                if (lArray[index].equals(lStandard)) {
                    return index;
                }
            }
        }
        return 0;
    }
}
