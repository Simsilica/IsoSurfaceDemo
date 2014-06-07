
import com.simsilica.lemur.*;
import com.simsilica.lemur.Button.ButtonAction;
import com.simsilica.lemur.component.*;

def gradient = TbtQuadBackgroundComponent.create( 
                                        texture( name:"/com/simsilica/lemur/icons/bordered-gradient.png", 
                                                 generateMips:false ),
                                                 1, 1, 1, 126, 126,
                                                 1f, false );

def border = TbtQuadBackgroundComponent.create(
                                        texture( name:"/com/simsilica/lemur/icons/border.png", 
                                                 generateMips:false ),
                                                 1, 2, 2, 6, 6,
                                                 1f, false );
 
                                  
selector( "nestedProperties.container", "glass" ) {
    background = border.clone();
    background.setColor(color(0.0, 0.0, 0.0, 0.5))
}

selector( "stats", "glass" ) {
    background = gradient.clone()
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "window", "glass" ) {
    background = gradient.clone()  
    background.setColor(color(0.25, 0.5, 0.5, 0.5))
}

selector( "root.rollup.title", "glass" ) {
    background = new QuadBackgroundComponent( color(0.25, 0.5, 0.85, 0.5) );
    background.texture = texture( name:"/com/simsilica/lemur/icons/double-gradient-128.png", 
                                  generateMips:false )
}
