uniform mat4 g_WorldViewProjectionMatrix;
uniform mat4 g_ViewMatrix;
uniform mat4 g_ProjectionMatrix;
uniform mat4 g_ViewProjectionMatrix;
uniform mat4 g_WorldMatrix;
uniform vec3 g_CameraPosition;

uniform vec3 m_LightPosition;

uniform float m_KmESun;
uniform float m_InnerRadius;
uniform float m_RadiusScale;
uniform vec3 m_InvWavelengthsKrESun;
uniform vec3 m_KWavelengths4PI;        
uniform float m_AverageDensityScale;       
uniform float m_InvAverageDensityHeight;

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
    return m_AverageDensityScale * exp(-0.00287 + x*(0.459 + x*(3.83 + x*(-6.80 + x*5.25))));
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
    // we are actually looking at.  We put that in the caller's hands
    // though and make them find prescaled direction and distance.
    // By the time it gets in here it's already relative to the 
    // inner and outer radius.

    // Setup some relative constants and useful aliases
    float scaleDepth = m_AverageDensityScale;  
    float scaleOverScaleDepth = m_InvAverageDensityHeight; 
    float mESun = m_KmESun; 
 
    // Create a camera position relative to sea level
    // From here on, positions will be relative to sea level so that
    // they properly track the curve of the planet   
    vec3 camPos = vec3(0.0, m_InnerRadius + elevation, 0.0);  
    vec3 lightPos = m_LightPosition;
    
    float rayLength = distance; 
    
    // Setup to cast the ray sections for sample accumulation
    vec3 start = camPos;
    float height = start.y;  // camera is always centered so y is good enough for elevation.
    float offset = m_InnerRadius - height;
    float depth = exp(scaleOverScaleDepth * offset);
    float startAngle = dot(direction, start) / height;
    float startOffset = depth * scale(startAngle);
    
    // Setup the loop stepping
    float sampleLength = rayLength / fSamples;
    float scaledLength = sampleLength * m_RadiusScale;  // sampleLength * (1 / (outer - inner))  
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
        // m_KWavelengths4PI = K(wavelength) * 4 * PI
        //  = (m_InvWavelengths * r4PI + m4PI) 
        //vec3 attenuation = exp(-scatter * (m_InvWavelengths * r4PI + m4PI));
        vec3 attenuation = exp(-scatter * m_KWavelengths4PI);
        
        accumulator += attenuation * (depth * scaledLength);
        
        // Step the sample point to the next value
        samplePoint += sampleStep;

        // The in-scattering equation:
        // 
        // Iv(waveLength) = sunligh * K(wavelength) * F(theta, g)
        //    * diff[Pa-Pb] { 
        //          exp(-h/H0) * exp(-t(PPc, wavelength) - t(PPa, wavelength)) 
        //      }
        //
        //  PPc is the ray from the point to the sun.
        //  PPa is the ray from the point to the camera. 
        //
        // F(theta, g) is the phase function.  That's a complicated bit... but it's done
        //              in the frag shader.
        // 
        // K(wavelength) = Kr / pow(waveLength, 4) + Km
        //
        // ...for us invWaveLength is already 1/pow(wavelength, 4)
        // so K(wavelength) = Kr * invWaveLength + Km
        //
        // The full out-scattering equation is:
        // t(Pa, Pb, wavelength) = 4 * PI * K(wavelength) 
        //              * diff[Pa-Pb] { exp(-h/H0) }
        //
        // The ending differential equation seems to be the average
        // atmospheric density along the ray multiplied by the length.
        // It's a guess as to how many air particles exist along the ray.
        //
        // We are sampling so the differential equation unrolls and:
        // h is heightAbove seal level
        // H0 is the height of average density... in this case scaleDepth * (outer - inner)
        //
        // We've precalculated fScaleOverScaleDepth to avoid a divide
        //  1 / ((outer - inner) * scaleDepth)
        //
        // So, the differential part is actually:
        //   depth = exp(fScaleOverScaleDepth * (fInnerRadius - fHeight))
        //
        // So, t(waveLength) = 4 * PI * K(wavelength) * depth
        // K(wavelength) is invWavelength * Kr + km but we already have premultiplied
        // versions of the constants.
        // K(wavelength) = invWaveLength * Kr4PI + Km4PI
        // 
        // So, t(waveLength) = depth * (invWaveLength * Kr4PI + Km4PI)
        // ...and that's the out scattering function
        //
        //    
          
              
    }

    // Now set the out parameters
    
    // Mie color
    mColor = accumulator * mESun;
    
    // Rayleigh color
    rColor = accumulator * (m_InvWavelengthsKrESun);
} 

void main() {

    // The sky always moves with the camera and we adjust 
    // how we see it otherwise.
    vec4 pos = vec4(inPosition, 1.0);
    
    // The idea being that for elevation changes large enough
    // to affect atmospherics that we'd want to change the horizon
    // line and relative atmospheric depth and so on.  In reality,
    // this tends to distort the sun and I'm not sure why.
    // No flight sims for us, I guess. ;)    
    float elevation = 0.0;
    pos.y -= elevation;
     
    vec3 direction = pos.xyz;

    float distance = length(direction);
    direction /= distance;
    
    vec3 rColor = vec3(0.0, 0.0, 0.0);
    vec3 mColor = vec3(0.0, 0.0, 0.0);
                        
    calculateSkyInAtmosphere(direction, distance * m_PlanetScale, elevation * m_PlanetScale, rColor, mColor);
    vRayleighColor.rgb = rColor;
    vMieColor.rgb = mColor;

    vec4 temp = g_ViewMatrix * vec4(inPosition, 0.0);
    temp.w = 1.0;     
    gl_Position = g_ProjectionMatrix * temp;
    vBackDirection = -direction; 
}


