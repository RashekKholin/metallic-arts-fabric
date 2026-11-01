package dev.metallicarts.allomancy;
/** Camera-local projection: rear targets are compass markers, never mirrored perspective rays. */
public final class SightProjection {
 public record Point(double x,double y,boolean onScreen,boolean behind){}
 public static Point project(double right,double up,double forward,double fov,double aspect){
  if(forward<=.05){double bearing=Math.atan2(right,forward);return new Point(Math.clamp(bearing/Math.PI,-.94,.94),.90,false,true);}
  double tangent=Math.tan(Math.toRadians(Math.clamp(fov,1,179))/2);double x=right/(forward*tangent*aspect),y=-up/(forward*tangent);
  boolean inside=Math.abs(x)<=.94&&Math.abs(y)<=.90;double scale=Math.max(1,Math.max(Math.abs(x)/.94,Math.abs(y)/.90));return new Point(x/scale,y/scale,inside,false);
 }
 private SightProjection(){}
}
