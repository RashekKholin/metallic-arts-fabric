package dev.metallicarts.client;
/** Compatibility facade for existing sensory render hooks. */
public final class TinOptions {
 public static final TinOptions INSTANCE=new TinOptions();
 public boolean brighterVision(){return ClientPreferences.value(0);}
 public boolean louderSound(){return ClientPreferences.value(1);}
 public boolean showOverload(){return ClientPreferences.value(2);}
 private TinOptions(){}
}
