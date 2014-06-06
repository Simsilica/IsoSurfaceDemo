
uniform vec3 m_LightDirection;
uniform float m_Exposure;

varying vec3 direction;
varying vec3 v3Direction;
varying vec4 vColor;
varying vec4 vColor2; 

//#define GAMMA 0.5

const float rGround = 40.0/255.0; 
const float gGround = 88.0/255.0; 
const float bGround = 16.0/255.0; 

void main() {

    vec4 groundColor = vec4(rGround, gGround, bGround, 1.0);
    
    #ifndef GAMMA
        gl_FragColor = (vColor + groundColor * vColor2) * m_Exposure;
    #else
        vec4 color = vColor + groundColor * vColor2;
        color *= m_Exposure; 
        gl_FragColor.xyz = pow(color.xyz, vec3(GAMMA));        
    #endif
    gl_FragColor.a = 1.0;            
}

