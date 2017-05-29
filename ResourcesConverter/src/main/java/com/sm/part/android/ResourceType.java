package com.sm.part.android;

public enum ResourceType {
    IMAGE {
        @Override
        public String getString() {
            return "IMAGE";
        }
    },
    DIMEN {
        @Override
        public String getString() {
            return "DIMEN";
        }
    };
    public abstract String getString();
}
