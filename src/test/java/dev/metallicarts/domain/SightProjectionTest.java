package dev.metallicarts.domain;
import dev.metallicarts.allomancy.SightProjection;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SightProjectionTest {
 @Test void directlyAheadAndKnownDepthProjectWithoutDirectionDrift(){var a=SightProjection.project(0,0,10,90,1);assertTrue(a.onScreen());assertEquals(0,a.x(),1e-9);assertEquals(0,a.y(),1e-9);assertEquals(.5,SightProjection.project(5,0,10,90,1).x(),1e-9);assertEquals(.25,SightProjection.project(5,0,20,90,1).x(),1e-9);}
 @Test void rearTargetsAreLabelledCompassMarkersRatherThanMirroredSkyRays(){for(double x:new double[]{-10,-.001,0,.001,10}){var p=SightProjection.project(x,5,-10,90,1.5);assertTrue(p.behind());assertFalse(p.onScreen());assertEquals(.9,p.y());assertTrue(Double.isFinite(p.x())&&Math.abs(p.x())<=.94);if(x!=0)assertEquals(Math.signum(x),Math.signum(p.x()));}}
 @Test void horizonCrossingStaysFiniteAndFrontSignsRemainCorrect(){for(double z:new double[]{-.001,0,.001,.05,.051,1,10}){var p=SightProjection.project(10,-2,z,70,1.777);assertTrue(Double.isFinite(p.x()+p.y()));assertTrue(Math.abs(p.x())<=.940001&&Math.abs(p.y())<=.900001);}assertTrue(SightProjection.project(-2,2,10,70,1).x()<0);assertTrue(SightProjection.project(-2,2,10,70,1).y()<0);}
}
