uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_ViewProjectionMatrix;
uniform mat4 g_ViewMatrix;
uniform mat4 g_ProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform vec3 g_CameraPosition;

uniform vec3 m_LightPosition;

#import "MatDefs/VertScattering.glsllib"


attribute vec3 inPosition;
attribute vec3 inNormal;

void main() {

    vec4 modelSpacePos = vec4(inPosition, 1.0);
    
    vec4 wPos = g_WorldMatrix * modelSpacePos;

    calculateVertexGroundScattering(wPos.xyz, g_CameraPosition, m_LightPosition);
    
    gl_Position = g_ViewProjectionMatrix * wPos;
}



