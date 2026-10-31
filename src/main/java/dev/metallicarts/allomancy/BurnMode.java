package dev.metallicarts.allomancy;

public enum BurnMode {
    OFF,BURN,FLARE;
    public static BurnMode from(int id){if(id<0||id>=values().length)throw new IllegalArgumentException("Unknown burn mode");return values()[id];}
}
