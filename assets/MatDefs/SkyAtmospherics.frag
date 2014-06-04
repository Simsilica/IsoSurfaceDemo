
uniform vec3 m_LightDirection;
uniform float m_MpaFactor;

uniform float m_Exposure;

varying vec3 vBackDirection;
varying vec4 vRayleighColor;
varying vec4 vMieColor; 

#define GAMMA 0.5

void main() {
    vec3 lightPos = -m_LightDirection;
    float g = m_MpaFactor;
    float g2 = g * g;
    
    
    float fCos = dot(lightPos, vBackDirection) / length(vBackDirection);
    float fMiePhase = 1.5 * ((1.0 - g2) / (2.0 + g2)) * (1.0 + fCos * fCos) / pow(1.0 + g2 - 2.0 * g * fCos, 1.5);
    
    #ifndef GAMMA
        gl_FragColor = vRayleighColor + fMiePhase * vMieColor;
    #else
        vec4 color = vRayleighColor + fMiePhase * vMieColor;
        color *= m_Exposure; 
        gl_FragColor.xyz = pow(color.xyz, vec3(GAMMA));        
    #endif
	gl_FragColor.a = gl_FragColor.b;
}

