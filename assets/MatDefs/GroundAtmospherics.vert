uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_ViewProjectionMatrix;
uniform mat4 g_ViewMatrix;
uniform mat4 g_ProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform vec3 g_CameraPosition;

uniform vec4 m_ScatteringConstants;
uniform float m_MpaFactor;
uniform vec3 m_LightPosition;
uniform float m_LightIntensity;
uniform float m_Exposure;
uniform float m_InnerRadius;
uniform float m_OuterRadius;
uniform float m_RadiusScale;
uniform vec3 m_InvWavelengths;
uniform float m_AverageDensityScale;       
uniform float m_InvAverageDensityHeight;


attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 direction;
varying vec4 vColor;
varying vec4 vColor2; 

const int nSamples = 2;
const float fSamples = 2.0;
//const int nSamples = 4;
//const float fSamples = 4.0;


float scale(float fCos)
{
	float x = 1.0 - fCos;
	return m_AverageDensityScale * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
}

void calculateGroundInAtmosphere( in vec3 direction, in float distance, in float elevation, out vec3 rColor, out vec3 mColor ) {


    // Setup some relative constants and useful aliases
    float scaleDepth = m_AverageDensityScale;  
    float scaleOverScaleDepth = m_InvAverageDensityHeight; //(1.0 / (m_OuterRadius - m_InnerRadius)) / scaleDepth;
    float rESun = m_ScatteringConstants.x * m_LightIntensity;
    float mESun = m_ScatteringConstants.z * m_LightIntensity;
    float r4PI = m_ScatteringConstants.y;    
    float m4PI = m_ScatteringConstants.w;
 
    // Create a camera position relative to sea level
    // From here on, positions will be relative to sea level so that
    // they properly track the curve of the planet   
    vec3 camPos = vec3(0.0, m_InnerRadius + elevation, 0.0);  
    vec3 lightPos = m_LightPosition;
    
    float rayLength = distance;
    
    // Setup to cast the ray sections for sample accumulation
    vec3 start = camPos;
    
    // Trying something... going to try doing the ray backwards
    start = camPos + direction * distance;
    direction *= -1.0;
    
    float height = start.y;  // camera is always centered so y is good enough for elevation.
    float offset = m_InnerRadius - height;
    float depth = exp(scaleOverScaleDepth * offset);
    float startAngle = dot(direction, start) / height;
    float startOffset = depth * scale(startAngle);
    
    // Setup the loop stepping
    float sampleLength = rayLength / fSamples;
    float scaledLength = sampleLength * m_RadiusScale;  // samppleLength * (1 / (outer - inner))  
    vec3 sampleStep = direction * sampleLength;
    vec3 samplePoint = start + sampleStep * 0.5; // samples are in the middle of the sample ray
    float scatter = 0.0;
 
    vec3 accumulator = vec3(0.0, 0.0, 0.0);
    vec3 attenuation = vec3(0.0, 0.0, 0.0);
    for( int i = 0; i < nSamples; i++ ) {
 
        // Ground points are generally always close enough that we pretend
        // the world is flat.   
        height = samplePoint.y;                                 
        offset = m_InnerRadius - height;
        depth = exp(scaleOverScaleDepth * offset);
  
        float lightAngle = dot(lightPos, samplePoint) / height;
        float cameraAngle = dot(direction, samplePoint) / height;
 
        scatter = startOffset + depth * (scale(lightAngle) - scale(cameraAngle));

        // m_InvWaveLength = 1 / (waveLength ^ 4)
        attenuation = exp(-scatter * (m_InvWavelengths * r4PI + m4PI));
        
        accumulator += attenuation * (depth * scaledLength);
        
        // Step the sample point to the next value
        samplePoint += sampleStep;
    }

    // Now set the out parameters
    
    // General attenuation... we stick it in the Mie color because I'm lazy
    mColor = attenuation; 
    
    // Rayleigh color
    rColor = accumulator * (m_InvWavelengths * rESun + mESun);
} 

void main() {

    vec4 modelSpacePos = vec4(inPosition, 1.0);
    
    vec4 wPos = g_WorldMatrix * modelSpacePos;
    vec3 direction = wPos.xyz - g_CameraPosition;
    
    // Need to make this a parameter
    float scale = 1.0 / 4000.0;
//    float scale = 1.0 / 200.0;
    direction *= scale;
    float distance = length(direction);
    //distance = max(distance, 0.1); 

    vec3 rColor = vec3(0.0, 0.0, 0.0);
    vec3 mColor = vec3(0.0, 0.0, 0.0);
    
    calculateGroundInAtmosphere( direction, distance, g_CameraPosition.y * scale, rColor, mColor );
    
    vColor.rgb = rColor;
    vColor.a = 1.0;
    vColor2.rgb = mColor;
    vColor2.a = 1.0; 
    gl_Position = g_ViewProjectionMatrix * wPos;
}



