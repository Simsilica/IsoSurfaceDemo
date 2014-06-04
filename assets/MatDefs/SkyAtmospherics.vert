uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_ViewMatrix;
uniform mat4 g_ProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform vec3 g_CameraPosition;

uniform vec4 m_ScatteringConstants;
uniform float m_MpaFactor;
uniform vec3 m_LightDirection;
uniform float m_LightIntensity;
uniform float m_Exposure;
uniform float m_InnerRadius;
uniform float m_OuterRadius;
uniform float m_RadiusScale;
uniform vec3 m_InvWaveLength;
uniform float m_RayleighScaleDepth;       
uniform float m_MieScaleDepth;

uniform float m_PlanetScale;
uniform float m_Flattening;


attribute vec3 inPosition;
attribute vec3 inNormal;

varying vec3 vBackDirection;
varying vec4 vRayleighColor;
varying vec4 vMieColor;

const int nSamples = 2;
const float fSamples = 2.0;

//const int nSamples = 4;
//const float fSamples = 4.0;

float scale( float fCos ) {
	float x = 1.0 - fCos;
	return m_RayleighScaleDepth * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
}

void calculateSkyInAtmosphere( in vec3 direction, in float distance, in float elevation, out vec3 rColor, out vec3 mColor ) {

    // We always calculate relative to the 'eye' elevation
    // and we assume that othrewise our sky moves around with us
    // in the ground plane.  Consequently, we need to factor in
    // the inner radius when plotting the ray direction through
    // the atmosphere.  The nice part is that we can scale things
    // more freely this way.  For example, we could have an
    // inner radius of 10 and an outer radius of 10.25 and simply
    // scale 'meters' to those values.  Smaller values are good
    // because pumping large values into exp() can result in
    // infinity pretty quickly.
    //
    // So, consider that a direction straight ahead does not mean
    // we are looking at the 0 latitude of the sphere.  The planet 
    // scale versus the radius tells us which part of the sphere
    // we are actually looking at.
    //float planetScale = 0.001;  // needs to be a parameter
    float planetScale = m_PlanetScale; //1.0;  // needs to be a parameter

    // Setup some relative constants and useful aliases
    float scaleDepth = m_RayleighScaleDepth;  
    float scaleOverScaleDepth = (1.0 / (m_OuterRadius - m_InnerRadius)) / scaleDepth;
    float rESun = m_ScatteringConstants.x * m_LightIntensity;
    float mESun = m_ScatteringConstants.z * m_LightIntensity;
    float r4PI = m_ScatteringConstants.y;    
    float m4PI = m_ScatteringConstants.w;
 
    // Create a camera position relative to sea level
    // From here on, positions will be relative to sea level so that
    // they properly track the curve of the planet   
    vec3 camPos = vec3(0.0, m_InnerRadius + elevation * planetScale, 0.0);  
    vec3 lightPos = -m_LightDirection;
    
    float rayLength = distance * planetScale;
    
    // Setup to cast the ray sections for sample accumulation
    vec3 start = camPos;
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
 
    vec3 accumulator = vec3(0.0, 0.0, 0.0);
    for( int i = 0; i < nSamples; i++ ) {
    
        // length(samplePoint) is the accurate planet-centric elevation but I find sometimes
        // flattening it out a bit makes better looking visuals... essentially
        // we are removing the curve of the earth from height.
        height = mix(length(samplePoint), samplePoint.y, m_Flattening);                                 
        offset = m_InnerRadius - height;
        depth = exp(scaleOverScaleDepth * offset);
  
        float lightAngle = dot(lightPos, samplePoint) / height;
        float cameraAngle = dot(direction, samplePoint) / height;
 
        float scatter = startOffset + depth * (scale(lightAngle) - scale(cameraAngle));

        // m_InvWaveLength = 1 / (waveLength ^ 4)
        vec3 attenuation = exp(-scatter * (m_InvWaveLength * r4PI + m4PI));
        
        accumulator += attenuation * (depth * scaledLength);
        
        // Step the sample point to the next value
        samplePoint += sampleStep;      
    }

    // Now set the out parameters
    
    // Mie color
    mColor = accumulator * mESun;
    
    // Rayleigh color
    rColor = accumulator * (m_InvWaveLength * rESun);
} 

void main() {

    // Right now we calculate as if we are in real world
    // space inside a giant dome.
	vec3 direction = inPosition - g_CameraPosition;
	float distance = length(direction);
	direction /= distance;
	float elevation = g_CameraPosition.y - m_InnerRadius;
	vec3 rColor = vec3(0.0, 0.0, 0.0);
	vec3 mColor = vec3(0.0, 0.0, 0.0);
	                    
    calculateSkyInAtmosphere(direction, distance, elevation, rColor, mColor );
    vRayleighColor.rgb = rColor;
    vMieColor.rgb = mColor;
	
	gl_Position = g_WorldViewProjectionMatrix * vec4(inPosition, 1.0);
	vBackDirection = g_CameraPosition - inPosition;
}


