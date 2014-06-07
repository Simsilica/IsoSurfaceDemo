
#import "MatDefs/FragScattering.glsllib"

const float rGround = 40.0/255.0; 
const float gGround = 88.0/255.0; 
const float bGround = 16.0/255.0; 


void main() {

    vec4 groundColor = vec4(rGround, gGround, bGround, 1.0);
    
    #ifndef GAMMA
        gl_FragColor = calculateGroundColor(groundColor);
    #else
        vec4 color = calculateGroundColor(groundColor);
        gl_FragColor.xyz = pow(color.xyz, vec3(GAMMA));        
    #endif
    gl_FragColor.a = 1.0;            
}

